package controllers

import play.api._
import play.api.mvc._
import models.database.Country

object Application extends Controller {

  def index = Action {
    implicit request => Ok(views.html.index())
  }

  def presentation = Action {
    implicit request => Ok(views.html.presentation())
  }

  def create = Action {
    val result = Country.create()
    Logger.debug("result : "+result)
    Ok(views.html.index())
  }

  def getNodeOfFrance = Action {
    val result: List[(String, String)] = Country.getNodesOfFrance()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index())
  }

  def getAllNodes = Action {
    val result: List[(String, String, String)] = Country.getAllNodes()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index())
  }

  def test = Action {
    Ok(views.html.test())
  }

}