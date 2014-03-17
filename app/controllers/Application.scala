package controllers

import models.database.Country

import play.api._
import play.api.mvc._
import com.sun.syndication.feed.synd.SyndEntry
import java.util

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def create = Action {
    val result = Country.create()
    Logger.debug("result : "+result)
    Ok(views.html.index("Your new application is ready."))
  }

  def get = Action {
    val result: List[(String, String)] = Country.getNodesOfFrance()
    result.foreach(el=> Logger.debug("el : "+el))
    Ok(views.html.index("Your new application is ready."))
  }

  def podcaststest = Action {
    import java.net.URL
    import java.io.InputStreamReader
    import com.sun.syndication.io.{XmlReader, SyndFeedInput}
    import com.sun.syndication.feed.synd.SyndFeed

    var ok : Boolean = false

    try {
      //          URL feedUrl = new URL(args[0]);
      //            URL feedUrl = new URL("http://lescastcodeurs.libsyn.com/rss");
      val feedUrl : URL = new URL("http://rss.lemonde.fr/c/205/f/3050/index.rss")

      val input : SyndFeedInput = new SyndFeedInput()
      // val feed2 : WireFeed = input.
      val feed : SyndFeed = input.build(new XmlReader(feedUrl))
      println(feed.getTitle)
      println(feed.getDescription)

      ok = true
      //: List[SyndEntry]
      val listeFlux: util.List[_] =  feed.getEntries
      val listeFluxCasted : util.List[SyndEntry] = listeFlux.asInstanceOf[util.List[SyndEntry]]

      //val listeFluxCasted2 : List[SyndEntry] = listeFluxCasted.asInstanceOf[List[SyndEntry]]

      var listeFluxScala : List [SyndEntry] = List()

      val ite =listeFluxCasted.iterator()
      while(ite.hasNext){
        val tmp =ite.next()
        listeFluxScala=listeFluxScala.::(tmp)
      }

      listeFluxScala.foreach(
        p => println( "Titre : " +p.getTitle)
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