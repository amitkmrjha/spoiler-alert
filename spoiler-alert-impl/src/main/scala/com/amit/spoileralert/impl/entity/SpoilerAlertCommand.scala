package com.amit.spoileralert.impl.entity

import akka.actor.typed.ActorRef
import com.amit.spoiler.UserSeriesStatus
import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue, Json, Reads, Writes}

/**
 * This is a marker trait for commands.
 * We will serialize them using Akka's Jackson support that is able to deal with the replyTo field.
 * (see application.conf)
 */
trait SpoilerAlertCommandSerializable

/**
 * This interface defines all the commands that the SpoilerAlertAggregate supports.
 */
sealed trait SpoilerAlertCommand extends SpoilerAlertCommandSerializable

/**
 * A command to create SpoilerAlert entity.
 *
 * It has a reply type of [[Confirmation]], which is sent back to the caller
 * when all the events emitted by this command are successfully persisted.
 */
case class CreateSpoilerAlert(userSeriesStatus: UserSeriesStatus, replyTo: ActorRef[Confirmation]) extends SpoilerAlertCommand

/**
 * A command to update SpoilerAlert entity.
 *
 * It has a reply type of [[Confirmation]], which is sent back to the caller
 * when all the events emitted by this command are successfully persisted.
 */
case class UpdateSpoilerAlert(userSeriesStatus: UserSeriesStatus, replyTo: ActorRef[Confirmation]) extends SpoilerAlertCommand

/**
 * A command to retrieve SpoilerAlert entity.
 *
 * It has a reply type of [[Confirmation]], which is sent back to the caller
 * when all the events emitted by this command are successfully persisted.
 */
case class GetSpoilerAlert(replyTo: ActorRef[Confirmation]) extends SpoilerAlertCommand

/**
 * A command to delete SpoilerAlert entity.
 *
 * It has a reply type of [[Confirmation]], which is sent back to the caller
 * when all the events emitted by this command are successfully persisted.
 */
case class DeleteSpoilerAlert(userSeriesStatus: UserSeriesStatus,replyTo: ActorRef[Confirmation]) extends SpoilerAlertCommand



//Spoiler Alert replies
final case class Summary(userSeriesStatus: UserSeriesStatus)
object Summary {
  implicit val format: Format[Summary] = Json.format
}
sealed trait Confirmation

case object Confirmation {
  implicit val format: Format[Confirmation] = new Format[Confirmation] {
    override def reads(json: JsValue): JsResult[Confirmation] = {
      if ((json \ "reason").isDefined)
        Json.fromJson[Rejected](json)
      else
        Json.fromJson[Accepted](json)
    }

    override def writes(o: Confirmation): JsValue = {
      o match {
        case acc: Accepted => Json.toJson(acc)
        case rej: Rejected => Json.toJson(rej)
      }
    }
  }
}

final case class Accepted(summary: Summary) extends Confirmation
object Accepted{
  implicit val format: Format[Accepted] =Json.format
}

final case class Rejected(reason: String) extends Confirmation
object Rejected {
  implicit val format: Format[Rejected] = Json.format
}