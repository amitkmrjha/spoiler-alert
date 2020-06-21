package com.amit.spoileralert.impl.daos

import java.util
import java.util.UUID

import akka.Done
import com.amit.spoiler.UserSeriesStatus
import com.datastax.driver.core.querybuilder.{Delete, Insert}
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future, Promise}

trait ReadSideTable[T <: UserSeriesStatus] {

  private val logger = Logger(this.getClass)

  protected val insertPromise: Promise[PreparedStatement] = Promise[PreparedStatement]

  protected val deletePromise: Promise[PreparedStatement] = Promise[PreparedStatement]

  protected def tableName: String

  protected def primaryKey: String

  protected def tableScript: String

  protected def fields: Seq[String]

  protected def prepareDelete: Delete.Where

  protected def getDeleteBindValues(entity: T): Seq[AnyRef]

  protected def cL: util.List[String]

  protected def vL: util.List[AnyRef]

  protected def prepareInsert: Insert

  protected def getInsertBindValues(entity: T): Seq[AnyRef]

  protected def getAllQueryString: String

  protected def getCountQueryString: String

  protected def id(e: T): UUID = e.id.getOrElse(UUIDs.timeBased())

  protected def key(e: T): String = e.key.getOrElse(s"FixMeKey-${UUIDs.timeBased()}")

  protected def percentage(e: T): Double = e.percentage


  def createTable()
                 (implicit session: CassandraSession, ec: ExecutionContext): Future[Done] = {
    for {
      _ <- sessionExecuteCreateTable(tableScript)
    } yield Done
  }

  protected def sessionExecuteCreateTable(tableScript: String)
                                         (implicit session: CassandraSession, ec: ExecutionContext): Future[Done] = {
    session.executeCreateTable(tableScript).recover {
      case ex: Exception =>
        logger.error(s"Store MS CreateTable $tableScript execute error => ${ex.getMessage}", ex)
        throw ex
    }
  }

  def prepareStatement()
                      (implicit session: CassandraSession, ec: ExecutionContext): Future[Done] = {
    val insertRepositoryFuture = sessionPrepare(prepareInsert.toString)
    insertPromise.completeWith(insertRepositoryFuture)
    val deleteRepositoryFuture = sessionPrepare(prepareDelete.toString)
    deletePromise.completeWith(deleteRepositoryFuture)
    for {
      _ <- insertRepositoryFuture
      _ <- deleteRepositoryFuture
    } yield Done
  }

  protected def sessionPrepare(stmt: String)
                              (implicit session: CassandraSession, ec: ExecutionContext): Future[PreparedStatement] = {
    session.prepare(stmt).recover {
      case ex: Exception =>
        logger.error(s"Statement $stmt prepare error => ${ex.getMessage}", ex)
        throw ex
    }
  }

  protected def bindPrepare(ps: Promise[PreparedStatement], bindV: Seq[AnyRef])(implicit session: CassandraSession, ec: ExecutionContext): Future[BoundStatement] = {
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
}

trait ReadSideDomainTable[T <: UserSeriesStatus] extends ReadSideTable[T] {
  def insert(t: T)
            (implicit session: CassandraSession, ec: ExecutionContext): Future[Option[BoundStatement]] = {
    val bindV = getInsertBindValues(t)
    bindPrepare(insertPromise, bindV).map(x => Some(x))
  }

  def delete(t: T)
            (implicit session: CassandraSession, ec: ExecutionContext): Future[Option[BoundStatement]] = {
    val bindV = getDeleteBindValues(t)
    bindPrepare(deletePromise, bindV).map(x => Some(x))
  }
}

