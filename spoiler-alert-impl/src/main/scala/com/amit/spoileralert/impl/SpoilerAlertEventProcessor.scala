package com.amit.spoileralert.impl

import akka.Done
import com.amit.spoiler.UserSeriesStatus
import com.amit.spoileralert.impl.daos.{AutoKeyTable, UserSeriesByKeyTable, UserSeriesByUserTable, UserSeriesTable}
import com.amit.spoileralert.impl.entity.{SpoilerAlertCreated, SpoilerAlertDeleted, SpoilerAlertEvent, SpoilerAlertUpdated}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}
import play.api.Logger
import play.api.i18n.{Langs, MessagesApi}
import scala.language.postfixOps

import scala.concurrent.{ExecutionContext, Future, Promise}

private[impl] class SpoilerAlertEventProcessor(session: CassandraSession,
                                               readSide: CassandraReadSide,
                                               messagesApi: MessagesApi,
                                               languages: Langs)
                                              (implicit ec: ExecutionContext)
  extends ReadSideProcessor[SpoilerAlertEvent] {

  val logger = Logger(this.getClass)

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[SpoilerAlertEvent] = {
    readSide.builder[SpoilerAlertEvent]("SpoilerAlertEventEventOffset")
      //.setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      //Repository event Handler
      .setEventHandler[SpoilerAlertCreated](e => spoilerAlertInsert(e.event.userSeriesStatus))
      .setEventHandler[SpoilerAlertUpdated](e => spoilerAlertInsert(e.event.userSeriesStatus))
      .setEventHandler[SpoilerAlertDeleted](e => spoilerAlertDelete(e.event.userSeriesStatus))

      .build()
  }

  override def aggregateTags: Set[AggregateEventTag[SpoilerAlertEvent]] =  Set(SpoilerAlertEvent.Tag)

  private def createTables() = {
    for {
      _ <- UserSeriesTable.createTable()(session, ec)
      _ <- UserSeriesByKeyTable.createTable()(session, ec)
      _ <- UserSeriesByUserTable.createTable()(session, ec)

      _ <- sessionExecuteCreateTable(AutoKeyTable.tableScript)
    } yield Done
  }

  private def spoilerAlertInsert(entity: UserSeriesStatus) = {
    for {
      ie<- UserSeriesTable.insert(entity)(session, ec)
      iebk <- UserSeriesByKeyTable.insert(entity)(session, ec)
      iebu <- UserSeriesByUserTable.insert(entity)(session, ec)
    } yield List(ie, iebk,iebu) flatten
  }

  private def spoilerAlertDelete(entity: UserSeriesStatus) = {
    for {
      ie<- UserSeriesTable.insert(entity)(session, ec)
      iebk <- UserSeriesByKeyTable.insert(entity)(session, ec)
      iebu <- UserSeriesByUserTable.insert(entity)(session, ec)
    } yield List(ie, iebk,iebu) flatten
  }

  private def bindPrepare(ps: Promise[PreparedStatement], bindV: Seq[AnyRef]): Future[BoundStatement] = {
    ps.future.map(x =>
      try {
        x.bind(bindV: _*)
      } catch {
        case ex: Exception =>
          logger.error(s"bindPrepare ${x.getQueryString} => ${ex.getMessage}", ex)
          throw ex
      }
    )
  }

  private def prepareStatements() = {
    for {
      _ <- UserSeriesTable.prepareStatement()(session, ec)
      _ <- UserSeriesByKeyTable.prepareStatement()(session, ec)
      _ <- UserSeriesByUserTable.prepareStatement()(session, ec)
    } yield {
      Done
    }
  }

  private def sessionExecuteCreateTable(tableScript: String): Future[Done] = {
    session.executeCreateTable(tableScript).recover {
      case ex: Exception =>
        logger.error(s"Store MS CreateTable $tableScript execute error => ${ex.getMessage}", ex)
        throw ex
    }
  }

  private def sessionPrepare(stmt: String): Future[PreparedStatement] = {
    session.prepare(stmt).recover {
      case ex: Exception =>
        logger.error(s"Statement $stmt prepare error => ${ex.getMessage}", ex)
        throw ex
    }
  }
}
