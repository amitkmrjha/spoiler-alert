package com.amit.spoileralert.impl

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import com.amit.spoiler.UserSeriesStatus
import com.amit.spoileralert.impl.entity.{Accepted, Confirmation, CreateSpoilerAlert, GetSpoilerAlert, SpoilerAlertBehavior, SpoilerAlertCommand, Summary, UpdateSpoilerAlert}
import com.datastax.driver.core.utils.UUIDs
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

class SpoileralertAggregateSpec extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with WordSpecLike with Matchers {

  "spoiler-alert aggregate" should {

    "say create spoiler-alert by default" in {
      val probe = createTestProbe[Confirmation]()
      val ref = spawn(SpoilerAlertBehavior.create(PersistenceId("fake-type-hint", "fake-id")))
      val p: UserSeriesStatus = UserSeriesStatus(Option(UUIDs.timeBased), Option("bak"),"hello", "ozark",10)

      ref ! CreateSpoilerAlert(p, probe.ref)
      probe.expectMessage(Accepted(Summary(p)))
    }

    "allow updating the spoiler-alert" in  {
      val ref = spawn(SpoilerAlertBehavior.create(PersistenceId("fake-type-hint", "fake-id")))

      val probe1 = createTestProbe[Confirmation]()
      val p: UserSeriesStatus = UserSeriesStatus(Option(UUIDs.timeBased), Option("bak"),"hello", "ozark",20)
      ref ! UpdateSpoilerAlert(p, probe1.ref)
      probe1.expectMessage(Accepted(Summary(p)))

      val probe2 = createTestProbe[Confirmation]()
      ref ! GetSpoilerAlert(probe2.ref)
      probe2.expectMessage(Accepted(Summary(p)))
    }

  }
}
