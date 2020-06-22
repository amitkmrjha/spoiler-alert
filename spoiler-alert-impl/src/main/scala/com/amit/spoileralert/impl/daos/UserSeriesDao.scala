package com.amit.spoileralert.impl.daos

import com.amit.spoiler.UserSeriesStatus
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.Logger
import play.api.i18n.{Langs, MessagesApi}

import scala.concurrent.{ExecutionContext, Future}

private[impl] class UserSeriesDao(session: CassandraSession, messagesApi: MessagesApi, languages: Langs)(implicit ec: ExecutionContext)
  extends AbstractDomainDao[UserSeriesStatus](session, messagesApi, languages)(ec) {

  private val logger = Logger(this.getClass)

  override def count: Future[Option[Long]] = {
    sessionSelectCount(UserSeriesTable.getCountQueryString)
  }

  def getByUsers(userNames: Seq[String]) :Future[Seq[UserSeriesStatus]] = {
    sessionSelectAll(UserSeriesByUserTable.getByUsersQueryString(userNames))
  }

   def getByUserAndSeries(userName: String, seriesName: String) :Future[Option[UserSeriesStatus]] = {
     sessionSelectOne(UserSeriesByUserTable.getByUserSeriesQueryString(userName,seriesName))
   }

  def getBySeriesPercentage(seriesName: String, percentage: Double) :Future[Seq[UserSeriesStatus]] = {
    sessionSelectAll(UserSeriesBySeriesPercentageTable.getBySeriesPercentageQueryString(seriesName,percentage))
  }

  private def getUserSeriesByKey(key: String): Future[Option[UserSeriesStatus]] = {
    sessionSelectOne(UserSeriesByKeyTable.getByKeyQueryString(key))
  }
  override protected def convert(r: Row): UserSeriesStatus = {
    UserSeriesStatus(
      id(r),
      key(r),
      userName(r),
      seriesName(r),
      percentage(r)
    )
  }
}
