package models

import org.anormcypher.{CypherRow, Cypher}

/**
 * Created by Administrator on 19/03/14.
 */
case class Tag(article: Article, entite: Entite, quantite: Int) {

}

object Tag {

  def create(article: Article, entite: Entite, quantite: Int): Boolean = {
    Cypher(
      """
         match (article: Article), (entite: Entite)
         where article.url = {urlArt} and entite.url = {urlEnt}
         create (article)-[r:tag {quantite : {quantite}}]->(entite)
      """
    ).on("urlArt" -> article.url,
        "urlEnt" -> entite.url,
        "quantite" -> quantite
      ).execute()
  }

  def create(tag: Tag): Boolean = {
    create(tag.article, tag.entite, tag.quantite)
  }

  def getQuantite(article: Article, entite: Entite): Int = {

    val result = Cypher(
      """
         match (article: Article {url : {urlArt}})-[r:tag]-(entite: Entite{url : {urlEnt}})
         return r.quantite as quantite
      """
    ).on("urlArt" -> article.url,
        "urlEnt" -> entite.url)().toList

    result match {
      case Nil => -1
      case head :: tail => head[BigDecimal]("quantite").toInt
    }
  }

  def getTagsOfArticles(article: Article): List[(Entite, Int)] = {

    Cypher(
      """
         match (article: Article {url : {urlArt}})-[r:tag]-(entite: Entite)
         return entite.nom,
                entite.url,
                entite.apparitionsJour,
                entite.apparitionsSemaine,
                entite.apparitionsSemaineDerniere,
                entite.apparitionsMois,
                entite.apparitions,
                r.quantite;
      """
    ).on("urlArt" -> article.url)().collect {
      case CypherRow(nom: String,
      url: String,
      apparitionsJour: BigDecimal,
      apparitionsSemaine: BigDecimal,
      apparitionsSemaineDerniere: BigDecimal,
      apparitionsMois: BigDecimal,
      apparitions: BigDecimal,
      quantite: BigDecimal) =>
        (new Entite(nom,
          url,
          apparitionsJour.toInt,
          apparitionsSemaine.toInt,
          apparitionsSemaineDerniere.toInt,
          apparitionsMois.toInt,
          apparitions.toInt),
          quantite.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
    }.toList
  }
}
