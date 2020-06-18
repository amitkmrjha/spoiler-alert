package com.amit.spoileralertstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.amit.spoileralertstream.api.SpoileralertStreamService
import com.amit.spoileralert.api.SpoileralertService
import com.softwaremill.macwire._

class SpoileralertStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SpoileralertStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SpoileralertStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SpoileralertStreamService])
}

abstract class SpoileralertStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[SpoileralertStreamService](wire[SpoileralertStreamServiceImpl])

  // Bind the SpoileralertService client
  lazy val spoileralertService: SpoileralertService = serviceClient.implement[SpoileralertService]
}
