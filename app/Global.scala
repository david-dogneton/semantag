/**
 * Created by Administrator on 15/03/14.
 */

import java.util.concurrent.TimeUnit
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
            Logger.info("ON START" + System.currentTimeMillis())}
        })


//    Akka.system.scheduler
//      .schedule(
//        Duration.create(0,TimeUnit.SECONDS),
//        //Duration.create(nextExecutionInSeconds(8, 0),TimeUnit.SECONDS),
//        //Duration.create(24, TimeUnit.HOURS),
//        Duration.create(10, TimeUnit.SECONDS),
//        new Runnable() {
//          override def run()= {
//            Logger.info("EVERY DAY AT 8:00" + System.currentTimeMillis()) }
//        }
//      )

  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
