package models

import org.anormcypher.{CypherRow, Cypher}
import org.joda.time.DateTime

/**
 * Created by Administrator on 17/03/14.
 */
case class Site(url: String, nom: String, typeSite: String, id: Int = -1) {}

object Site {

  def create(site: Site): Option[Site] = {
    Cypher(
      """
        create (site: Site{
          url: {url},
          nom: {nom},
          type: {type}
        })
        return ID(site);
      """
    ).on("url" -> site.url,
        "nom" -> site.nom,
        "type" -> site.typeSite
      )().collect {
      case CypherRow(id : BigDecimal) => Some(new Site(site.url, site.nom, site.typeSite, id.toInt))
      case _ => None
    }.head
  }

  def getById(id : Int): Option[Site] = {

    val result: List[Site] = Cypher(
      """
        Match (site:Site) where ID(site) = {id}
        return  site.url,
                site.nom,
                site.type,
                ID(site);
      """).on("id" -> id)().collect {
      case CypherRow(url: String, nom: String, typeSite: String, id: BigDecimal) => new Site(url, nom, typeSite, id.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }

  def getByUrl(url: String): Option[Site] = {

    val result: List[Site] = Cypher(
      """
        Match (site:Site) where site.url = {url}
        return  site.url,
                site.nom,
                site.type,
                ID(site);
      """).on("url" -> url)().collect {
      case CypherRow(url: String, nom: String, typeSite: String, id: BigDecimal) => new Site(url, nom, typeSite, id.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }

  def getAll(): List[Site] = {

    val result: List[Site] = Cypher(
      """
        Match (site:Site)
        return  site.url as url,
                site.nom as nom,
                site.type as type,
                ID(site);
      """)().collect {
      case CypherRow(url: String, nom: String, typeSite: String, id: BigDecimal) => new Site(url, nom, typeSite, id.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result
  }

  def getTypes(): List[Site] = {

    val result: List[Site] = Cypher(
      """
        Match (site:Site)
        return distinct site.type as type ORDER BY site.type;
      """)().collect {
      case CypherRow(typeSite: String) => new Site("", "", typeSite)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result
  }

  def delete(url: String): Boolean = {
    val result: Boolean = Cypher(
      """
        Match (site:Site) where site.url = {url} delete site;
      """).on("url" -> url).execute()

    println("result : " + result)
    result
  }


  def getTopSites(user: Utilisateur, nbSites: Int): Option[List[Site]] = {
    val result: List[Site] = Cypher(
      """
        match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site)
                 return  site.url as url,
                        site.nom as nom,
                        site.type as type
                 order by r.nbCoeurs
                 limit {nbSites};
      """).on("mailUser" -> user.mail, "nbSites" -> nbSites)().collect {
      case CypherRow(url: String, nom: String, typeSite: String) => new Site(url, nom, typeSite)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }


  def getAllArticles(site: Site): Option[List[Article]] = {
    val result: List[Article] = Cypher(
      """
      Match (article:Article)--(site:Site {url: {urlSite}})
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
      """).on("urlSite" -> site.url)().collect {
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
        new Article(
          titre,
          auteur,
          description,
          new DateTime(date),
          url,
          site,
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
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }
}
