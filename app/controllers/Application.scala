package controllers


import play.api._
import play.api.mvc._
import com.sun.syndication.feed.synd.{SyndEnclosureImpl, SyndEntry}
import java.util
import models.database.{Country}
import models.{AppreciationEntite, Entite, Utilisateur}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def create = Action {
    val result = Country.create()
    Logger.debug("result : "+result)
    Ok(views.html.index("Your new application is ready."))
  }

  def createEntite = Action {
    val result = Entite.create(Entite("Robin Van Persie", "http://quartsDeFinale.com"))
    Logger.debug("result test create entité : "+result)
    Ok(views.html.index("Your new application is ready."))
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
    Ok(views.html.index("Your new application is ready."))
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
    Ok(views.html.index("Your new application is ready."))
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
    Ok(views.html.index("Your new application is ready."))
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
    Ok(views.html.index("Your new application is ready."))
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
    Ok(views.html.index("Your new application is ready."))
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
    Ok(views.html.index("Your new application is ready."))
  }


  def createUser = Action {
    val result = Utilisateur.create(Utilisateur("mail2@test.com", "mdpTest2", "pseudoTest2"))
    Logger.debug("result test create user : "+result)
    Ok(views.html.index("Your new application is ready."))
  }

  def getUser = Action {
    val resultOpt = Utilisateur.get("mail1Change@test.com")
    resultOpt match {
      case Some(result) =>
        Logger.debug("result test get user : " + result)
      case None => println("Utilisateur not found.")
    }
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

  def lectureFlux(url :String) = Action{
    import java.net.URL
    import com.sun.syndication.io.{XmlReader, SyndFeedInput}
    import com.sun.syndication.feed.synd.SyndFeed

    var ok : Boolean = false

    try {
      //val feedUrl : URL = new URL("http://rss.lemonde.fr/c/205/f/3050/index.rss")
      val feedUrl : URL = new URL(url)

      val input : SyndFeedInput = new SyndFeedInput()
      val feed : SyndFeed = input.build(new XmlReader(feedUrl))
      println(feed.getTitle)
      println(feed.getDescription)

      ok = true
      val listeFlux: util.List[_] =  feed.getEntries
      val listeFluxCasted : util.List[SyndEntry] = listeFlux.asInstanceOf[util.List[SyndEntry]]

      //Manip avec un iterator pour récuperer une liste "Scala" plus facile pour la manip
      var listeFluxScala : List [SyndEntry] = List()
      val ite =listeFluxCasted.iterator()
      while(ite.hasNext){
        val tmp =ite.next()
        listeFluxScala=listeFluxScala.::(tmp)
      }

      listeFluxScala.foreach(
        art => {
          println( "Titre : " +art.getTitle)
          println("Auteur : " +art.getAuthor)
          println("Date : " +art.getPublishedDate)
          println("Description : " +art.getDescription.getValue)
          println("Lien : " +art.getLink)
          //Liste images  => on la "caste" pour récupérer le bon type (SyndEnclosureImpl) pour pouvoir récuperer l'url des images
          val image : util.List[SyndEnclosureImpl] = art.getEnclosures.asInstanceOf[util.List[SyndEnclosureImpl]]
          // On récupère la première image de la liste et son URL
          if(image.size != 0){
            for(i <- 0 to image.size -1)
              println("image : " +image.get(i).getUrl)

          }
        }
      )

    }
    catch {
      case ex : Exception =>
        ex.printStackTrace()
        println("ERROR: "+ex.getMessage)
    }

    if (!ok) {
      println("FeedReader reads and prints any RSS/Atom feed type.")
      println("The first parameter must be the URL of the feed to read.")
    }

    Ok(views.html.index("Your new application is ready."))
  }


}