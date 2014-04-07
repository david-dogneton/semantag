package models

import org.anormcypher.{CypherRow, Cypher}

/**
 * Created by Administrator on 19/03/14.
 */
case class EstLie(urlArticleA: String, urlArticleB: String, ponderation: Double) {

}

object EstLie {

  def create(urlA: String, urlB: String, ponderation: Double): Boolean = {
    Cypher(
      """
         match (articleA: Article), (articleB: Article)
         where articleA.url = {urlA} and articleB.url = {urlB}
         create (articleA)-[r:estLie {ponderation : {ponderation}}]->(articleB)
      """
    ).on("urlA" -> urlA,
        "urlB" -> urlB,
        "ponderation" -> ponderation
      ).execute()
  }
  def create(articleA: Article, articleB: Article, ponderation: Double): Boolean = {
      create(articleA.url, articleB.url, ponderation)
  }

  def create(estLie: EstLie): Boolean = {
    create(estLie.urlArticleA, estLie.urlArticleB, estLie.ponderation)
  }

//  def getPonderation(articleA: Article, articleB: Article): Int = {
//
//    val result = Cypher(
//      """
//         match (articleA: Article {url : {urlA}})-[r:estLie]-(articleB: Article {url : {urlB}})
//         return r.ponderation as ponderation
//      """
//    ).on("urlA" -> articleA.url,
//        "urlB" -> articleB.url)().toList
//
//    result match {
//      case Nil=> -1
//      case head::tail => head[BigDecimal]("ponderation").toInt
//    }
//  }

  def getLinkedArticles(article : Article): List[(String, String, Double)] = {

    Cypher(
      """
        MATCH (a:Article)<-[r:`tag`]-(b:Entite)-[r2:`tag`]->(c:Article) where a.url = {url}
        return distinct a.url, c.url, r.quantite, r2.quantite
      """).on("url" -> article.url)().collect {
      case CypherRow(urlA : String, urlB : String, quantiteA : BigDecimal, quantiteB : BigDecimal) => (urlA, urlB, (quantiteB / quantiteA).toDouble)
    }.toList
  }
}
