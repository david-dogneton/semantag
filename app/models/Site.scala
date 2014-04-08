package models

import org.anormcypher.{CypherRow, Cypher}

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
        return distinct site.type as type;
      """)().collect {
      case CypherRow(typeSite: String) => new Site("", "", typeSite)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result
  }

  def delete(url : String): Boolean = {
    val result: Boolean = Cypher(
      """
        Match (site:Site) where site.url = {url} delete site;
      """).on("url" -> url).execute()

    println("result : " + result)
    result
  }
}
