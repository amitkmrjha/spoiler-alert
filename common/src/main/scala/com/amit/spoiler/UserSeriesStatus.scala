package com.amit.spoiler

import java.util.UUID
import julienrf.json.derived
import play.api.libs.json._

import play.api.libs.json.{Format, Json}

/*sealed trait UserSeriesStatus {

  def id : Option[UUID]

  def userName: String

  def seriesName: String

  def percentage: Double
}*/

/*object UserSeriesStatus {
  implicit val userSeriesStatusformat: Format[UserSeriesStatus] =derived.flat.oformat((__ \ "type").format[String])
}*/

/*case class Watched(id:Option[UUID], userName : String, seriesName: String, percentage: Double) extends UserSeriesStatus
object Watched {
  implicit val watchedFormat: Format[Watched] = Json.format[Watched]
}*/

case class UserSeriesStatus(id:Option[UUID],key: Option[String], userName : String, seriesName: String, percentage: Double)
object UserSeriesStatus {
  implicit val userSeriesStatusformat: Format[UserSeriesStatus] = Json.format
}