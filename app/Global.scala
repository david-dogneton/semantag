/**
 * Created by Administrator on 15/03/14.
 */

import java.util.concurrent.TimeUnit
import models.FluxRss
import org.joda.time.DateTime
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

    Akka.system.scheduler
      .scheduleOnce(
        Duration.create(0,TimeUnit.MILLISECONDS)
        ,
        new Runnable() {
          override def run()= {
            Logger.info("ON START" + System.currentTimeMillis())
            Logger.debug("Mise à jour de la BDD de sites ...")
            FluxRss.miseAJourBddSites
          }
        })


//    Akka.system.scheduler
//      .schedule(
//        Duration.create(0,TimeUnit.SECONDS),
//        Duration.create(5, TimeUnit.MINUTES),
//        new Runnable() {
//          override def run()= {
//            Logger.debug("===============================================")
//            Logger.debug("Toutes les 5 minutes : mise à jour " + System.currentTimeMillis())
//            val nbnewsart=FluxRss.misAJourTousSites()
//            Logger.debug("Nombre articles rajoutés TOTAL :" +nbnewsart)
//            Logger.debug("===============================================")
//          }
//        }
//      )

  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
