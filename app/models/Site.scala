package models

import org.anormcypher.{CypherRow, Cypher}

/**
 * Created by Administrator on 17/03/14.
 */
case class Site(url: String, nom: String, typeSite: String) {}

object Site {

  def create(site: Site): Boolean = {
    Cypher(
      """
        create (site: Site{
          url: {url},
          nom: {nom},
          type: {type}
        })
      """
    ).on("url" -> site.url,
        "nom" -> site.nom,
        "type" -> site.typeSite
      ).execute()
  }

  def get(url: String): Site = {

    val result: List[Site] = Cypher(
      """
        Match (site:Site) where site.url = {url}
        return  site.url as url,
                site.nom as nom,
                site.type as type;
      """).on("url" -> url)().collect {
      case CypherRow(url: String, nom: String, typeSite: String) => new Site(url, nom, typeSite)
      case _ => throw new IllegalArgumentException("Mauvais format du site")
    }.toList

    result match {
      case Nil => throw new NoSuchElementException("Site not found")
      case head::tail =>                     head
    }
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
