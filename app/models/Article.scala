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
                   nbCoeurs: Int = 0,
                   id: Int = -1
                    ) {

}

object Article {

  def create(article: Article): Option[Article] = {

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
        return id(article);
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
      )().collect {
      case CypherRow(id: BigDecimal) => Some(new Article(article.titre,
        article.auteur,
        article.description,
        new DateTime(dateF),
        article.url,
        article.site,
        article.image,
        article.consultationsJour,
        article.consultationsSemaine,
        article.consultationsSemaineDerniere,
        article.consultationsMois,
        article.consultations,
        article.totalEtoiles,
        article.nbEtoiles,
        article.nbCoeurs,
        id.toInt))
      case _ => None
    }.head
  }

  def getById(id: BigDecimal): Option[Article] = {

    val result: List[CypherResultRow] = Cypher(
      """
        Match (article:Article)--(site:Site)
        where ID(article) = {id}
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
                article.nbCoeurs as nbCoeurs,
                ID(article);
      """).on("id" -> id)().toList

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
        nbCoeurs: BigDecimal,
        id: BigDecimal) =>
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
              nbCoeurs.toInt,
              id.toInt))
            case None => None
          }
        case _ => throw new IllegalArgumentException("Mauvais format de l'article")
      }
    }
  }

  def getByUrl(url: String): Option[Article] = {

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
                article.nbCoeurs as nbCoeurs,
                ID(article);
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
        nbCoeurs: BigDecimal,
        id: BigDecimal) =>
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
              nbCoeurs.toInt,
              id.toInt))
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
                        article.nbCoeurs as nbCoeurs,
                        ID(article)
                				ORDER BY date DESC
                        LIMIT 30;
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
      nbCoeurs: BigDecimal,
      id: BigDecimal) =>
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
            nbCoeurs.toInt,
            id.toInt)
          case None => throw new NoSuchElementException("Pas de site pour cette article")
        }
      case _ => throw new IllegalArgumentException("Mauvais format de l'article")
    }.toList

    result
  }

  def delete(id: BigDecimal) = {

    val result: Boolean = Cypher(
      """
Match (article:Article) where ID(article) = {id} delete article;
      """).on("id" -> id).execute()

    println("result : " + result)
    result
  }
}
