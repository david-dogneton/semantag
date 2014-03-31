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

/**
 * Created by Romain on 20/03/14.
 */
object FluxRss {

  def miseAJourBddSites() = {
    var listeSites: List[Site] = List()
    val src = Source.fromFile("public/Liste_Flux_Rss_PFE.csv", "utf-8")
    val iter: Iterator[Array[String]] = src.getLines().map(_.split(";"))

    while (iter.hasNext) {
      val currentline = iter.next()
      val site = Site(currentline(1), currentline(0), currentline(2))
      listeSites = listeSites.::(site)
    }

    Logger.debug("taille liste " + listeSites.size)

    src.close()
    listeSites.foreach(
      site => {
        if (!Site.get(site.url).isDefined) {
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
    master ! Process
  }
}

case object Process

case class Work(article: SyndEntry, site: Site)

class Child extends Actor {

  def receive = {
    case Work(art, site) =>
      //On teste pour chaque article du site en cours de MAJ si le lien de l'article correspond à un lien d'un article en BDD
      //Si ce n'est pas le cas => insertion
      if (!Article.getArticle(art.getLink).isDefined) {

        val titre = art.getTitle
        val auteur = art.getAuthor
        val date = art.getPublishedDate
        val description = art.getDescription.getValue
        val lien = art.getLink
        //Liste images  => on la "caste" pour récupérer le bon type (SyndEnclosureImpl) pour pouvoir récuperer l'url des images
        val imageList: util.List[SyndEnclosureImpl] = art.getEnclosures.asInstanceOf[util.List[SyndEnclosureImpl]]

        // On récupère la première image de la liste et son URL
        val image = if (imageList.size != 0) {
          imageList.get(0).getUrl
        } else {
          ""
        }
        val nouvelArticle = Article(titre, auteur, description, new DateTime(date), lien, site, image)
        val allResources: Future[List[ResourceDbPedia]] = AnnotatorWS.annotate(titre + ". " + description + auteur)

        // on regroupe les éléments de la liste selon leurs URIs => (Key : Uri => Valeurs : listes des éléments identiques)
        // on renvoie le premier élément et la taille de la liste (quantité dans tag)
        val uniqueResources: Future[Map[ResourceDbPedia, Int]] = allResources.map(el => {
          val mapByUri: Map[String, List[ResourceDbPedia]] = el.groupBy(_.uri)
          mapByUri.map(el => {
            (el._2.head, el._2.size)
          })
        })

        // on crée l'entité et le tag associé au nouvel article créé
        uniqueResources.map(liste => liste.map(el => {
          val bool = Article.create(nouvelArticle)

          if (bool) {
            val entite = new Entite(el._1.surfaceForm, el._1.uri, el._2, el._2, el._2, el._2, el._2)
            if (!Entite.get(el._1.uri).isDefined) {
              Entite.create(entite)
            }
            Tag.create(nouvelArticle, entite, el._2)
            el._1.types.split(",").map(typeEl => {
              if (typeEl != "") {
                val nouveauType = new Type(typeEl)
                if (!Type.get(typeEl).isDefined) {
                  Type.create(nouveauType)
                }
                APourType.create(entite, nouveauType)
              }
            })
          }
        }))
      }
  }
}

class Master(nbActors: Int) extends Actor {

  val smallestMailBoxRouter = context.actorOf(Props[Child].withRouter(SmallestMailboxRouter(nbActors)), name = "workerRouter")

  def receive = {
    case Process => traitementSite(Site.getAll(), 0)
  }


  def traitementSite(listeSites: List[Site], res: Int): Int = {

    listeSites match {
      case Nil => res + 1
      case head :: tail =>
        misAJourSite(head)
        Logger.debug("Nombre flux RAJOUTE" + head.nom + "   " + res)
        traitementSite(tail, res + 1)
    }
  }

  def misAJourSite(site: Site) = {
    import java.net.URL
    import com.sun.syndication.io.{XmlReader, SyndFeedInput}
    import com.sun.syndication.feed.synd.SyndFeed

    var ok: Boolean = false

    Logger.debug("SITE :" + site.nom + site.typeSite)
    Logger.debug("SITE :" + site.url)
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
      Logger.debug("Nombre flux " + site.nom + "   " + listeFluxScala.size)
      listeFluxScala.foreach(
        art => smallestMailBoxRouter ! Work(art, site)
      )
    }
    catch {
      case ex: Exception =>
        ex.printStackTrace()
        println("ERROR: " + ex.getMessage)
    }

    if (!ok) {
      Logger.debug("FeedReader reads and prints any RSS/Atom feed type.")
      Logger.debug("The first parameter must be the URL of the feed to read.")
    }
  }
}
