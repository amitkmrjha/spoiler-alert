package com.amit.spoiler

import play.api.libs.json.{Format, Json}

case class  SpoilerResponse(userName: String, seriesSpoiler: Seq[SeriesSpoiler])
case object SpoilerResponse {
  implicit val spoilerResponseFormat: Format[SpoilerResponse] = Json.format

}
case class SeriesSpoiler(seriesName: String, spoilers:Seq[String])
case object SeriesSpoiler {
  implicit val seriesSpoilerFormat: Format[SeriesSpoiler] = Json.format

}