package models

import java.util
import com.sun.syndication.feed.synd.{SyndEnclosureImpl, SyndEntry}
import org.joda.time.DateTime
import scala.io.Source
import play.api.Logger

/**
 * Created by Romain on 20/03/14.
 */
object FluxRss {

  def miseAJourBddSites = {
    val src = Source.fromFile("public/Liste_Flux_Rss_PFE.csv","utf-8")
    val iter: Iterator[Array[String]] = src.getLines().map(_.split(";"))
//    while(iter.hasNext){
//      val currentline = iter.next()
//      val site = Site(currentline(1),currentline(0),currentline(2))
//      listeSites=listeSites.::(site)
//    }

    val listeSites = iter.map(el => {
      Site(el(1),el(0),el(2))
    }).toList
    Logger.debug("taille liste " + listeSites.size)

    src.close()
    listeSites.foreach(
      site =>{
        if(! Site.get(site.url).isDefined){
          Site.create(site)
          Logger.debug("Le site n'existe pas INSERTION " + site.url)
        }
        else{
          Logger.debug("Le site existe " + site.url)
        }
      }
    )
  }
  //Mise à jour des sites DEJA existants en BDD
  def misAJourTousSites() : Int= {
    val listeSites =Site.getAll()
    if(listeSites.size!=0){
      listeSites.map(
        site => misAJourSite(site)
      ).foldLeft(0)(_ + _)
    } else {
      0
    }
  }


  def misAJourSite(site :Site) : Int = {
    import java.net.URL
    import com.sun.syndication.io.{XmlReader, SyndFeedInput}
    import com.sun.syndication.feed.synd.SyndFeed

    var ok : Boolean = false

    Logger.debug("SITE :" +site.nom + site.typeSite)
    Logger.debug("SITE :" +site.url)
    var nombreArtAdd =0
    try {
      val feedUrl : URL = new URL(site.url)

      val input : SyndFeedInput = new SyndFeedInput()
      val feed : SyndFeed = input.build(new XmlReader(feedUrl))

      ok = true
      // "Cast" obligatoire car l'api ne reconnait pas directement le type renvoyé par getEntries
      val listeFlux: util.List[_] =  feed.getEntries
      val listeFluxCasted : util.List[SyndEntry] = listeFlux.asInstanceOf[util.List[SyndEntry]]

      //Manip avec un iterator pour récuperer une liste "Scala" plus facile pour la manip
      var listeFluxScala : List [SyndEntry] = List()
      val ite: util.Iterator[SyndEntry] =listeFluxCasted.iterator()
      while(ite.hasNext){
        val tmp =ite.next()
        listeFluxScala=listeFluxScala.::(tmp)
      }

      Logger.debug("Nombre flux " + site.nom +"   "+listeFluxScala.size)
      listeFluxScala.foreach(
        art => {
          //On teste pour chaque article du site en cours de MAJ si le lien de l'article correspond à un lien d'un article en BDD
          //Si ce n'est pas le cas => insertion
          if(! Article.getArticle(art.getLink).isDefined){
            val titre = art.getTitle
            val auteur = art.getAuthor
            val date = art.getPublishedDate
            val description = art.getDescription.getValue
            val lien = art.getLink

            //Liste images  => on la "caste" pour récupérer le bon type (SyndEnclosureImpl) pour pouvoir récuperer l'url des images
            val imageList : util.List[SyndEnclosureImpl] = art.getEnclosures.asInstanceOf[util.List[SyndEnclosureImpl]]
            var image : String =""
            // On récupère la première image de la liste et son URL
            if(imageList.size != 0){
              image =imageList.get(0).getUrl
            }
            val nouvelArticle=Article(titre,auteur,description,new DateTime(date),lien,site,image)
            val bool =Article.create(nouvelArticle)
            if(bool){
              nombreArtAdd = nombreArtAdd+1
            }
            else{
              Logger.debug("article non inséré")
            }
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
      Logger.debug("FeedReader reads and prints any RSS/Atom feed type.")
      Logger.debug("The first parameter must be the URL of the feed to read.")
    }
    Logger.debug("Nombre flux RAJOUTE" + site.nom +"   "+nombreArtAdd)
    nombreArtAdd
  }

}
