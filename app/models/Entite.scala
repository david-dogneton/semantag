package models

import org.anormcypher.{CypherRow, Cypher}

/**
 * Created by Administrator on 18/03/14.
 */
case class Entite(nom: String,
                  url: String,
                  apparitionsJour: Int = 0,
                  apparitionsSemaine: Int = 0,
                  apparitionsSemaineDerniere: Int = 0,
                  apparitionsMois: Int = 0,
                  apparitions: Int = 0) {

}

object Entite {

  def create(entite: Entite): Boolean = {
    Cypher(
      """
        create (entite: Entite{
          nom: {nom},
          url: {url},
          apparitionsJour: {apparitionsJour},
          apparitionsSemaine: {apparitionsSemaine},
          apparitionsSemaineDerniere: {apparitionsSemaineDerniere},
          apparitionsMois: {apparitionsMois},
          apparitions: {apparitions}
        })
      """
    ).on("nom" -> entite.nom,
        "url" -> entite.url,
        "apparitionsJour" -> entite.apparitionsJour,
        "apparitionsSemaine" -> entite.apparitionsSemaine,
        "apparitionsSemaineDerniere" -> entite.apparitionsSemaineDerniere,
        "apparitionsMois" -> entite.apparitionsMois,
        "apparitions" -> entite.apparitions
      ).execute()
  }

  def get(url: String): Option[Entite] = {

    val result: List[Entite] = Cypher(
      """
        Match (entite:Entite) where entite.url = {url}
        return  entite.nom as nom,
                entite.url as url,
                entite.apparitionsJour as apparitionsJour,
                entite.apparitionsSemaine as apparitionsSemaine,
                entite.apparitionsSemaineDerniere as apparitionsSemaineDerniere,
                entite.apparitionsMois as apparitionsMois,
                entite.apparitions as apparitions;
      """).on("url" -> url)().collect {
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
      case head :: tail => Some(head)
    }
  }
}
