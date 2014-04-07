package models

import org.anormcypher.Cypher
import org.joda.time.DateTime
import play.api.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class AppreciationSite(utilisateur: Utilisateur, site: Site, totalEtoiles : Int = 0, nbEtoiles : Int = 0, nbCoeurs: Int = 0, estFavori: Boolean = false)

object AppreciationSite {
  def create(appreciationSite: AppreciationSite): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (site: Site)
         where user.mail = {mailUser} and site.url = {urlSite}
         create (user)-[r:appreciationSite {totalEtoiles : {totalEtoiles}, nbEtoiles : {nbEtoiles}, nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(site)
      """
    ).on("mailUser" -> appreciationSite.utilisateur.mail,
      "urlSite" -> appreciationSite.site.url,
      "totalEtoiles" -> appreciationSite.totalEtoiles,
      "nbEtoiles" -> appreciationSite.nbEtoiles,
      "nbCoeurs" -> appreciationSite.nbCoeurs,
      "estFavori" -> appreciationSite.estFavori
    ).execute()
  }

  def get(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site {url : {urlSite}})
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => {
        Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
      }
    }
  }

  def incrNbCoeurs(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.nbCoeurs = r.nbCoeurs + 1
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbCoeurs(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def estFavori(user: Utilisateur, site: Site): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    result match {
      case Nil => throw new Exception("AppreciationSite node doesn't exist.")
      case head :: tail => head[Boolean]("estFavori")
    }
  }

  def setFavori(user: Utilisateur, site: Site): Option[AppreciationSite] = {

    val estFavoriList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url)().toList

    estFavoriList match {
      case Nil => None
      case head :: tail => {
        val estFavori = !head[Boolean]("estFavori")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         set r.estFavori = {nouvFavori}
         return r.totalEtoiles as totalEtoiles, r.nbEtoiles as nbEtoiles, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
          """
        ).on("mailUser" -> user.mail,
          "urlSite" -> site.url,
          "nouvFavori" -> estFavori)().toList

        result match {
          case Nil => None
          case head :: tail => Some(AppreciationSite(user, site, head[BigDecimal]("totalEtoiles").toInt, head[BigDecimal]("nbEtoiles").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
        }
      }
    }
  }

  def delete(user: Utilisateur, site: Site): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url).execute()
    result
  }


}
