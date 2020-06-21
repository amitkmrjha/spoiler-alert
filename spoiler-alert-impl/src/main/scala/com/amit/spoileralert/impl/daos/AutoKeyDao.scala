package com.amit.spoileralert.impl.daos

import com.amit.spoiler.Columns
import com.amit.spoileralert.impl.{AutoKeyModel, UserSeriesAutoKey}
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait AutoKeyDao {
  def getNextKey(model: AutoKeyModel): Future[Option[UserSeriesAutoKey]]
}

class AutoKeyDaoImpl (session: CassandraSession)(implicit ec: ExecutionContext) extends
  AutoKeyDao {

  def getTableName: String = ColumnFamilies.AutoKey

   def getInsertMap(entity: UserSeriesAutoKey) : Map[String,AnyRef]= {
    Map(Columns.ModelContext -> entity.model.context,
      Columns.ModelName -> entity.model.name,
      Columns.KeyNumber -> entity.keyNumber.toString)
  }

   def update(entity: UserSeriesAutoKey): Future[Option[UserSeriesAutoKey]] = {
    val update = QueryBuilder.update(getTableName).`with`(
      QueryBuilder.incr(Columns.KeyNumber,1)).where(
      QueryBuilder.eq(Columns.ModelContext ,entity.model.context))
      .and(QueryBuilder.eq(Columns.ModelName , entity.model.name))
     session.executeWrite(update.toString).flatMap(_=>
       getByModel(entity.model)
     )
  }

   protected def getUpdateTuple(entity: UserSeriesAutoKey):
  (mutable.LinkedHashMap[String, AnyRef], mutable.LinkedHashMap[String, AnyRef]) = {

    val withAssgn: mutable.LinkedHashMap[String, AnyRef] = mutable.LinkedHashMap()

    val whereFilter:mutable.LinkedHashMap[String, AnyRef] =mutable.LinkedHashMap(
      Columns.ModelContext ->entity.model.context,
      Columns.ModelName -> entity.model.name)
    (withAssgn,whereFilter)
  }

  def delete(entity: UserSeriesAutoKey): Future[Option[UserSeriesAutoKey]] = {
    require(entity != null, "entity cannot be null")
    require(entity.id != null, "id cannot be null or empty")
    val delWhere = QueryBuilder.delete().from(getTableName)
      .where(QueryBuilder.eq(Columns.ModelContext,entity.model.context))
      .and(QueryBuilder.eq(Columns.ModelName,entity.model.name))
     session.executeWrite(delWhere.toString)
    Future(Option(entity))
  }

   def getByNameQueryString(model: AutoKeyModel): String = {
    QueryBuilder.select().from(getTableName)
      .where(QueryBuilder.eq(Columns.ModelContext, model.context))
      .and(QueryBuilder.eq(Columns.ModelName, model.name))
      .toString
  }

  private def getByModel(model: AutoKeyModel): Future[Option[UserSeriesAutoKey]] =  {
    assert(model != null, "Auto Key Model cannot be empty")
    val query = getFindByModelQueryString(model)
    session.selectOne(query).map {
      case Some(row) => Some((convert(row)))
      case None => null
    }
  }

  private def getFindByModelQueryString(model: AutoKeyModel): String = {
    QueryBuilder.select().from(getTableName)
      .where(QueryBuilder.eq(Columns.ModelContext, model.context))
      .and(QueryBuilder.eq(Columns.ModelName, model.name))
      .toString
  }

  override def getNextKey(model: AutoKeyModel): Future[Option[UserSeriesAutoKey]] = {
    assert(model != null, "Auto Key Model cannot be empty")
    val tmpAutoKey = UserSeriesAutoKey(model = model,keyNumber = "")
    update(tmpAutoKey)
    /*val query = getByNameQueryString(model)
    getSingleByQuery(query)*/
  }

   def convert(r: Row): UserSeriesAutoKey = {
    val id = UUIDs.timeBased()
    val mdlContext = r.getString(Columns.ModelContext)
    val mdlName = r.getString(Columns.ModelName)
    val kyNumber = r.getObject(Columns.KeyNumber)
     UserSeriesAutoKey(model = AutoKeyModel(mdlContext,mdlName), keyNumber = kyNumber.toString)
  }
}
