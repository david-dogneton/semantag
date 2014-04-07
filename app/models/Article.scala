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
      "date" -> article.date.toString(),
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

  def deleteArticle(url: String) = {

    val result: Boolean = Cypher(
      """
        Match (article:Article) where article.url = {url} delete article;
      """).on("url" -> url).execute()

    println("result : " + result)
    result
  }

  def getArticlesLies(entite: Entite, nbMax: Int): Option[List[(Article, Int)]] = {
    val result: List[(Article, Int)] = Cypher(
      """
        match (entite: Entite {url : {urlEntite}})-[r:tag]-(article: Article)
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
                  tag.quantite as quantiteTag
                order by r.quantite
                limit {nbArticles};
      """).on("urlEntite" -> entite.url, "nbArticles" -> nbMax)().collect {
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
      quantite: BigDecimal) =>
        val siteOpt = Site.get(urlSite)
        siteOpt match {
          case Some(site) => (new Article(
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
            nbCoeurs.toInt), quantite.toInt)
          case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
        }
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  def getEntitesLiees(article: Article): Option[List[Entite]] = {
    val result: List[Entite] = Cypher(
      """
        match (entite: Entite)-[r:tag]-(article: Article {url : {urlArticle}})
                return entite.nom as nom,
                  entite.url as url,
                  entite.apparitionsJour as apparitionsJour,
                  entite.apparitionsSemaine as apparitionsSemaine,
                  entite.apparitionsSemaineDerniere as apparitionsSemaineDerniere,
                  entite.apparitionsMois as apparitionsMois,
                  entite.apparitions as apparitions;
      """).on("urlArticle" -> article.url)().collect {
      case CypherRow(nom: String,
      url: String,
      apparitionsJour: BigDecimal,
      apparitionsSemaine: BigDecimal,
      apparitionsSemaineDerniere: BigDecimal,
      apparitionsMois: BigDecimal,
      apparitions: BigDecimal) =>
        new Entite(nom,
          url,
          apparitionsJour.toInt,
          apparitionsSemaine.toInt,
          apparitionsSemaineDerniere.toInt,
          apparitionsMois.toInt,
          apparitions.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  def getDomainesLies(article: Article): Option[List[Domaine]] = {
    val result: List[Domaine] = Cypher(
      """
        match (article: Article {url : {urlArticle}})-[r:aPourDomaine]->(domaine: Domaine)
                return domaine.nom as nom;
      """).on("urlArticle" -> article.url)().collect {
      case CypherRow(nom: String) =>
        new Domaine(nom)
      case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }
}
