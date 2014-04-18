package controllers


import models._
import models.database.{Country}
import scala.Some
import org.joda.time.DateTime
import scala.Some
import controllers.sparql.SparqlQueryExecuter
import play.api.data._
import play.api.data.Forms._
import jp.t2v.lab.play2.auth._
import play.api._
import play.api.mvc._
import play.api.libs.json.{JsObject, Json}
import scala.Some
import play.api.libs.json.JsObject


object Application extends Controller with OptionalAuthElement with LoginLogout with AuthConfigImpl {

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          controllers.routes.javascript.Application.getArt,
          controllers.routes.javascript.Application.displayLinkedArt,
          controllers.routes.javascript.Application.getDomaines,
          controllers.routes.javascript.Application.getTypes,
          controllers.routes.javascript.Application.getTop,
          controllers.routes.javascript.Application.getTopJour,
          controllers.routes.javascript.Application.getTopMois,
          controllers.routes.javascript.Application.getTopSemaine,
          controllers.routes.javascript.Application.getTop10,
          controllers.routes.javascript.Application.getArticlesByTag
        )
      ).as("text/javascript")
  }



  def mapage = StackAction {
    implicit request =>
      Ok(views.html.mapage())
  }

  val urlForm = Form(
    single(
      "urlarticle" -> nonEmptyText
    )
  )

  // Router.JavascriptReverseRoute
  def getArt = StackAction {
    implicit request =>

    // Logger.debug("avant")
      val listeArt: List[Article] = Article.getLastArticle
      // Logger.debug("apres")
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.year().get() + "-" + art.date.monthOfYear().get() + "-" +art.date.dayOfMonth().get()  + " "+art.date.hourOfDay().get()+":"+art.date.minuteOfHour().get()
        val tags: List[JsObject] = Tag.getTagsOfArticles(art).map(tag => (Json.obj("id" -> tag._1.id,
          "nom" -> tag._1.nom)))
        Json.obj(
          "id" -> art.id,
          "url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "tags"-> tags,
          "note" -> art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.countLinkedArticles(art)
        )
      })
      // Logger.debug("RES " +res )
      Ok(Json.obj("liste"->res))
  }
  val searchForm = Form(
    single(
      "search" -> nonEmptyText
    )
  )

  def displayArt(id : Int) = StackAction {
    implicit request =>

      Logger.debug("URL OKAY ")
      // Logger.debug("URL TEST " + url)
      val article: Option[Article] = Article.getById(id)
      if (article.isDefined) {
        val listeTag = Tag.getTagsOfArticles(article.get)
        val listeTagArticles: List[Entite] =listeTag.map(elt => elt._1)
        Ok(views.html.visualisationarticle(article.get, listeTagArticles))
      } else {
        Redirect(routes.Application.index).flashing("error" -> "L'article à visualiser n'existe plus ! :o")

            }

  }

  def displayLinkedArt(id : Int) = StackAction {
    implicit request =>

      Logger.debug("TEST DISPLAY LINKED ART" + id)
//       val listeLinked = EstLie.getLinkedArticlesById(id)
//       val listeArt: List[Article] =listeLinked.map(elt=>{
//          Article.getByUrl(elt._2).get
//       })

      val listeArt = EstLie.getById(id)
      val res: List[JsObject] = listeArt.map(art => {
        val dateF: String = art.date.year().get() + "-" + art.date.monthOfYear().get() + "-" +art.date.dayOfMonth().get()  + " "+art.date.hourOfDay().get()+":"+art.date.minuteOfHour().get()
        val tags: List[JsObject] = Tag.getTagsOfArticles(art).map(tag => (Json.obj("id" -> tag._1.id,
          "nom" -> tag._1.nom)))
        Json.obj(
          "id"->art.id,
          "url" -> art.url,
          "titre" -> art.titre,
          "description" -> art.description,
          "site" -> art.site.nom,
          "image" -> art.image,
          "consultationsJour" -> art.consultationsJour,
          "coeurs" -> art.nbCoeurs,
          "domaine" -> art.site.typeSite,
          "tags" -> tags,
          "note" -> art.nbEtoiles,
          "tags"-> tags,
          "note" -> art.nbEtoiles,
          "date" -> dateF,
          "lies" -> EstLie.countLinkedArticles(art)
        )
      })
      Ok(Json.obj("liste"->res))


  }

  def getArticlesByTag = StackAction {
    implicit request =>

      urlForm.bindFromRequest.fold(
        hasErrors = { form =>
          Ok(Json.obj())
        },
        success = { url =>
          val tmp = Entite.getByUrl(url)
          tmp match {
            case Some(entite) =>
              val listeTmp = Tag.getArticlesLies(entite,20)
              listeTmp match {
                case Some(liste) =>
                  val res: List[JsObject] = liste.map(art => {
                    val dateF: String = art._1.date.year().get() + "-" + art._1.date.monthOfYear().get() + "-" +art._1.date.dayOfMonth().get()  + " "+art._1.date.hourOfDay().get()+":"+art._1.date.minuteOfHour().get()
                    val tags: List[JsObject] = Tag.getTagsOfArticles(art._1).map(tag => (Json.obj("id" -> tag._1.id,
                      "nom" -> tag._1.nom)))
                    Json.obj(
                      "id"->art._1.id,
                      "url" -> art._1.url,
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
                      "lies" -> EstLie.countLinkedArticles(art._1)
                    )
                  })
                  Ok(Json.obj("liste"->res))
                case None =>
                  Ok(Json.obj())
              }
            case None =>
              Ok(Json.obj())
          }
        })
  }

  def getTop10= StackAction {
    implicit request =>

      val top: List[Entite] = Entite.lesPlusTaggesDuJour()
      val sparql : SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> sparql.getName(entite.url),
          "nombre" -> entite.apparitionsJour,
          "id" -> entite.id
        )
      })
      Ok(Json.obj("liste"->res))
  }

  def getTop(idType :Int)= StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotations(1000,idType)
      val sparql : SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> sparql.getName(entite.url),
          "nombre" -> entite.apparitions,
          "image" -> sparql.getImage(entite.url),
          "id" -> entite.id
        )
      })
      Ok(Json.obj("liste"->res))
  }

  def getTopJour(idType :Int)= StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotationsJour(1000,idType)
      val sparql : SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> sparql.getName(entite.url),
          "nombre" -> entite.apparitionsJour,
          "image" -> sparql.getImage(entite.url),
          "id" -> entite.id
        )
      })
      Ok(Json.obj("liste"->res))
  }

  def getTopSemaine(idType :Int)= StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotationsSemaine(1000,idType)
      val sparql : SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> sparql.getName(entite.url),
          "nombre" -> entite.apparitionsSemaine,
          "image" -> sparql.getImage(entite.url),
          "id" -> entite.id
        )
      })
      Ok(Json.obj("liste"->res))
  }

  def getTopMois(idType :Int)= StackAction {
    implicit request =>

      val top: List[Entite] = Entite.topAnnotationsMois(1000,idType)
      val sparql : SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      val res: List[JsObject] = top.map(entite => {
        Json.obj("nom" -> sparql.getName(entite.url),
          "nombre" -> entite.apparitionsMois,
          "image" -> sparql.getImage(entite.url),
          "id" -> entite.id
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

  def getTypes = StackAction {
    implicit request =>

      val listeTypes: List[Type] = Type.getAll

      val res: List[JsObject] = listeTypes.map(typeEntite => {
        Json.obj("nom" -> typeEntite.denomination,
        "id" -> typeEntite.id
        )
      })
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

  def statistiques = StackAction {
    implicit request => Ok(views.html.stats())
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
      val siteOpt = Site.getByUrl("www.magness.fr")
      siteOpt match {
        case Some(site) =>
          val result = Article.insert(Article("Monuments Men : jusqu'au bout de l'ennui.", "Thib", "Ceci est la description de la critique de Monuments Men.", new DateTime(), "http://magness.fr/blablabla", site))
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

  def entite(id:Int) = StackAction {
    implicit request =>
      val res = Entite.getById(id)
      val sparql : SparqlQueryExecuter = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql")
      res match{
        case Some(entite) =>
          val url: String = entite.url
          Ok(views.html.entite(sparql.getName(url), sparql.getImage(url), sparql.getImageDescription(url), sparql.getAbstract(url), sparql.getWikiLink(url), url))
        case None =>  Ok(views.html.index())
      }
  }

  def search() = StackAction{
    implicit request =>

      searchForm.bindFromRequest.fold(
        hasErrors = { form =>
          Ok(views.html.index())
        },
        success = { input =>
          val entites:List[Entite] = Entite.rechercheDansNom(input)
          val articles:List[Article]  = Article.rechercheDansTitre(input)
          Ok(views.html.results(entites, articles, input))
        })
  }


}