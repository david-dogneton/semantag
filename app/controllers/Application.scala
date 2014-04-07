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
import jp.t2v.lab.play2.auth.{OptionalAuthElement, AuthenticationElement}
import scala.Some


import play.api.mvc.{Action, Controller}
import jp.t2v.lab.play2.auth._
import play.api.data.Form
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.core.Router
import play.api.libs.json.{JsObject, Json}


object Application extends Controller with OptionalAuthElement with LoginLogout with AuthConfigImpl {


  def javascriptRoutes = Action {
    implicit request =>
      import routes.javascript._
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          controllers.routes.javascript.Application.getArt
          //Users.get
        )
      ).as("text/javascript")
  }

  def mapage = StackAction {
    implicit request =>
    // implicit val maybeUser: Option[User] = Some(loggedIn)
      Ok(views.html.mapage())
  }

  // Router.JavascriptReverseRoute
  def getArt = StackAction {
    implicit request =>

      Logger.debug("avant")
      val listeArt: List[Article] = Article.getLastArticle
      Logger.debug("apres")
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.dayOfMonth() + "-" + art.date.monthOfYear() + "-" + art.date.year()
        val tags: List[String] = Tag.getTagsOfArticles(art).map(tag => /*(*/tag._1.nom/*, tag._1.url*/)/*(*/
        Json.obj("url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags"-> tags,
          "note" ->art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.getLinkedArticles(art).size
        )
      })
      Logger.debug("RES " +res )
      Ok(Json.obj("liste"->res))

    //      val art = Article.getArticle("http://www.lemonde.fr/proche-orient/article/2014/03/31/israel-l-ancien-premier-ministre-ehoud-olmert-reconnu-coupable-de-corruption_4392663_3218.html#xtor=RSS-3208")
    //      Logger.debug("Test article " + art.get.date + "  ..." + art.get.titre)
    //
    //
    //      Ok(Json.obj("url" -> art.get.url,
    //        "titre"->art.get.titre,
    //        "description"->art.get.description,
    //        "site"->art.get.site.nom,
    //        "image"->art.get.image,
    //        "consultationsJour" -> art.get.consultationsJour,
    //        "coeurs"->art.get.nbCoeurs,
    //         "domaine" -> art.get.site.typeSite,
    //         //tags A FAIRE,
    //         // Note a faire,
    //         "date" ->dateF
    //
    //
    //          //lies
    //         ))

  }

  def index = StackAction {
    implicit request =>
      val maybeUser: Option[User] = loggedIn
      val user: User = maybeUser.getOrElse(Utilisateur("default", "", ""))
      Ok(views.html.index())
  }


  def presentation = StackAction {
    implicit request => Ok(views.html.presentation())
  }

  def create = StackAction {
    implicit request =>
      val result = Country.create()
      Logger.debug("result : " + result)
      Ok(views.html.index())
  }

  def createEntite = StackAction {
    implicit request =>
      val result = Entite.create(Entite("Robin Van Persie", "http://quartsDeFinale.com"))
      Logger.debug("result test create entité : " + result)
      Ok(views.html.index())
  }

  def createAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val entiteOpt = Entite.get("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.create(AppreciationEntite(utilisateur, entite, 4, 3))
              Logger.debug("result test create appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def getAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.get("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.get(utilisateur, entite)
              Logger.debug("result test create appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setQuantiteAppreciationEntite = StackAction {
    implicit request =>
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

  def setNbCoeursAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.get("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.incrNbCoeurs(utilisateur, entite)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def setFavoriAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail1@test.com")
      val entiteOpt = Entite.get("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.setFavori(utilisateur, entite)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def estFavoriAppreciationEntite = StackAction {
    implicit request =>
      val utilisateurOpt = Utilisateur.get("mail2@test.com")
      val entiteOpt = Entite.get("http://quartsDeFinale.com")
      utilisateurOpt match {
        case Some(utilisateur) =>
          entiteOpt match {
            case Some(entite) =>
              val result = AppreciationEntite.estFavori(utilisateur, entite)
              Logger.debug("result test set quantité appréciation entité : " + result)
            case None => println("entiteOpt not found")
          }
        case None => println("utilisateurOpt not found")
      }
      Ok(views.html.index())
  }

  def createSite = StackAction {
    implicit request =>
      val result = Site.create(Site("www.magness.fr", "Magness", "Informations diverses"))
      Logger.debug("result test create site : " + result)
      Ok(views.html.index())
  }

  def createNote = StackAction {
    implicit request =>
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

  def createConsultation = StackAction {
    implicit request =>
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

  def createRecommandation = StackAction {
    implicit request =>
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

  def getNote = StackAction {
    implicit request =>
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

  def getConsultation = StackAction {
    implicit request =>
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

  def getRecommandation = StackAction {
    implicit request =>
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

  def createArticle = StackAction {
    implicit request =>
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
  def createAppreciationDomaine = StackAction {
    implicit request =>
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

  def getAppreciationDomaine = StackAction {
    implicit request =>
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

  def setNbCoeursAppreciationDomaine = StackAction {
    implicit request =>
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

  def setFavoriAppreciationDomaine = StackAction {
    implicit request =>
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

  def estFavoriAppreciationDomaine = StackAction {
    implicit request =>
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

  def createDomaine = StackAction {
    implicit request =>
      val result = Domaine.create(Domaine("Sport"))
      Logger.debug("result test create domaine : " + result)
      Ok(views.html.index())
  }

  def createUser = StackAction {
    implicit request =>
      val result = Utilisateur.create(Utilisateur("mail2@test.com", "mdpTest2", "pseudoTest2"))
      Logger.debug("result test create user : " + result)
      Ok(views.html.index())
  }

  def getUser = StackAction {
    implicit request =>
      val resultOpt = Utilisateur.get("mail1Change@test.com")
      resultOpt match {
        case Some(result) =>
          Logger.debug("result test get user : " + result)
        case None => println("Utilisateur not found.")
      }
      Ok(views.html.index())
  }


  def setMailUser = StackAction {
    implicit request =>
      val result = Utilisateur.setMail("mail1Change@test.com", "mail1Change@test.com")
      Logger.debug("result test set mail user : " + result)
      Ok(views.html.index())
  }

  def setPseudoUser = StackAction {
    implicit request =>
      val result = Utilisateur.setPseudo("mail1Change@test.com", "pseudoTest1Change")
      Logger.debug("result test set pseudo user : " + result)
      Ok(views.html.index())
  }

  def setMdpUser = StackAction {
    implicit request =>
      val result = Utilisateur.setMdp("mail1Change@test.com", "mdpTest1Change")
      Logger.debug("result test set mdp user : " + result)
      Ok(views.html.index())
  }

  def incremrNbCoeurs = StackAction {
    implicit request =>
      val result = Utilisateur.incrementerNbCoeurs("mail1Change@test.com")
      Logger.debug("result test incrémenter nb coeurs : " + result)
      Ok(views.html.index())
  }

  def decrementerNbCoeurs = StackAction {
    implicit request =>
      val result = Utilisateur.decrementerNbCoeurs("mail1Change@test.com")
      Logger.debug("result test décrémenter nb coeurs : " + result)
      Ok(views.html.index())
  }

  def deleteUser = StackAction {
    implicit request =>
      val result = Utilisateur.delete("mail1Change@test.com")
      Logger.debug("result test delete : " + result)
      Ok(views.html.index())
  }

  def delete = StackAction {
    implicit request =>
      Country.delete()
      Ok(views.html.index())
  }

  def getNodeOfFrance = StackAction {
    implicit request =>
      val result: List[(String, String)] = Country.getNodesOfFrance()
      result.foreach(el => Logger.debug("el : " + el))
      Ok(views.html.index())
  }


  def getAllNodes = StackAction {
    implicit request =>
      val result: List[(String, String, Double)] = Country.getAllCountries()
      result.foreach(el => Logger.debug("el : " + el))
      Ok(views.html.index())
  }

  def test = StackAction {
    implicit request =>
      Ok(views.html.test())
  }


  def miseAJourFlux = StackAction {
    implicit request =>
      FluxRss.misAJourTousSites()
      Ok(views.html.index())

  }

  def miseAJourSites = StackAction {
    implicit request =>
      FluxRss.miseAJourBddSites
      Ok(views.html.index())
  }

}