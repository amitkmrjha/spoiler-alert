package com.amit.spoileralert.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.amit.spoileralert.api.SpoileralertService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._

class SpoileralertLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SpoileralertApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SpoileralertApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SpoileralertService])
}

abstract class SpoileralertApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[SpoileralertService](wire[SpoileralertServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = SpoileralertSerializerRegistry

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(SpoileralertState.typeKey)(
      entityContext => SpoileralertBehavior.create(entityContext)
    )
  )

}
