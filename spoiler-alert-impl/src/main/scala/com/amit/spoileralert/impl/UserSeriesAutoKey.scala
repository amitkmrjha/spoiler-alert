package com.amit.spoileralert.impl

import java.util.UUID

import com.amit.spoiler.UserSeriesStatus
import play.api.libs.json.{Format, Json}

case class UserSeriesAutoKey(id: UUID = UUID.randomUUID(), model: AutoKeyModel, keyNumber: String)

trait AutoKeyModel {
  def name: String
  def context: String
}

object AutoKeyModel {
  def apply(context: String, name: String): AutoKeyModel = Class.forName(name) match {
    case x if x == classOf[UserSeriesStatus] => KeyUserSeries()
    case _ => KeyUnknown(context, name)
  }

  def unapply(akm: AutoKeyModel): Option[(String, String)] = Some(akm.context, akm.name)

  implicit val autoKeyModelFormat: Format[AutoKeyModel] = Json.format[AutoKeyModel]
}

case class KeyUserSeries() extends AutoKeyModel{
  override def name = UserSeriesStatus.getClass.getName
  override def context = "-"
}

case class KeyUnknown(context: String, name: String) extends AutoKeyModel
