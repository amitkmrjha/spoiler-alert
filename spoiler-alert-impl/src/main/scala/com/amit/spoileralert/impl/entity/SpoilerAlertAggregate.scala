package com.amit.spoileralert.impl.entity

import java.time.LocalDateTime

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import com.amit.spoiler.UserSeriesStatus
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AkkaTaggerAdapter}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.Logger
import play.api.libs.json.{Format, Json, _}

import scala.collection.immutable.Seq

/**
  * This provides an event sourced behavior. It has a state, [[SpoilerAlertState]], which
  * stores what the greeting should be (eg, "Hello").
  *
  * Event sourced entities are interacted with by sending them commands. This
  * aggregate supports two commands, a [[CreateSpoilerAlert]] command, which is
  * used to change the greeting, and a [[UpdateSpoilerAlert]] command, which is a read
  * only command which returns a greeting to the name specified by the command.
  *
  * Commands get translated to events, and it's the events that get persisted.
  * Each event will have an event handler registered for it, and an
  * event handler simply applies an event to the current state. This will be done
  * when the event is first created, and it will also be done when the aggregate is
  * loaded from the database - each event will be replayed to recreate the state
  * of the aggregate.
  *
  * This aggregate defines one event, the [[SpoilerAlertCreated]] event,
  * which is emitted when a [[CreateSpoilerAlert]] command is received.
  */
object SpoilerAlertBehavior {
  val logger = Logger(this.getClass)

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[SpoilerAlertEvent, Option[SpoilerAlertState]]

  val typeKey: EntityTypeKey[SpoilerAlertCommand] =   EntityTypeKey[SpoilerAlertCommand]("SpoilerAlert")

  /**
    * Given a sharding [[EntityContext]] this function produces an Akka [[Behavior]] for the aggregate.
    */
  def create(entityContext: EntityContext[SpoilerAlertCommand]): Behavior[SpoilerAlertCommand] = {
    val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)
    logger.debug(s"I am here in create behaviour")
    create(persistenceId)
      .withTagger(
        // Using Akka Persistence Typed in Lagom requires tagging your events
        // in Lagom-compatible way so Lagom ReadSideProcessors and TopicProducers
        // can locate and follow the event streams.
        AkkaTaggerAdapter.fromLagom(entityContext, SpoilerAlertEvent.Tag)
      )

  }
  /*
   * This method is extracted to write unit tests that are completely independendant to Akka Cluster.
   */
  private[impl] def create(persistenceId: PersistenceId) = EventSourcedBehavior
      .withEnforcedReplies[SpoilerAlertCommand, SpoilerAlertEvent, Option[SpoilerAlertState]](
        persistenceId = persistenceId,
        emptyState = None,
        commandHandler = (state, cmd) => state match {
          case None          => onFirstCommand(cmd)
          case Some(sas) => sas.applyCommand(cmd)
        },
        eventHandler = (state, evt) => state match {
          case None          => Some(onFirstEvent(evt))
          case Some(account) => Some(account.applyEvent(evt))
        }
      )

  def onFirstCommand(cmd: SpoilerAlertCommand): ReplyEffect = {
    cmd match {
      case CreateSpoilerAlert(uss,replyTo) =>
        Effect.persist(SpoilerAlertCreated(uss)).thenReply(replyTo)(_ => Accepted(Summary(uss)))
      case UpdateSpoilerAlert(uss, replyTo) => replyNotStarted(replyTo)
      case GetSpoilerAlert(replyTo) => replyNotStarted(replyTo)
      case DeleteSpoilerAlert(uss,replyTo) => replyNotStarted(replyTo)
      case _ =>{
        Effect.unhandled.thenNoReply()
      }

    }
  }
  private def replyNotStarted(replyTo: ActorRef[Confirmation]): ReplyEffect =
    Effect.reply(replyTo)(Rejected(s"SpoilerAlert state is not started"))


  def onFirstEvent(event: SpoilerAlertEvent): SpoilerAlertState = {
    event match {
      case SpoilerAlertCreated(uss) => ActiveSpoilerAlert(uss)
      case _              => throw new IllegalStateException(s"unexpected event [$event] in state [EmptyAccount]")
    }
  }

}






