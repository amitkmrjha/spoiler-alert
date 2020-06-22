package com.amit.spoileralert.api

import java.util.UUID

import akka.{Done, NotUsed}
import com.amit.spoiler.{SpoilerResponse, UserSeriesStatus}
import com.lightbend.lagom.scaladsl.api.Service.restCall
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


  def inputUserSeriesProgress: ServiceCall[UserSeriesStatus, UserSeriesStatus]

  def getUserSeriesProgress(key: String): ServiceCall[NotUsed, UserSeriesStatus]

  def getSameProgressUsers(username: String,seriesname: String): ServiceCall[NotUsed, Seq[String]]

  def getSpoilers: ServiceCall[Seq[String], Seq[SpoilerResponse]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("spoiler-alert")
      .withCalls(
        restCall(Method.POST,"/api/vi/userseries", inputUserSeriesProgress _),
        restCall(Method.GET, "/api/vi/userseries/:id", getUserSeriesProgress _),
        restCall(Method.GET, "/api/vi/userseries/match/:username/:seriesname", getSameProgressUsers _),
        restCall(Method.POST, "/api/vi/userseries/spoilers", getSpoilers _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}
