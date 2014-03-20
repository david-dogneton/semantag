package models

import org.anormcypher.{CypherRow, CypherResultRow, Cypher}
import org.joda.time.DateTime

/**
 * Created by Administrator on 17/03/14.
 */
case class Article(titre: String,
                   auteur: String,
                   description: String,
                   date: DateTime,
                   image: String,
                   url: String,
                   site: Site,
                   consultationsJour: Int = 0,
                   consultationsSemaine: Int = 0,
                   consultationsSemaineDerniere: Int = 0,
                   consultationsMois: Int = 0,
                   consultations: Int = 0,
                   totalEtoiles: Int = 0,
                   nbEtoiles: Int = 0,
                   nbCoeurs: Int = 0
                   ) {

}

object Article {

  def create(article: Article): Boolean = {

    Cypher(
      """
        match (site : Site)
        where site.url = {urlSite}
        create (article: Article {
          titre: {titre},
          auteur: {auteur},
          description: {description},
          date: {date},
          image: {image},
          url: {url},
          consultationsJour: {consultationsJour},
          consultationsSemaine: {consultationsSemaine},
          consultationsSemaineDerniere: {consultationsSemaineDerniere},
          consultationsMois: {consultationsMois},
          consultations: {consultationsMois},
          totalEtoiles: {totalEtoiles},
          nbEtoiles: {nbEtoiles},
          nbCoeurs: {nbCoeurs}
        })-[r: appartient]->(site)
      """
    ).on("titre" -> article.titre,
        "auteur" -> article.auteur,
        "description" -> article.description,
        "date" -> article.date.toString(),
        "image" -> article.image,
        "url" -> article.url,
        "consultationsJour" -> article.consultationsJour,
        "consultationsSemaine" -> article.consultationsSemaine,
        "consultationsSemaineDerniere" -> article.consultationsSemaineDerniere,
        "consultationsMois" -> article.consultationsMois,
        "consultations" -> article.consultations,
        "totalEtoiles" -> article.totalEtoiles,
        "nbEtoiles" -> article.nbEtoiles,
        "nbCoeurs" -> article.nbCoeurs,
        "urlSite" -> article.site.url
      ).execute()
  }

  def getArticle(url: String): Option[Article] = {

    val result: List[CypherResultRow] = Cypher(
      """
        Match (article:Article {url: {url}})--(site:Site)
        return  article.titre as titre,
                article.auteur as auteur,
                article.description as description,
                article.date as date,
                article.image as image,
                article.url as url,
                site.url as urlSite,
                article.consultationsJour as consultationsJour,
                article.consultationsSemaine as consultationsSemaine,
                article.consultationsSemaineDerniere as consultationsSemaineDerniere,
                article.consultationsMois as consultationsMois,
                article.consultations as consultations,
                article.totalEtoiles as totalEtoiles,
                article.nbEtoiles as nbEtoiles,
                article.nbCoeurs as nbCoeurs;
      """).on("url" -> url).apply().toList


    result match {
      case Nil => throw new NoSuchElementException("Article Not Found")
      case head :: tail => head match {
        case CypherRow(titre: String,
        auteur: String,
        description: String,
        date: String,
        image: String,
        url: String,
        urlSite: String,
        consultationsJour: BigDecimal,
        consultationsSemaine: BigDecimal,
        consultationsSemaineDerniere: BigDecimal,
        consultationsMois: BigDecimal,
        consultations: BigDecimal,
        totalEtoiles: BigDecimal,
        nbEtoiles: BigDecimal,
        nbCoeurs: BigDecimal) =>
          val siteOpt = Site.get(urlSite)
          siteOpt match {
            case Some(site) => Some(new Article(
              titre,
              auteur,
              description,
              new DateTime(date),
              image, url,
              new Site(urlSite, site.nom, site.typeSite),
              consultationsJour.toInt,
              consultationsSemaine.toInt,
              consultationsSemaineDerniere.toInt,
              consultationsMois.toInt,
              consultations.toInt,
              totalEtoiles.toInt,
              nbEtoiles.toInt,
              nbCoeurs.toInt))
            case None => None
          }
        case _ => throw new IllegalArgumentException("Mauvais format de l'article")
      }
    }
  }

  def deleteArticle(url: String) = {

    val result: Boolean = Cypher(
      """
        Match (article:Article) where article.url = {url} delete article;
      """).on("url" -> url).execute()

    println("result : " + result)
    result
  }
}
