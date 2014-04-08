package controllers


import models._
import models.database.Country
import org.joda.time.DateTime
import scala.Some
import controllers.sparql.SparqlQueryExecuter
import play.api.data._
import play.api.data.Forms._
import jp.t2v.lab.play2.auth._
import play.api._
import play.api.mvc._
import play.api.libs.json.{JsObject, Json}


object Application extends Controller with OptionalAuthElement with LoginLogout with AuthConfigImpl {

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          controllers.routes.javascript.Application.getArt,
          controllers.routes.javascript.Application.displayLinkedArt,
          controllers.routes.javascript.Application.getArt,
          controllers.routes.javascript.Application.getDomaines,
          controllers.routes.javascript.Application.getTop,
          controllers.routes.javascript.Application.getArticlesByTag
        )
      ).as("text/javascript")
  }

  def mapage = StackAction {
    implicit request =>
    // implicit val maybeUser: Option[User] = Some(loggedIn)
      Ok(views.html.mapage())
  }


  val urlForm = Form(
    single(
      "urlarticle" -> nonEmptyText
    )
  )


  def displayArt = StackAction{
    implicit request =>

      urlForm.bindFromRequest.fold(
        hasErrors = { form =>
          Logger.debug("BUG URL ")
          Redirect(routes.Application.index)
        },
        success = { url =>
          Logger.debug("URL OKAY ")
          Logger.debug("URL TEST " + url)
          val article: Option[Article] = Article.getByUrl(url)
          if (article.isDefined) {

            Ok(views.html.visualisationarticle(article.get))
          }else{
            Redirect(routes.Application.index).flashing("error" -> "L'article à viualiser n'existe plus ! :o")

          }

        })
  }

  def displayLinkedArt = StackAction {
    implicit request =>


      Ok("")


  }

  def getArticlesByTag = StackAction {
    implicit request =>

      urlForm.bindFromRequest.fold(
        hasErrors = { form =>
          Logger.debug("BUG URL get by tag")
          Ok(Json.obj())
        },
        success = { url =>
          Logger.debug("URL OKAY get by tag ")
          Logger.debug("URL TEST get by tag" + url)
          val tmp = Entite.getByUrl(url)
          tmp match {
            case Some(entite) =>
              val listeTmp = Tag.getArticlesLies(entite,20)
              Logger.debug("TAILLE LISTE get by tag " +listeTmp.size)
              listeTmp match {
                case Some(liste) =>
                  val res: List[JsObject] = liste.map(art => {
                    val dateF: String = art._1.date.year().get() + "-" + art._1.date.monthOfYear().get() + "-" +art._1.date.dayOfMonth().get()  + " "+art._1.date.hourOfDay().get()+":"+art._1.date.minuteOfHour().get()
                    val tags: List[JsObject] = Tag.getTagsOfArticles(art._1).map(tag => (Json.obj("url" -> tag._1.url,
                      "nom" -> tag._1.nom)))
                    Json.obj("url" -> art._1.url,
                      "titre" -> art._1.titre,
                      "description" -> art._1.description,
                      "site" -> art._1.site.nom,
                      "image" -> art._1.image,
                      "consultationsJour" -> art._1.consultationsJour,
                      "coeurs" -> art._1.nbCoeurs,
                      "domaine" -> art._1.site.typeSite,
                      "tags"-> tags,
                      "note" ->art._1.nbEtoiles,
                      "date" -> dateF,
                      "lies" -> EstLie.getLinkedArticles(art._1).size
                    )
                  })
                  Logger.debug("RENVOIT RES get by tag :"+res)
                  Logger.debug("FIN RENVOI RES:")
                  Ok(Json.obj("liste"->res))
                case None =>
                  Logger.debug("NONE SUR lISTE get by tag ")
                  Ok(Json.obj())
              }
            case None =>
              Logger.debug("NONE SUR ENTITE get by tag ")
              Ok(Json.obj())
          }
        })
  }

  def getTop= StackAction {
    implicit request =>

      val top: List[Entite] = Entite.lesPlusTaggesDuJour()

      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> entite.nom,
          "nombre" -> entite.apparitionsJour,
          "image" -> SparqlQueryExecuter.getImage(entite.url),
          "url" -> entite.url
        )
      })
      Ok(Json.obj("liste"->res))
  }


  def getDomaines = StackAction {
    implicit request =>

      val listeDomaines: List[Site] = Site.getTypes()

      val res: List[JsObject] = listeDomaines.map(site => {
        Json.obj("nom" -> site.typeSite
        )
      })
      Ok(Json.obj("liste"->res))
  }

  // Router.JavascriptReverseRoute
  def getArt = StackAction {
    implicit request =>

    // Logger.debug("avant")
      val listeArt: List[Article] = Article.getLastArticle
      // Logger.debug("apres")
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.year().get() + "-" + art.date.monthOfYear().get() + "-" +art.date.dayOfMonth().get()  + " "+art.date.hourOfDay().get()+":"+art.date.minuteOfHour().get()
        val tags: List[JsObject] = Tag.getTagsOfArticles(art).map(tag => (Json.obj("url" -> tag._1.url,
          "nom" -> tag._1.nom)))
        Json.obj("url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags"-> tags,
          "note" -> art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.getLinkedArticles(art).size
        )
      })
      // Logger.debug("RES " +res )
      Ok(Json.obj("liste"->res))
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
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
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
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
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
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
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
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
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
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
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
      val entiteOpt = Entite.getByUrl("http://quartsDeFinale.com")
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
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
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
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
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
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
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
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
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
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
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
      val articleOpt = Article.getByUrl("http://magness.fr/blablabla")
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
      val resultOpt = Utilisateur.get("mail3@test.com")
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

  def entite = StackAction {
    implicit request =>
      urlForm.bindFromRequest.fold(
        hasErrors = { form =>
          Logger.debug("BUG URL ")
          Ok(views.html.index())
        },
        success = { url =>
          Logger.debug("URL OKAY ")
          Logger.debug("URL TEST " + url)
          Ok(views.html.entite(SparqlQueryExecuter.getName(url), SparqlQueryExecuter.getImage(url), SparqlQueryExecuter.getImageDescription(url), SparqlQueryExecuter.getAbstract(url), SparqlQueryExecuter.getWikiLink(url), url))
        })
  }


}