package com.amit.spoileralert.impl

import com.amit.spoiler.UserSeriesStatus
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.amit.spoileralert.api._
import com.datastax.driver.core.utils.UUIDs

class SpoilerAlertServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new SpoilerAlertApplication(ctx) with LocalServiceLocator
  }

  val client: SpoilerAlertService = server.serviceClient.implement[SpoilerAlertService]

  override protected def afterAll(): Unit = server.stop()

  "spoiler-alert service" should {

    "say hello" in {
      client.inputUserSeriesProgress.invoke(UserSeriesStatus(Option(UUIDs.timeBased), Option("bak"),"hello", "ozark",10)).map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        answer <- client.getSpoilers.invoke(Seq("Amit","Amit1"))
      } yield {
        answer should === ("Hello, Alice!")
      }
    }
  }
}
