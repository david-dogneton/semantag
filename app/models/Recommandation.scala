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

  def buildRecommandations(user: Utilisateur): Boolean = {
    val listeEntitesOpt = Utilisateur.getTopEntitesPasFavories(user, 10)
    val articlesLusOpt = Utilisateur.getArticlesLus(user)
    var res = true
    listeEntitesOpt match {
      case Some(listeEntites) => {
        articlesLusOpt match {
          case Some(articlesLus) => {
            for (entite <- listeEntites) {
              var listeArticlesOpt = Tag.getArticlesLies(entite, 10)
              listeArticlesOpt match {
                case Some(listeArticles) => {
                  val max = maxListe(listeArticles)
                  //val listeRecommandations = listeArticles diff articlesLus
                  for (article <- listeArticles) {
                    if (!articlesLus.contains(article._1)) {
                      res = Recommandation.create(new Recommandation(user, article._1, article._2 / max)) && res
                    }
                  }
                }
              }
            }
          }
        }
      }
      case None => None
    }
    res
  }

  def maxListe(articles: List[(Article, Int)]): Int = {
    var max = 0
    for (article <- articles) {
      if (article._2 > max) {
        max = article._2
      }
    }
    max
  }


}
