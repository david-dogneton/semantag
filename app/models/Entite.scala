package models

import org.anormcypher.Cypher

/**
 * Created by Administrator on 18/03/14.
 */
case class Entite(nom : String,
                  url : String,
                  apparitionsJour: String,
                  apparitionsSemaine: String,
                  apparitionsSemaineDerniere: String,
                  apparitionsMois: String,
                  apparitions: String) {

}

object Entite {

  def create(entite: Entite) = {
    Cypher(
      """
        create (article: Article {
          url: {url},
          nom: {nom};
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


}
