package models

import org.anormcypher.Cypher

/**
 * Created by Administrator on 19/03/14.
 */
case class EstLie(articleA: Article, articleB: Article, ponderation: Int) {

}

object EstLie {

  def create(articleA: Article, articleB: Article, ponderation: Int): Boolean = {
      Cypher(
        """
         match (articleA: Article), (articleB: Article)
         where articleA.url = {urlA} and articleB.url = {urlB}
         create (articleA)-[r:estLie {ponderation : {ponderation}}]->(articleB)
        """
      ).on("urlA" -> articleA.url,
          "urlB" -> articleB.url,
          "ponderation" -> ponderation
        ).execute()
  }

  def create(estLie: EstLie): Boolean = {
    create(estLie.articleA, estLie.articleB, estLie.ponderation)
  }

  def getPonderation(articleA: Article, articleB: Article): Int = {

    val result = Cypher(
      """
         match (articleA: Article {url : {urlA}})-[r:estLie]-(articleB: Article {url : {urlB}})
         return r.ponderation as ponderation
      """
    ).on("urlA" -> articleA.url,
        "urlB" -> articleB.url)().toList

    result match {
      case Nil=> -1
      case head::tail => head[BigDecimal]("ponderation").toInt
    }
  }

}
