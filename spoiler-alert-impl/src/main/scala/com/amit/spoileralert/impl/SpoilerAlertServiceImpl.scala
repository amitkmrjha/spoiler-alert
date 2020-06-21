package com.amit.spoileralert.impl

import java.util.UUID

import com.amit.spoileralert.api
import com.amit.spoileralert.api.SpoilerAlertService
import akka.Done
import akka.NotUsed
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import com.amit.spoiler.UserSeriesStatus
import com.amit.spoileralert.impl.daos.{AutoKeyDao, UserSeriesDao}
import com.amit.spoileralert.impl.entity.{Accepted, Confirmation, CreateSpoilerAlert, GetSpoilerAlert, Rejected, SpoilerAlertBehavior, SpoilerAlertCommand, SpoilerAlertState}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode.UnexpectedCondition
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.Logger

/**
  * Implementation of the SpoilerAlertService.
  */
class SpoilerAlertServiceImpl(
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry,
  autoKeyDao: AutoKeyDao,
  userSeriesDao: UserSeriesDao,
  messagesApi: MessagesApi,
  languages: Langs
)(implicit ec: ExecutionContext)  extends SpoilerAlertService {

  implicit val lang: Lang = languages.availables.head

  val logger = Logger(this.getClass)

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[SpoilerAlertCommand] = clusterSharding.entityRefFor(SpoilerAlertBehavior.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  /**
   * Example: curl -H "Content-Type: application/json" -X POST -d '{"userName:":
   * "abc",seriesName = "GOT", percentage = 10}' http://localhost:9000/api/vi/userseries
   */
  override def inputUserSeriesProgress: ServiceCall[UserSeriesStatus, UserSeriesStatus] = logged(
    ServerServiceCall {  uss =>

      processNewEntity(uss).flatMap(x => x match {
        case None => Future.successful(throw BadRequest(s"Entity already exists."))
        case Some(newEntity) =>
          entityRef(newEntity.key.getOrElse("")).ask[Confirmation]{ reply =>
          CreateSpoilerAlert(newEntity,reply)
        }.map {
            case x:Accepted => x.summary.userSeriesStatus
            case x:Rejected        => throw BadRequest(s"${x.reason} ")
            case _        => throw BadRequest(s"Internal Server Error occurred.")
          }
      })
    }
  )

  override def getUserSeriesProgress(key: String): ServiceCall[NotUsed, UserSeriesStatus] = logged(
    ServerServiceCall {  _ =>
      entityRef(key).ask[Confirmation]{ reply =>
       GetSpoilerAlert(reply)
      }.map {
        case x:Accepted => x.summary.userSeriesStatus
        case x:Rejected        => throw NotFound(s"${x.reason} for key ${key}")
        case _        => throw NotFound(s"Unable find persistent entity with key ${key}.")
      }
    }
  )

  private def logged[Request, Response](serviceCall: ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] =
    ServerServiceCall.compose { requestHeader =>
      val caller = requestHeader.principal.map(x => x.getName).getOrElse("")
      logger.info(messagesApi("service.request.info", caller, requestHeader.method, requestHeader.uri)(lang))
      serviceCall
    }

  private def processNewEntity(input: UserSeriesStatus):Future[Option[UserSeriesStatus]] = {
    userSeriesDao.getbyUserAndSeries(input.userName,input.seriesName).flatMap(x =>  x match{
      case None => getAutoKey.map(ak => Option(input.copy(id =Option(UUIDs.timeBased()),key = ak)))
      case Some(uss) =>
        logger.error(s"entity with key ${uss.key.getOrElse("")} username ${uss.userName} series name ${uss.seriesName}" +
          s" already exist. Unable to create new one. Use update api")
        Future.successful(None)
    })
  }

  private def getAutoKey: Future[Option[String]] = {
    autoKeyDao.getNextKey(KeyUserSeries()).map {
      case Some(autoKey) => Some(s"${autoKey.keyNumber}")
      case None => None
    }
  }

}
