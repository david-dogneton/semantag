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
case class Recommandation(utilisateur: Utilisateur, article: Article, ponderation: Double)

object Recommandation {
  def create(recommandation: Recommandation): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (article: Article)
         where user.mail = {mailUser} and article.url = {urlArt}
         create (user)-[r:recommandation {ponderation : {ponderation}}]->(article)
      """
    ).on("mailUser" -> recommandation.utilisateur.mail,
      "urlArt" -> recommandation.article.url,
      "ponderation" -> recommandation.ponderation
    ).execute()
  }

  def get(user: Utilisateur, article: Article): Option[Recommandation] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:recommandation]-(article: Article {url : {urlArt}})
         return r.ponderation as ponderation;
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url)().toList

    result match {
      case Nil => None
      case head :: tail => {
        Some(Recommandation(user, article, head[BigDecimal]("ponderation").toDouble))
      }
    }
  }

  def delete(user: Utilisateur, article: Article): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:recommandation]-(article: Article{url : {urlArt}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url).execute()
    result
  }


}
