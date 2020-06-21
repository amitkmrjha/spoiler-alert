package com.amit.spoileralert.impl.daos

import java.util

import akka.Done
import com.amit.spoiler.{Columns, UserSeriesStatus}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.datastax.driver.core.querybuilder.{Clause, Delete, Insert, QueryBuilder}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}

trait ReadUserSeriesTable extends ReadSideDomainTable[UserSeriesStatus] {

  override protected def tableScript: String =
    s"""
        CREATE TABLE IF NOT EXISTS $tableName (
          ${Columns.Id} timeuuid,
          ${Columns.Key} text,
          ${Columns.UserName} text,
          ${Columns.SeriesName} text,
          ${Columns.Percentage} double,
          PRIMARY KEY (${primaryKey})
        )
      """.stripMargin

  override protected def fields: Seq[String]  = Seq(
    Columns.Id,
    Columns.Key,
    Columns.UserName,
    Columns.SeriesName,
    Columns.Percentage
  )

  override protected def cL: util.List[String] = fields.toList.asJava

  override protected def vL: util.List[AnyRef] = fields.map(_ =>
    QueryBuilder.bindMarker().asInstanceOf[AnyRef]).toList.asJava

  override protected def prepareInsert: Insert  = QueryBuilder.insertInto(tableName).values(cL, vL)

  override protected def getInsertBindValues(entity: UserSeriesStatus): Seq[AnyRef] = {

    val bindValues: Seq[AnyRef] = fields.map(x => x match {
      case Columns.Id => id(entity)
      case Columns.Key => key(entity)
      case Columns.UserName => entity.userName
      case Columns.SeriesName => entity.seriesName
      case Columns.Percentage => entity.percentage.asInstanceOf[AnyRef]

    })
    bindValues
  }

  override val getAllQueryString: String =  {
    val select = QueryBuilder.select().from(tableName)
    select.toString
  }

  override val getCountQueryString: String = {
    val countAllQuery = QueryBuilder.select().countAll().from(tableName)
    countAllQuery.toString
  }
}

object UserSeriesTable extends ReadUserSeriesTable {

  override protected def tableName: String  = ColumnFamilies.SpoilerAlerts

  override protected def primaryKey: String = s"${Columns.Id}"

  override protected def prepareDelete: Delete.Where  = QueryBuilder.delete().from(tableName)
    .where(QueryBuilder.eq(Columns.Id, QueryBuilder.bindMarker()))

  override protected def getDeleteBindValues(entity: UserSeriesStatus): Seq[AnyRef]  = {
    val bindValues: Seq[AnyRef] = Seq(
      entity.id.getOrElse(UUIDs.timeBased())
    )
    bindValues
  }
}

object UserSeriesByKeyTable extends ReadUserSeriesTable {

  override protected def tableName: String  = ColumnFamilies.SpoilerAlertsByKey

  override protected def primaryKey: String = s"${Columns.Key}"

  override protected def prepareDelete: Delete.Where  = QueryBuilder.delete().from(tableName)
    .where(QueryBuilder.eq(Columns.Key, QueryBuilder.bindMarker()))

  override protected def getDeleteBindValues(entity: UserSeriesStatus): Seq[AnyRef]  = {
    val bindValues: Seq[AnyRef] = Seq(
      entity.key
    )
    bindValues
  }

  def getByKeyQueryString(key:String): String = {
    val select = QueryBuilder.select().from(tableName)
      .where(QueryBuilder.eq(Columns.Key, key))
    select.toString
  }
}

object UserSeriesByUserTable extends ReadUserSeriesTable {

  override protected def tableName: String  = ColumnFamilies.SpoilerAlertByUser

  override protected def primaryKey: String = s"${Columns.UserName},${Columns.SeriesName}"

  override protected def prepareDelete: Delete.Where  = QueryBuilder.delete().from(tableName)
    .where(QueryBuilder.eq(Columns.UserName, QueryBuilder.bindMarker()))
    .and(QueryBuilder.eq(Columns.SeriesName, QueryBuilder.bindMarker()))

  override protected def getDeleteBindValues(entity: UserSeriesStatus): Seq[AnyRef]  = {
    val bindValues: Seq[AnyRef] = Seq(
      entity.userName,
      entity.seriesName,
    )
    bindValues
  }

  def getByUserQueryString(userName:String): String = {
    val select = QueryBuilder.select().from(tableName)
      .where(QueryBuilder.eq(Columns.UserName, userName))
    select.toString
  }

  def getByUserSeriesQueryString(userName:String,seriesName: String): String = {
    val select = QueryBuilder.select().from(tableName)
      .where(QueryBuilder.eq(Columns.UserName, userName))
        .and(QueryBuilder.eq(Columns.SeriesName, seriesName))
    select.toString
  }
}