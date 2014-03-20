package controllers


import play.api._
import play.api.mvc._
import com.sun.syndication.feed.synd.{SyndEnclosureImpl, SyndEntry}
import java.util
import models.database.Country
import models.FluxRss
import models.database.{Country}
import models.Utilisateur


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def create = Action {
    val result = Country.create()
    Logger.debug("result : "+result)
    Ok(views.html.index("Your new application is ready."))
  }

  def createUser = Action {
    val result = Utilisateur.create(Utilisateur("mail1Change@test.com", "mdpTest1", "pseudoTest1"))
    Logger.debug("result test create user : "+result)
    Ok(views.html.index("Your new application is ready."))
  }

  def getUser = Action {
    val result = Utilisateur.get("mail1Change@test.com")
    Logger.debug("result test get user : " + result)
    Ok(views.html.index("Your new application is ready."))
  }


  def setMailUser = Action {
    val result = Utilisateur.setMail("mail1Change@test.com", "mail1Change@test.com")
    Logger.debug("result test set mail user : " + result)
    Ok(views.html.index("Your new application is ready."))
  }

  def setPseudoUser = Action {
    val result = Utilisateur.setPseudo("mail1Change@test.com", "pseudoTest1Change")
    Logger.debug("result test set pseudo user : " + result)
    Ok(views.html.index("Your new application is ready."))
  }

  def setMdpUser = Action {
    val result = Utilisateur.setMdp("mail1Change@test.com", "mdpTest1Change")
    Logger.debug("result test set mdp user : " + result)
    Ok(views.html.index("Your new application is ready."))
  }

  def incrementerNbCoeurs = Action {
    val result = Utilisateur.incrementerNbCoeurs("mail1Change@test.com")
    Logger.debug("result test incrémenter nb coeurs : " + result)
    Ok(views.html.index("Your new application is ready."))
  }

  def decrementerNbCoeurs = Action {
    val result = Utilisateur.decrementerNbCoeurs("mail1Change@test.com")
    Logger.debug("result test décrémenter nb coeurs : " + result)
    Ok(views.html.index("Your new application is ready."))
  }

  def deleteUser = Action {
    val result = Utilisateur.delete("mail1Change@test.com")
    Logger.debug("result test delete : " + result)
    Ok(views.html.index("Your new application is ready."))
  }

  def delete = Action {
    Country.delete()
    Ok(views.html.index("Your new application is ready."))
  }

  def getNodeOfFrance = Action {
    val result: List[(String, String)] = Country.getNodesOfFrance()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index("Your new application is ready."))
  }



  def getAllNodes = Action {
    val result: List[(String, String, Double)] = Country.getAllCountries()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index("Your new application is ready."))
  }


  def miseAJourFlux = Action {

    println("RES " + FluxRss.MisAJourTousSites())
    Ok(views.html.index("C'est READY QUOI"))

  }

  def insertSites= Action {
    FluxRss.insertAll
    Ok(views.html.index("C'est READY QUOI"))
  }

}