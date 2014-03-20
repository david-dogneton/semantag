package models

import org.anormcypher.Cypher

/**
 * Created by Administrator on 20/03/14.
 */
case class APourDomaine(article: Article, domaine: Domaine) {

}

object APourDomaine {

  def create(article: Article, domaine: Domaine): Boolean = {
    Cypher(
      """
         match (article: Article), (domaine: Domaine)
         where article.url = {url} and domaine.nom = {nom}
         create (article)-[r:aPourDomaine]->(domaine)
      """
    ).on("url" -> article.url,
        "nom" -> domaine.nom
      ).execute()
  }

  def create(aPourDomaine: APourDomaine): Boolean = {
     create(aPourDomaine.article, aPourDomaine.domaine)
  }
}
