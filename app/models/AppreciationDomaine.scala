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
case class AppreciationDomaine(utilisateur: Utilisateur, domaine: Domaine, nbCoeurs: Int, estFavori: Boolean = false)

object AppreciationDomaine {
  def create(appreciationDomaine: AppreciationDomaine): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (domaine: Domaine)
         where user.mail = {mailUser} and domaine.nom = {nomDom}
         create (user)-[r:appreciationDomaine {nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(domaine)
      """
    ).on("mailUser" -> appreciationDomaine.utilisateur.mail,
      "nomDom" -> appreciationDomaine.domaine.nom,
      "nbCoeurs" -> appreciationDomaine.nbCoeurs,
      "estFavori" -> appreciationDomaine.estFavori
    ).execute()
  }

  def get(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine {nom : {nomDom}})
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => None
      case head :: tail => {
        Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
      }
    }
  }

  def incrNbCoeurs(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         set r.nbCoeurs = r.nbCoeurs + 1
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbCoeurs(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def estFavori(user: Utilisateur, domaine: Domaine): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => throw new Exception("AppreciationDomaine node doesn't exist.")
      case head :: tail => head[Boolean]("estFavori")
    }
  }

  def setFavori(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val estFavoriList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    estFavoriList match {
      case Nil => None
      case head :: tail => {
        val estFavori = !head[Boolean]("estFavori")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         set r.estFavori = {nouvFavori}
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
          """
        ).on("mailUser" -> user.mail,
          "nomDom" -> domaine.nom,
          "nouvFavori" -> estFavori)().toList

        result match {
          case Nil => None
          case head :: tail => Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
        }
      }
    }
  }

  def delete(user: Utilisateur, domaine: Domaine): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom).execute()
    result
  }


}
