package com.amit.spoileralert.impl.entity

import com.amit.spoiler.UserSeriesStatus
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue, Json, Reads, Writes}

/**
 * This interface defines all the events that the SpoilerAlertAggregate supports.
 */
sealed trait SpoilerAlertEvent extends AggregateEvent[SpoilerAlertEvent] {
  override def aggregateTag: AggregateEventTag[SpoilerAlertEvent] =SpoilerAlertEvent.SpoilerAlertEventTag
}

object SpoilerAlertEvent {
  val SpoilerAlertEventTag = AggregateEventTag[SpoilerAlertEvent]
}

/**
 * An event that represents a change in SpoilerAlert entity.
 */
case class SpoilerAlertCreated(userSeriesStatus: UserSeriesStatus) extends SpoilerAlertEvent

object SpoilerAlertCreated {

  /**
   * Format for the greeting message changed event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[SpoilerAlertCreated] = Json.format
}

/**
 * An event that represents a change in SpoilerAlert entity.
 */
case class SpoilerAlertUpdated(userSeriesStatus: UserSeriesStatus) extends SpoilerAlertEvent

object SpoilerAlertUpdated {

  /**
   * Format for the greeting message changed event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[SpoilerAlertUpdated] = Json.format
}

/**
 * An event that represents a change in SpoilerAlert entity.
 */
case class SpoilerAlertDeleted(userSeriesStatus: UserSeriesStatus) extends SpoilerAlertEvent

object SpoilerAlertDeleted{
  /**
   * Format for the greeting message changed event.
   *
   * Events get stored and loaded from the database, hence a JSON format
   * needs to be declared so that they can be serialized and deserialized.
   */
  implicit val format: Format[SpoilerAlertDeleted] = Json.format
}


