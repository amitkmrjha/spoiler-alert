package com.amit.spoileralert.impl.entity

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import com.amit.spoiler.UserSeriesStatus
import com.amit.spoileralert.impl.entity.SpoilerAlertBehavior.ReplyEffect


trait SpoilerAlertStateSerializable

sealed trait SpoilerAlertState extends SpoilerAlertStateSerializable{

  def applyCommand(cmd: SpoilerAlertCommand): ReplyEffect

  def applyEvent(event: SpoilerAlertEvent): SpoilerAlertState

}

case class ActiveSpoilerAlert(userSeriesStatus: UserSeriesStatus) extends SpoilerAlertState{

  override def applyCommand(cmd: SpoilerAlertCommand): ReplyEffect = cmd match {

    case CreateSpoilerAlert(uss, replyTo) =>
      Effect.reply(replyTo)(Rejected(s"UserSeriesStatus with id ${userSeriesStatus.id}" +
        s" user-name ${userSeriesStatus.userName}  series-name ${userSeriesStatus.seriesName} already exist."))

    case UpdateSpoilerAlert(uss, replyTo) =>
      if (canUpdate(uss.percentage))
        Effect.persist(SpoilerAlertUpdated(uss)).thenReply(replyTo)(_ => Accepted(Summary(uss)))
      else
        Effect.reply(replyTo)(Rejected(s"Invalid watch percentage %${uss.percentage}%. " +
          s"user ${userSeriesStatus.seriesName} has already watched %${userSeriesStatus.percentage}% of" +
          s" series ${userSeriesStatus.seriesName}"))

    case GetSpoilerAlert(replyTo) =>
      Effect.reply(replyTo)(Accepted(Summary(userSeriesStatus)))

    case DeleteSpoilerAlert(uss,replyTo) =>
        Effect.persist(SpoilerAlertDeleted(uss)).thenReply(replyTo)(m => Accepted(Summary(uss)))

  }

  override def applyEvent(event: SpoilerAlertEvent): SpoilerAlertState = event match {

  case SpoilerAlertCreated(uss) => throw new IllegalStateException(s"unexpected event [$event] in state [ActiveSpoilerAlert]")
  case SpoilerAlertUpdated(uss) => ActiveSpoilerAlert(uss)
  case SpoilerAlertDeleted(uss)     => ClosedSpoilerAlert

  }

  private def canUpdate(percentage: Double): Boolean = {
    percentage - userSeriesStatus.percentage  > 0
  }
}

case object ClosedSpoilerAlert extends SpoilerAlertState {

  override def applyCommand(cmd: SpoilerAlertCommand): ReplyEffect = cmd match {
    case CreateSpoilerAlert(uss, replyTo) => replyClosed(replyTo)

    case UpdateSpoilerAlert(uss, replyTo) => replyClosed(replyTo)

    case GetSpoilerAlert(replyTo) => replyClosed(replyTo)

    case DeleteSpoilerAlert(uss,replyTo) => replyClosed(replyTo)
  }

  private def replyClosed(replyTo: ActorRef[Confirmation]): ReplyEffect =
    Effect.reply(replyTo)(Rejected(s"SpoilerAlert state is closed"))

  override def applyEvent(event: SpoilerAlertEvent): SpoilerAlertState = {
    throw new IllegalStateException(s"unexpected event [$event] in state [ClosedSpoilerAlert]")
  }
}





