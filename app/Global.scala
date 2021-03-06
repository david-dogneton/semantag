

import akka.actor.{Props, ActorSystem}
import java.util.concurrent.TimeUnit
import models.{Utils, Master, FluxRss}
import play.api._
import org.anormcypher._
import play.api.libs.concurrent.Akka
import scala.concurrent.duration.Duration
import play.api.Play.current
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

object Global extends GlobalSettings {

  override def onStart(app: Application) {

    Neo4jREST.setServer("localhost", 7474, "/db/data/")
    Logger.info("Application has started")

//    Akka.system.scheduler
//      .scheduleOnce(
//        Duration.create(0,TimeUnit.MILLISECONDS)
//        ,
//        new Runnable() {
//          override def run()= {
//            Logger.debug("ON START" + System.currentTimeMillis())
//            Logger.debug("Mise à jour de la BDD de sites ...")
//            FluxRss.miseAJourBddSites
//          }
//        })

   val nbActors = 100
    val system = ActorSystem("InsertionSiteArticle")
    val master = system.actorOf(Props(new Master(nbActors)), name = "master")

//    Akka.system.scheduler
//      .schedule(
//        Duration.create(0,TimeUnit.SECONDS),
//        Duration.create(5, TimeUnit.MINUTES),
//        new Runnable() {
//          override def run()= {
//            Logger.debug("===============================================")
//            Logger.debug("Toutes les 5 minutes : mise à jour " + System.currentTimeMillis())
//            //val nbnewsart=FluxRss.misAJourTousSites()
//            master ! Compute
//            //Logger.debug("Nombre articles rajoutés TOTAL :" +nbnewsart)
//            Logger.debug("===============================================")
//          }
//        }
//      )

    /**************************************************/

/*   Akka.system.scheduler
      .schedule(
        Duration.create(0,TimeUnit.SECONDS),
        Duration.create(1, TimeUnit.MINUTES),
        new Runnable() {
          override def run()= {
            Logger.debug("===============================================")
            Logger.debug("Toutes les 1 minutes : mise à jour " + System.currentTimeMillis())
            val nbnewsart=FluxRss.misAJourTousSites()
            Logger.debug("Nombre articles rajoutés TOTAL :" +nbnewsart)
            Logger.debug("===============================================")
          }
        }
      )

    Akka.system.scheduler.schedule(
      Duration.create(0, TimeUnit.SECONDS),
      Duration.create(2, TimeUnit.MINUTES),
      new Runnable() {
        override def run() {
          Utils.remiseAZeroJour()
          Logger.debug("remise a zéro articles")
        }
      })*/


  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
