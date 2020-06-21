package com.amit.spoileralert.impl.entity

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

/**
 * Akka serialization, used by both persistence and remoting, needs to have
 * serializers registered for every type serialized or deserialized. While it's
 * possible to use any serializer you want for Akka messages, out of the box
 * Lagom provides support for JSON, via this registry abstraction.
 *
 * The serializers are registered here, and then provided to Lagom in the
 * application loader.
 */
object SpoilerAlertSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)

    JsonSerializer[SpoilerAlertCreated],
    JsonSerializer[SpoilerAlertUpdated],
    JsonSerializer[SpoilerAlertDeleted],
    // the replies use play-json as well
    JsonSerializer[Summary],
    JsonSerializer[Confirmation],
    JsonSerializer[Accepted],
    JsonSerializer[Rejected]
  )
}
