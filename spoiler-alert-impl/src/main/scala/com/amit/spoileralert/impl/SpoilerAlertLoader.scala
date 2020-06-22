package com.amit.spoileralert.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.amit.spoileralert.api.SpoilerAlertService
import com.amit.spoileralert.impl.daos.{ UserSeriesDao}
import com.amit.spoileralert.impl.entity.{SpoilerAlertBehavior, SpoilerAlertSerializerRegistry, SpoilerAlertState}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._

class SpoilerAlertLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SpoilerAlertApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SpoilerAlertApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SpoilerAlertService])
}

abstract class SpoilerAlertApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[SpoilerAlertService](wire[SpoilerAlertServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = SpoilerAlertSerializerRegistry

  readSide.register(wire[SpoilerAlertEventProcessor])

  lazy val userSeriesDao: UserSeriesDao = wire[UserSeriesDao]


  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init( Entity(SpoilerAlertBehavior.typeKey)(
      entityContext => SpoilerAlertBehavior.create(entityContext)
    )
  )

}
