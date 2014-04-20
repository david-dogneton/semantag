package models

import java.util
import com.sun.syndication.feed.synd.{SyndEnclosureImpl, SyndEntry}
import org.joda.time.DateTime
import scala.io.Source
import play.api.Logger
import controllers.AnnotatorWS
import akka.actor.{ActorSystem, Props, Actor}
import akka.routing.SmallestMailboxRouter
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global


object FluxRss {


  def miseAJourBddSites() = {
    val src = Source.fromFile("public/Liste_Flux_Rss_PFE.csv", "utf-8")
    val iter: Iterator[Array[String]] = src.getLines().map(_.split(";"))
    val listeSites = iter.map(el => {
      Site(el(1), el(0), el(2))
    }).toList
    Logger.debug("taille liste " + listeSites.size)

    src.close()
    listeSites.foreach(
      site => {
        if (!Site.getByUrl(site.url).isDefined) {
          Site.create(site)
          Logger.debug("Le site n'existe pas INSERTION " + site.url)
        }
        else {
          Logger.debug("Le site existe " + site.url)
        }
      }
    )
  }

  private final val nbActors = 1000

  //Mise à jour des sites DEJA existants en BDD

  def misAJourTousSites() = {
    Logger.debug("/*/*/*/*/*/*/*/*/*/*/*/")
    val system = ActorSystem("InsertionSiteArticle")
    val master = system.actorOf(Props(new Master(nbActors)), name = "master")
    master ! Compute
  }
}

case object Compute

case class Work(article: SyndEntry, site: Site)

class Child extends Actor {

  def getLastFlux(art: SyndEntry): Boolean = {
    val dateArticle = art.getPublishedDate
    val dateJoda = new DateTime(dateArticle)
    val dateNowMinusOne = DateTime.now().minusMinutes(30)

    if (dateJoda.isAfter(dateNowMinusOne)) {
      true
    } else {
      false
    }
  }

  def receive: PartialFunction[Any, Unit] = {
    case Work(art, site) =>
      //On teste pour chaque article du site en cours de MAJ si le lien de l'article correspond à un lien d'un article en BDD
      //Si ce n'est pas le cas => insertion

      if (getLastFlux(art) && !Article.getByUrl(art.getLink).isDefined) {
        //      if (!Article.getByUrl(art.getLink).isDefined) {

        val titre = art.getTitle
        val auteur = art.getAuthor
        val date = new DateTime(art.getPublishedDate)
        val dateValid = if (date.isAfter(DateTime.now())) {
          DateTime.now()
        } else {
          date
        }

        val descriptionValue = art.getDescription.getValue
        val descriptionIndex = descriptionValue.indexOf("<img")
        val descriptionWithoutImg = if (descriptionIndex != -1) {
          descriptionValue.substring(0, descriptionIndex)
        } else {
          descriptionValue
        }
        val description =if(descriptionWithoutImg.length > 250){
          descriptionWithoutImg.substring(0,247).concat("...")

        }else {
          descriptionWithoutImg
        }
        val lien = art.getLink
        //Liste images  => on la "caste" pour récupérer le bon type (SyndEnclosureImpl) pour pouvoir récuperer l'url des images
        val imageList: util.List[SyndEnclosureImpl] = art.getEnclosures.asInstanceOf[util.List[SyndEnclosureImpl]]

        // On récupère la première image de la liste et son URL
        val image = if (imageList.size != 0) {
          imageList.get(0).getUrl
        } else {
          ""
        }
        val nouvelArticle = Article(titre, auteur, description, dateValid, lien, site, image)

        val allResources: Future[List[ResourceDbPedia]] = AnnotatorWS.annotate(titre + ". " + description)

        // on regroupe les éléments de la liste selon leurs URIs => (Key : Uri => Valeurs : listes des éléments identiques)
        // on renvoie le premier élément et la taille de la liste (quantité dans tag)
        val uniqueResources: Future[Map[ResourceDbPedia, Int]] = allResources.map(el => {
          val mapByUri: Map[String, List[ResourceDbPedia]] = el.groupBy(_.uri)
          mapByUri.map(el => {
            (el._2.head, el._2.size)
          })
        })
        // on crée l'entité et le tag associé au nouvel article créé
        val articleInsertedOpt = Article.insert(nouvelArticle)
        articleInsertedOpt match {
          case Some(articleInserted) =>

            uniqueResources.map(liste => {
              liste.map(el => {
                val entite = new Entite(el._1.surfaceForm, el._1.uri, el._2, el._2, el._2, el._2, el._2)
                val entiteOpt = Entite.getByUrl(el._1.uri)
                val entiteWithId: Entite = if (!entiteOpt.isDefined) {
                  Tag.createTagAndEntity(articleInserted, entite, el._2).get
                } else {
                  val entiteRecupere = entiteOpt.get
                  Tag.create(articleInserted, entiteRecupere, el._2)
                  Entite.incrApparitions(entiteRecupere)
                  entiteRecupere
                }
                el._1.types.split(",").map(typeEl => {
                  if (typeEl != "") {
                    val nouveauType = new Type(typeEl)
                    if (!Type.get(typeEl).isDefined) {
                      APourType.createTypeAndRel(entiteWithId, nouveauType)
                    } else {
                      APourType.create(entiteWithId, nouveauType)
                    }
                  }
                })
              })
              EstLie.getLinkedArticles(articleInserted).map(el => EstLie.create(el._1, el._2, el._4))
            })
          case None =>
        }

      }
  }
}

class Master(nbActors: Int) extends Actor {

  val smallestMailBoxRouter = context.actorOf(Props[Child].withRouter(SmallestMailboxRouter(nbActors)), name = "workerRouter")

  def receive = {
    case Compute => traitementSite(Site.getAll(), 0)
  }


  def traitementSite(listeSites: List[Site], res: Int): Int = {

    listeSites match {
      case Nil => Logger.debug("Nombre flux Totaux: " + res)
        res
      case head :: tail =>
        val nbFlux = misAJourSite(head)
        Logger.debug("Nombre flux RAJOUTE" + head.nom + "   " + res)
        traitementSite(tail, nbFlux + res)
    }
  }

  def misAJourSite(site: Site): Int = {
    import java.net.URL
    import com.sun.syndication.io.{XmlReader, SyndFeedInput}
    import com.sun.syndication.feed.synd.SyndFeed

    var ok: Boolean = false

    Logger.debug("SITE :" + site.nom + site.typeSite)
    Logger.debug("SITE :" + site.url + ", " + site.id)
    try {
      val feedUrl: URL = new URL(site.url)
      val input: SyndFeedInput = new SyndFeedInput()
      val feed: SyndFeed = input.build(new XmlReader(feedUrl))

      ok = true
      // "Cast" obligatoire car l'api ne reconnait pas directement le type renvoyé par getEntries
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
        art => smallestMailBoxRouter ! Work(art, site)
      )
      listeFluxScala.size
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        println("ERROR: " + ex.getMessage)
        0
    }

    //    if (!ok) {
    //      Logger.debug("FeedReader reads and prints any RSS/Atom feed type.")
    //      Logger.debug("The first parameter must be the URL of the feed to read.")
    //    }
  }
}
