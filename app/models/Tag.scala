package models

import org.anormcypher.{CypherRow, Cypher}
import org.joda.time.DateTime

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
         where ID(article) = {idArticle} and ID(entite) = {idEntite}
         create (article)<-[r:tag {quantite : {quantite}}]-(entite)
      """
    ).on("idArticle" -> article.id,
        "idEntite" -> entite.id,
        "quantite" -> quantite
      ).execute()
  }

  def createTagAndEntity(article: Article, entite: Entite, quantite: Int): Option[Entite] = {
    Cypher(
      """
         match (article: Article)
         where ID(article) = {idArticle}
         create (article)<-[r:tag {quantite : {quantite}}]-(entite: Entite{
                 nom: {nom},
                 url: {urlEnt},
                 apparitionsJour: {apparitionsJour},
                 apparitionsSemaine: {apparitionsSemaine},
                 apparitionsSemaineDerniere: {apparitionsSemaineDerniere},
                 apparitionsMois: {apparitionsMois},
                 apparitions: {apparitions}
                })
         return ID(entite)
      """
    ).on("idArticle" -> article.id,
        "quantite" -> quantite,
        "nom" -> entite.nom,
        "urlEnt" -> entite.url,
        "apparitionsJour" -> entite.apparitionsJour,
        "apparitionsSemaine" -> entite.apparitionsSemaine,
        "apparitionsSemaineDerniere" -> entite.apparitionsSemaineDerniere,
        "apparitionsMois" -> entite.apparitionsMois,
        "apparitions" -> entite.apparitions
      )().collect {
      case CypherRow(id: BigDecimal) =>
        Some(new Entite(entite.nom,
          entite.url,
          entite.apparitionsJour,
          entite.apparitionsSemaine,
          entite.apparitionsSemaineDerniere,
          entite.apparitionsMois,
          entite.apparitions,
          id.toInt))
      case _ => None
    }.head
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
                r.quantite,
                ID(entite);
      """
    ).on("urlArt" -> article.url)().collect {
      case CypherRow(nom: String,
      url: String,
      apparitionsJour: BigDecimal,
      apparitionsSemaine: BigDecimal,
      apparitionsSemaineDerniere: BigDecimal,
      apparitionsMois: BigDecimal,
      apparitions: BigDecimal,
      quantite: BigDecimal,
      id: BigDecimal) =>
        (new Entite(nom,
          url,
          apparitionsJour.toInt,
          apparitionsSemaine.toInt,
          apparitionsSemaineDerniere.toInt,
          apparitionsMois.toInt,
          apparitions.toInt,
          id.toInt),
          quantite.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
    }.toList
  }

  def getArticlesLies(entite: Entite, nbMax: Int): Option[List[(Article, Int)]] = {
    val result: List[(Article, Int)] = Cypher(
      """
        match (entite: Entite {url : {urlEntite}})-[r:tag]-(article: Article)--(site:Site)
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
                  ID(article),
                  r.quantite as quantiteTag
                ORDER BY article.date DESC
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
      id : BigDecimal,
      quantite: BigDecimal) =>
        val siteOpt = Site.getByUrl(urlSite)
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
            nbCoeurs.toInt,
            id.toInt), quantite.toInt)
          case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
        }
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  def getNombreArticlesLies(entite: Entite): Int = {
    Cypher(
      """
        match (entite: Entite)-[r:tag]-(article: Article)--(site:Site)
                where ID(entite) = {id}
                return  count(r);
      """).on("id" -> entite.id)().collect {
      case CypherRow(count : BigDecimal) => count.toInt
      case _ => -1
    }.head
  }
}
