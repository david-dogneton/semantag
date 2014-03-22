package controllers


import play.api._
import play.api.mvc._
import com.sun.syndication.feed.synd.{SyndEnclosureImpl, SyndEntry}
import java.util
import models.database.Country
import models.FluxRss
import models.database.{Country}
import models.{AppreciationEntite, Entite, Utilisateur}


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

  def createEntite = Action {
    val result = Entite.create(Entite("Robin Van Persie", "http://quartsDeFinale.com"))
    Logger.debug("result test create entité : "+result)
    Ok(views.html.index())
  }

  def createAppreciationEntite = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val entiteOpt = Entite.get("http://quartsDeFinale.com")
    utilisateurOpt match {
      case Some(utilisateur) =>
        entiteOpt match {
          case Some(entite) =>
            val result = AppreciationEntite.create(AppreciationEntite(utilisateur, entite, 4, 3))
            Logger.debug("result test create appréciation entité : "+result)
          case None => println("entiteOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def getAppreciationEntite = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val entiteOpt = Entite.get("http://quartsDeFinale.com")
    utilisateurOpt match {
      case Some(utilisateur) =>
        entiteOpt match {
          case Some(entite) =>
            val result = AppreciationEntite.get(utilisateur, entite)
            Logger.debug("result test create appréciation entité : "+result)
          case None => println("entiteOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def setQuantiteAppreciationEntite = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val entiteOpt = Entite.get("http://quartsDeFinale.com")
    utilisateurOpt match {
      case Some(utilisateur) =>
        entiteOpt match {
          case Some(entite) =>
            val result = AppreciationEntite.setQuantite(utilisateur, entite, -3)
            Logger.debug("result test set quantité appréciation entité : "+result)
          case None => println("entiteOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def setNbCoeursAppreciationEntite = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val entiteOpt = Entite.get("http://quartsDeFinale.com")
    utilisateurOpt match {
      case Some(utilisateur) =>
        entiteOpt match {
          case Some(entite) =>
            val result = AppreciationEntite.incrNbCoeurs(utilisateur, entite)
            Logger.debug("result test set quantité appréciation entité : "+result)
          case None => println("entiteOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def setFavoriAppreciationEntite = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val entiteOpt = Entite.get("http://quartsDeFinale.com")
    utilisateurOpt match {
      case Some(utilisateur) =>
        entiteOpt match {
          case Some(entite) =>
            val result = AppreciationEntite.setFavori(utilisateur, entite)
            Logger.debug("result test set quantité appréciation entité : "+result)
          case None => println("entiteOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def estFavoriAppreciationEntite = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val entiteOpt = Entite.get("http://quartsDeFinale.com")
    utilisateurOpt match {
      case Some(utilisateur) =>
        entiteOpt match {
          case Some(entite) =>
            val result = AppreciationEntite.estFavori(utilisateur, entite)
            Logger.debug("result test set quantité appréciation entité : "+result)
          case None => println("entiteOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }


  def createUser = Action {
    val result = Utilisateur.create(Utilisateur("mail2@test.com", "mdpTest2", "pseudoTest2"))
    Logger.debug("result test create user : "+result)
    Ok(views.html.index())
  }

  def getUser = Action {
    val resultOpt = Utilisateur.get("mail1Change@test.com")
    resultOpt match {
      case Some(result) =>
        Logger.debug("result test get user : " + result)
      case None => println("Utilisateur not found.")
    }
    Ok(views.html.index())
  }


  def setMailUser = Action {
    val result = Utilisateur.setMail("mail1Change@test.com", "mail1Change@test.com")
    Logger.debug("result test set mail user : " + result)
    Ok(views.html.index())
  }

  def setPseudoUser = Action {
    val result = Utilisateur.setPseudo("mail1Change@test.com", "pseudoTest1Change")
    Logger.debug("result test set pseudo user : " + result)
    Ok(views.html.index())
  }

  def setMdpUser = Action {
    val result = Utilisateur.setMdp("mail1Change@test.com", "mdpTest1Change")
    Logger.debug("result test set mdp user : " + result)
    Ok(views.html.index())
  }

  def incrementerNbCoeurs = Action {
    val result = Utilisateur.incrementerNbCoeurs("mail1Change@test.com")
    Logger.debug("result test incrémenter nb coeurs : " + result)
    Ok(views.html.index())
  }

  def decrementerNbCoeurs = Action {
    val result = Utilisateur.decrementerNbCoeurs("mail1Change@test.com")
    Logger.debug("result test décrémenter nb coeurs : " + result)
    Ok(views.html.index())
  }

  def deleteUser = Action {
    val result = Utilisateur.delete("mail1Change@test.com")
    Logger.debug("result test delete : " + result)
    Ok(views.html.index())
  }

  def delete = Action {
    Country.delete()
    Ok(views.html.index())
  }

  def getNodeOfFrance = Action {
    val result: List[(String, String)] = Country.getNodesOfFrance()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index())
  }



  def getAllNodes = Action {
    val result: List[(String, String, Double)] = Country.getAllCountries()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index())
  }

  def test = Action {
    Ok(views.html.test())
  }


  def miseAJourFlux = Action {
    FluxRss.misAJourTousSites()
    Ok(views.html.index())

  }

  def miseAJourSites= Action {
    FluxRss.miseAJourBddSites
    Ok(views.html.index())
  }

}