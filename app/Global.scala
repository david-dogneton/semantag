/**
 * Created by Administrator on 15/03/14.
 */
import play.api._
import org.anormcypher._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Neo4jREST.setServer("localhost", 7474, "/db/data/")
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
