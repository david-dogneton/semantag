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

  /**
   * Construit la liste de recommandations d'un utilisateur à partir de ses goûts (en l'occurrence, de ses "likes" d'entités).
   * @param user utilisateur étudié
   * @return une liste d'articles comportant au maximum 35 éléments (lignes de 7 articles côté vue), correspondant aux goûts de l'utilisateur.
   */
  def buildRecommandations(user: Utilisateur): List[Article] = {
    val requeteSuppression = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:recommandation]-(article: Article)
         delete r
      """
    ).on("mailUser" -> user.mail).execute()
    val listeEntitesOpt = Utilisateur.getTopEntites(user, 10)
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
                  for (article <- listeArticles) {
                    var onAjoute = true
                    for(artTmp <- articlesLus) {
                      if(artTmp.url.equals(article._1.url)) {
                        onAjoute = false
                      }
                    }
                    if (onAjoute) {
                      res = Recommandation.create(new Recommandation(user, article._1, article._2 / max)) && res
                    }
                  }
                }
              }
            }
          }
          case None => {
            for (entite <- listeEntites) {
              var listeArticlesOpt = Tag.getArticlesLies(entite, 10)
              listeArticlesOpt match {
                case Some(listeArticles) => {
                  val max = maxListe(listeArticles)
                  for (article <- listeArticles) {
                      res = Recommandation.create(new Recommandation(user, article._1, article._2 / max)) && res
                  }
                }
              }
            }
          }
        }
      }
      case None => None
    }
    if(res) {
      Utilisateur.getRecommandations(user, 35)
    }
    else {
      throw new Exception("La construction de la liste de recommandations n'a pas fonctionné.")
    }
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
