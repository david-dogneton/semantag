package controllers

import play.api.libs.ws.WS
import play.api.libs.json._
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import play.api.libs.functional.syntax._
import models.ResourceDbPedia

/**
 * Created by Administrator on 27/03/2014.
 */
object AnnotatorWS {

  private final val production: String = "http://spotlight.dbpedia.org/rest/annotate"
  private final val fr: String = "http://localhost:2222/rest/annotate"
  //private final val fr:String ="http://spotlight.sztaki.hu:2225/rest/annotate"

  def annotate(text: String): Future[List[ResourceDbPedia]] = {

    val result = WS.url(fr)
      .withHeaders(("Accept", "application/json"), ("Accept", "application/xml"), ("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"))
      .post("&text=" + text+"&support=-1")

    result.map(response => {
      if (response.status == 200) {
        implicit val resourceReader: Reads[(String, String, String, String, String, String, String)] = (
          (JsPath \ "@URI").read[String] and
            (JsPath \ "@support").read[String] and
            (JsPath \ "@types").read[String] and
            (JsPath \ "@surfaceForm").read[String] and
            (JsPath \ "@offset").read[String] and
            (JsPath \ "@similarityScore").read[String] and
            (JsPath \ "@percentageOfSecondRank").read[String]
          ).tupled

        val resources = (response.json \ "Resources").as[List[(String, String, String, String, String, String, String)]]
        resources.map(resource => ResourceDbPedia(resource._1, resource._2.toInt, resource._3, resource._4, resource._5.toInt, resource._6.toDouble, resource._7.toDouble))
      } else {
        Nil
      }
    })
  }
}
