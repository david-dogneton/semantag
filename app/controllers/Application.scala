package controllers


import play.api._
import play.api.mvc._
import com.sun.syndication.feed.synd.{SyndEnclosureImpl, SyndEntry}
import java.util
import models.database.Country
import models._
import models.database.{Country}
import scala.Some
import org.joda.time.DateTime
import jp.t2v.lab.play2.auth.AuthenticationElement
import scala.Some


object Application extends Controller  with AuthenticationElement with AuthConfigImpl {

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
            Logger.debug("result test set quantité appréciation entité : " + result)
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

  def createSite = Action {
    val result = Site.create(Site("www.magness.fr", "Magness", "Informations diverses"))
    Logger.debug("result test create site : " + result)
    Ok(views.html.index())
  }

  def createNote = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val articleOpt = Article.getArticle("http://magness.fr/blablabla")
    utilisateurOpt match {
      case Some(utilisateur) =>
        articleOpt match {
          case Some(article) =>
            val result = Note.create(Note(utilisateur, article, 4))
            Logger.debug("result test créer note : " + result)
          case None => println("articleOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def createConsultation = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val articleOpt = Article.getArticle("http://magness.fr/blablabla")
    utilisateurOpt match {
      case Some(utilisateur) =>
        articleOpt match {
          case Some(article) =>
            val result = Consultation.create(Consultation(utilisateur, article, DateTime.now()))
            Logger.debug("result test créer consultation : " + result)
          case None => println("articleOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def createRecommandation = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val articleOpt = Article.getArticle("http://magness.fr/blablabla")
    utilisateurOpt match {
      case Some(utilisateur) =>
        articleOpt match {
          case Some(article) =>
            val result = Recommandation.create(Recommandation(utilisateur, article, 4.3))
            Logger.debug("result test créer recommandation : " + result)
          case None => println("articleOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def getNote = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val articleOpt = Article.getArticle("http://magness.fr/blablabla")
    utilisateurOpt match {
      case Some(utilisateur) =>
        articleOpt match {
          case Some(article) =>
            val result = Note.get(utilisateur, article)
            Logger.debug("result test get note : " + result)
          case None => println("articleOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def getConsultation = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val articleOpt = Article.getArticle("http://magness.fr/blablabla")
    utilisateurOpt match {
      case Some(utilisateur) =>
        articleOpt match {
          case Some(article) =>
            val result = Consultation.get(utilisateur, article)
            Logger.debug("result test get consultation : " + result)
          case None => println("articleOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def getRecommandation = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val articleOpt = Article.getArticle("http://magness.fr/blablabla")
    utilisateurOpt match {
      case Some(utilisateur) =>
        articleOpt match {
          case Some(article) =>
            val result = Recommandation.get(utilisateur, article)
            Logger.debug("result test get recommandation : " + result)
          case None => println("articleOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def createArticle = Action {
    val siteOpt = Site.get("www.magness.fr")
    siteOpt match {
      case Some(site) =>
        val result = Article.create(Article("Monuments Men : jusqu'au bout de l'ennui.", "Thib", "Ceci est la description de la critique de Monuments Men.", new DateTime(), "http://magness.fr/blablabla", site))
        Logger.debug("result test create article : " + result)
      case None => println("www.magness.fr not found.")
    }

    Ok(views.html.index())
  }


  //début tests AppreciationDomaine
  def createAppreciationDomaine = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val domaineOpt = Domaine.get("Sport")
    utilisateurOpt match {
      case Some(utilisateur) =>
        domaineOpt match {
          case Some(domaine) =>
            val result = AppreciationDomaine.create(AppreciationDomaine(utilisateur, domaine, 4))
            Logger.debug("result test create appréciation domaine : " + result)
          case None => println("domaineOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def getAppreciationDomaine = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val domaineOpt = Domaine.get("Sport")
    utilisateurOpt match {
      case Some(utilisateur) =>
        domaineOpt match {
          case Some(domaine) =>
            val result = AppreciationDomaine.get(utilisateur, domaine)
            Logger.debug("result test create appréciation domaine : " + result)
          case None => println("domaineOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def setNbCoeursAppreciationDomaine = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val domaineOpt = Domaine.get("Sport")
    utilisateurOpt match {
      case Some(utilisateur) =>
        domaineOpt match {
          case Some(domaine) =>
            val result = AppreciationDomaine.incrNbCoeurs(utilisateur, domaine)
            Logger.debug("result test set quantité appréciation domaine : " + result)
          case None => println("domaineOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def setFavoriAppreciationDomaine = Action {
    val utilisateurOpt = Utilisateur.get("mail1@test.com")
    val domaineOpt = Domaine.get("Sport")
    utilisateurOpt match {
      case Some(utilisateur) =>
        domaineOpt match {
          case Some(domaine) =>
            val result = AppreciationDomaine.setFavori(utilisateur, domaine)
            Logger.debug("result test set quantité appréciation domaine : " + result)
          case None => println("domaineOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  def estFavoriAppreciationDomaine = Action {
    val utilisateurOpt = Utilisateur.get("mail2@test.com")
    val domaineOpt = Domaine.get("Sport")
    utilisateurOpt match {
      case Some(utilisateur) =>
        domaineOpt match {
          case Some(domaine) =>
            val result = AppreciationDomaine.estFavori(utilisateur, domaine)
            Logger.debug("result test set quantité appréciation domaine : " + result)
          case None => println("domaineOpt not found")
        }
      case None => println("utilisateurOpt not found")
    }
    Ok(views.html.index())
  }

  //fin tests AppreciationDomaine

  def createDomaine = Action {
    val result = Domaine.create(Domaine("Sport"))
    Logger.debug("result test create domaine : " + result)
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

  def incremrNbCoeurs = Action {
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
    result.foreach(el => Logger.debug("el : " + el))
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


  def mapage = StackAction {
    implicit request =>
      Ok(views.html.mapage())
  }

  def lectureFlux(url: String) = Action {
    import java.net.URL
    import com.sun.syndication.io.{XmlReader, SyndFeedInput}
    import com.sun.syndication.feed.synd.SyndFeed

    var ok: Boolean = false

    try {
      //val feedUrl : URL = new URL("http://rss.lemonde.fr/c/205/f/3050/index.rss")
      val feedUrl: URL = new URL(url)

      val input: SyndFeedInput = new SyndFeedInput()
      val feed: SyndFeed = input.build(new XmlReader(feedUrl))
      println(feed.getTitle)
      println(feed.getDescription)

      ok = true
      val listeFlux: util.List[_] = feed.getEntries
      val listeFluxCasted: util.List[SyndEntry] = listeFlux.asInstanceOf[util.List[SyndEntry]]

      //Manip avec un iterator pour récuperer une liste "Scala" plus facile pour la manip
      var listeFluxScala: List[SyndEntry] = List()
      val ite = listeFluxCasted.iterator()
      while (ite.hasNext) {
        val tmp = ite.next()
        listeFluxScala = listeFluxScala.::(tmp)
      }

      listeFluxScala.foreach(
        art => {
          println("Titre : " + art.getTitle)
          println("Auteur : " + art.getAuthor)
          println("Date : " + art.getPublishedDate)
          println("Description : " + art.getDescription.getValue)
          println("Lien : " + art.getLink)
          //Liste images  => on la "caste" pour récupérer le bon type (SyndEnclosureImpl) pour pouvoir récuperer l'url des images
          val image: util.List[SyndEnclosureImpl] = art.getEnclosures.asInstanceOf[util.List[SyndEnclosureImpl]]
          // On récupère la première image de la liste et son URL
          if (image.size != 0) {
            for (i <- 0 to image.size - 1)
              println("image : " + image.get(i).getUrl)

          }
        }
      )

    }
    catch {
      case ex: Exception =>
        ex.printStackTrace()
        println("ERROR: " + ex.getMessage)
    }

    if (!ok) {
      println("FeedReader reads and prints any RSS/Atom feed type.")
      println("The first parameter must be the URL of the feed to read.")
    }

    Ok(views.html.index())
  }


}