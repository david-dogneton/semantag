package models

import org.anormcypher.{CypherRow, CypherResultRow, Cypher}
import org.joda.time.{DateTimeUtils, Duration, DateTime}

/**
 * Created by Administrator on 17/03/14.
 */
case class Article(titre: String,
                   auteur: String,
                   description: String,
                   date: DateTime,
                   url: String,
                   site: Site,
                   image: String = "",
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

//    val dt = new DateTime(article.date)
//    val milis: Long = dt.getMillis
//    val duration = Duration.millis(milis)
//    val dateF = DateTimeUtils.getDurationMillis(duration)
    val dateF = article.date.toString()

    Cypher(
      """
        match (site : Site)
        where site.url = {urlSite}
        create (article: Article {
          titre: {titre},
          auteur: {auteur},
          description: {description},
          date: {date},
          url: {url},
          image: {image},
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
        "date" -> dateF,
        "url" -> article.url,
        "image" -> article.image,
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
                article.url as url,
                site.url as urlSite,
                article.image as image,
                article.consultationsJour as consultationsJour,
                article.consultationsSemaine as consultationsSemaine,
                article.consultationsSemaineDerniere as consultationsSemaineDerniere,
                article.consultationsMois as consultationsMois,
                article.consultations as consultations,
                article.totalEtoiles as totalEtoiles,
                article.nbEtoiles as nbEtoiles,
                article.nbCoeurs as nbCoeurs;
      """).on("url" -> url)().toList

    result match {
      case Nil => None
      case head :: tail => head match {
        case CypherRow(titre: String,
        auteur: String,
        description: String,
        date: String,
        url: String,
        urlSite: String,
        image: String,
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
              url,
              new Site(urlSite, site.nom, site.typeSite),
              image,
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

  def getLastArticle() = {

    val result: List[Article] = Cypher(
      """
        Match (article:Article)--(site:Site)
                return  article.titre as titre,
                        article.auteur as auteur,
                        article.description as description,
                        article.date as date,
                        article.url as url,
                        site.url as urlSite,
                        article.image as image,
                        article.consultationsJour as consultationsJour,
                        article.consultationsSemaine as consultationsSemaine,
                        article.consultationsSemaineDerniere as consultationsSemaineDerniere,
                        article.consultationsMois as consultationsMois,
                        article.consultations as consultations,
                        article.totalEtoiles as totalEtoiles,
                        article.nbEtoiles as nbEtoiles,
                        article.nbCoeurs as nbCoeurs
                				ORDER BY article.date DESC
                        LIMIT 50;
      """)().collect {
      case CypherRow(titre: String,
                      auteur: String,
                      description: String,
                      date: String,
                      url: String,
                      urlSite: String,
                      image: String,
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
          case Some(site) => new Article(
            titre,
            auteur,
            description,
            new DateTime(date),
            url,
            new Site(urlSite, site.nom, site.typeSite),
            image,
            consultationsJour.toInt,
            consultationsSemaine.toInt,
            consultationsSemaineDerniere.toInt,
            consultationsMois.toInt,
            consultations.toInt,
            totalEtoiles.toInt,
            nbEtoiles.toInt,
            nbCoeurs.toInt)
          case None => throw new NoSuchElementException("Pas de site pour cette article")
        }
      case _ => throw new IllegalArgumentException("Mauvais format de l'article")
    }.toList

    result
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
