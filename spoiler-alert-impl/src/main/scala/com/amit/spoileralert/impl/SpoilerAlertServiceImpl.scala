package com.amit.spoileralert.impl

import java.util.UUID

import com.amit.spoileralert.api
import com.amit.spoileralert.api.SpoilerAlertService
import akka.Done
import akka.NotUsed
import akka.actor.typed.ActorRef
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
import com.amit.spoiler.{SeriesSpoiler, SpoilerResponse, UserSeriesStatus}
import com.amit.spoileralert.impl.daos.{ UserSeriesDao}
import com.amit.spoileralert.impl.entity.{Accepted, Confirmation, CreateSpoilerAlert, GetSpoilerAlert, Rejected, SpoilerAlertBehavior, SpoilerAlertCommand, SpoilerAlertState}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode.UnexpectedCondition
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.Logger

import scala.collection.immutable

/**
  * Implementation of the SpoilerAlertService.
  */
class SpoilerAlertServiceImpl(
  clusterSharding: ClusterSharding,
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
      handleNewEntity(uss).flatMap(x => x match {
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

  override def getSpoilers: ServiceCall[Seq[String], Seq[SpoilerResponse]] = {
    ServerServiceCall { uss =>
      handleSpoilersQuery(uss)
    }
  }



  override def getSameProgressUsers(username: String, seriesname: String): ServiceCall[NotUsed, Seq[String]] = {
    ServerServiceCall {  uss =>
      getSpoilerAlertEntity(username,seriesname).flatMap(x => x match {
        case None => Future.successful(throw BadRequest(s"Spoiler Alert for username ${username} seriesname ${seriesname} not found."))
        case Some(entity) =>handleSameProgress(entity)
      })
    }
  }


  private def logged[Request, Response](serviceCall: ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] =
    ServerServiceCall.compose { requestHeader =>
      val caller = requestHeader.principal.map(x => x.getName).getOrElse("")
      logger.info(messagesApi("service.request.info", caller, requestHeader.method, requestHeader.uri)(lang))
      serviceCall
    }

  private def handleNewEntity(input: UserSeriesStatus):Future[Option[UserSeriesStatus]] = {
    getSpoilerAlertEntity(input.userName,input.seriesName).map(x =>  x match{
      case None => Option(input.copy(id =Option(UUIDs.timeBased()),key = Option(input.identifier)))
      case Some(uss) =>
        logger.error(s"entity with key ${uss.key.getOrElse("")} username ${uss.userName} series name ${uss.seriesName}" +
          s" already exist. Unable to create new one. Use update api")
        None
    })
  }

  private def handleSameProgress(input: UserSeriesStatus):Future[Seq[String]] = {
    userSeriesDao.getBySeriesPercentage(input.seriesName,input.percentage)
      .map(_ map(_.userName)filterNot( _ == input.userName))
  }

  private def handleSpoilersQuery(input: Seq[String]):Future[Seq[SpoilerResponse]] = {
    userSeriesDao.getByUsers(input).map { entities =>
      input.map { user =>
        val seriesSpoilers = entities.groupBy(_.seriesName).map { s =>
          val series = s._1
          val spoilerFor = s._2.find(e => e.userName == user && e.seriesName == series)
          val spoilers = spoilerFor.map { l =>
            s._2.filter(k => k.userName != l.userName && k.percentage > l.percentage).map(_.userName)
          }.getOrElse(Seq.empty)
          SeriesSpoiler(series, spoilers)
        }
        SpoilerResponse(user, seriesSpoilers.toSeq)
      }
    }
  }

  private def handleSameProgressQuery(input: UserSeriesStatus):Future[Seq[String]] = {
    userSeriesDao.getBySeriesPercentage(input.seriesName,input.percentage)
      .map(_ map(_.userName)filterNot( _ == input.userName))
  }


  private def getSpoilerAlertEntity(username: String, seriesname: String):Future[Option[UserSeriesStatus]] = {
    val identifier = UserSeriesStatus.getIdentifier(username,seriesname)
    entityRef(identifier).ask[Confirmation]{ reply =>
      GetSpoilerAlert(reply)
    }.map {
      case x:Accepted => Option(x.summary.userSeriesStatus)
      case _        => None
    }
  }

}
