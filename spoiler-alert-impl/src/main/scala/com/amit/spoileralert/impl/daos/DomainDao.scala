package com.amit.spoileralert.impl.daos

import java.time.LocalDateTime
import java.util.UUID

import com.amit.spoiler.{Columns, UserSeriesStatus}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

trait DomainDao[T <: UserSeriesStatus] {

  def count: Future[Option[Long]]

  protected def convert(r: Row): T

  protected def sessionSelectAll(queryString: String): Future[Seq[T]]

  protected def sessionSelectOne(queryString: String): Future[Option[T]]

  protected def sessionSelectCount(queryString: String): Future[Option[Long]]

  protected def key(r: Row): Option[String] = {
   Option(r.getString(Columns.Key))
  }

  protected def id(r: Row): Option[UUID] = {
    Option(r.getUUID(Columns.Id))
  }

  protected def userName(r: Row): String =
    r.getString(Columns.UserName)

  protected def seriesName(r: Row): String =
    r.getString(Columns.SeriesName)

  protected def percentage(r: Row): Double =
    r.getDouble(Columns.Percentage)
}



abstract class AbstractDomainDao[T <: UserSeriesStatus]
(session: CassandraSession, messagesApi: MessagesApi, languages: Langs)(implicit ec: ExecutionContext)
  extends DomainDao[T] {

  override protected def sessionSelectAll(queryString: String): Future[Seq[T]] = {
    session.selectAll(queryString).map(_.map(convert))
  }

  override protected def sessionSelectOne(queryString: String): Future[Option[T]] = {
    session.selectOne(queryString).map(_.map(convert))
  }

  override protected def sessionSelectCount(queryString: String): Future[Option[Long]] = ???/*{
    session.selectOne(queryString).map(_.map(count))
  }*/


}
