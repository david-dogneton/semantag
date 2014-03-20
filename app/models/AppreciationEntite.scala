package models

import org.anormcypher.Cypher

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class AppreciationEntite(utilisateur: Utilisateur, entite: Entite, quantite: Int, nbCoeurs: Int, estFavori: Boolean = false)

object AppreciationEntite {
  def create(appreciationEntite: AppreciationEntite): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (entite: Entite)
         where user.mail = {mailUser} and entite.url = {urlEnt}
         create (user)-[r:appreciationEntite {quantite : {quantite}, nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(entite)
      """
    ).on("mailUser" -> appreciationEntite.utilisateur.mail,
      "urlEnt" -> appreciationEntite.entite.url,
      "quantite" -> appreciationEntite.quantite,
      "nbCoeurs" -> appreciationEntite.nbCoeurs,
      "estFavori" -> appreciationEntite.estFavori
    ).execute()
  }

  def get(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def setQuantite(user: Utilisateur, entite: Entite, changementQuantite: Int): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.quantite = r.quantite + {changementQuantite}
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url,
      "changementQuantite" -> changementQuantite)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def incrNbCoeurs(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.nbCoeurs = r.nbCoeurs + 1
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbCoeurs(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def estFavori(user: Utilisateur, entite: Entite): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => throw new Exception("AppreciationEntite node doesn't exist.")
      case head :: tail => head[Boolean]("estFavori")
    }
  }

  def setFavori(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val estFavoriList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    estFavoriList match {
      case Nil => None
      case head :: tail => {
        val estFavori = !head[Boolean]("estFavori")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.estFavori = {nouvFavori}
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
          """
        ).on("mailUser" -> user.mail,
          "urlEnt" -> entite.url,
          "nouvFavori" -> estFavori)().toList

        result match {
          case Nil => None
          case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
        }
      }
    }
  }

  def delete(user: Utilisateur, entite: Entite): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url).execute()
    result
  }


}
