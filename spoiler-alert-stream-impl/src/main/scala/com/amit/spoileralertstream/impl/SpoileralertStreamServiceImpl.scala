package com.amit.spoileralertstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.amit.spoileralertstream.api.SpoileralertStreamService
import com.amit.spoileralert.api.SpoileralertService

import scala.concurrent.Future

/**
  * Implementation of the SpoileralertStreamService.
  */
class SpoileralertStreamServiceImpl(spoileralertService: SpoileralertService) extends SpoileralertStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(spoileralertService.hello(_).invoke()))
  }
}
