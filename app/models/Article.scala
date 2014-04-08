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
        where ID(site) = {idSite}
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
        "idSite" -> article.site.id
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

  private def getArticles[A](args: (String, A), criteria : String, restriction : String): Stream[Option[Article]] = {

    Cypher(
      """
        Match (article:Article)--(site:Site)
        """+ criteria +"""
        return  article.titre,
                article.auteur,
                article.description,
                article.date,
                article.url,
                site.url,
                site.nom,
                site.type,
                ID(site),
                article.image,
                article.consultationsJour,
                article.consultationsSemaine,
                article.consultationsSemaineDerniere,
                article.consultationsMois,
                article.consultations,
                article.totalEtoiles,
                article.nbEtoiles,
                article.nbCoeurs,
                ID(article)
      """+restriction).on(args)().collect {

      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      nomSite: String,
      typeSite: String,
      idSite: BigDecimal,
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
        Some(new Article(
          titre,
          auteur,
          description,
          new DateTime(date),
          url,
          new Site(urlSite, nomSite, typeSite, idSite.toInt),
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
      case _ => None
    }
  }

  def getById(id: Int): Option[Article] = {
    val stream = getArticles("param" -> id, "where ID(article) = {param}",";")
    if(stream.isEmpty)
      None
    else
      stream.head
  }

  def getByUrl(url: String): Option[Article] = {

    val stream = getArticles(("param" -> url), "where article.url = {param}", ";")
    if(stream.isEmpty)
      None
    else
      stream.head
  }

  def getLastArticle(): List[Article] = {

    val result = getArticles(("" -> ""), "", "ORDER BY article.date DESC LIMIT 30;").toList
    result.map(_.get)
  }

  def delete(id: BigDecimal) = {

    val result: Boolean = Cypher(
      """
Match (article:Article) where ID(article) = {id} delete article;
      """).on("id" -> id).execute()

    println("result : " + result)
    result
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
      case _ => throw new IllegalArgumentException("Mauvais format du domaine")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }
}
