package com.amit.spoileralert.api

import java.util.UUID

import akka.{Done, NotUsed}
import com.amit.spoiler.UserSeriesStatus
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object SpoilerAlertService  {
  val TOPIC_NAME = "greetings"
}

/**
  * The spoiler-alert service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the SpoilerAlertService.
  */
trait SpoilerAlertService extends Service {

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"userName:":
    * "abc",seriesName = "GOT", percentage = 10}' http://localhost:9000/api/vi/userseries
    */
  def inputUserSeriesProgress: ServiceCall[UserSeriesStatus, UserSeriesStatus]

  def getUserSeriesProgress(key: String): ServiceCall[NotUsed, UserSeriesStatus]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("spoiler-alert")
      .withCalls(
        restCall(Method.POST, "/api/vi/userseries", inputUserSeriesProgress _),
        restCall(Method.GET, "/api/vi/userseries/:id", getUserSeriesProgress _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}
