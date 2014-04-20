package models

import org.anormcypher.Cypher

/**
 * Ensemble de fonctions utilitaires
 */
object Utils {

  def remiseAZeroJour(): Boolean = {

    Cypher(
      """
        Match (article:Article)
        SET article.consultationsJour = 0;
      """).execute()
  }

  def delete() = {
    val result: Boolean = Cypher(
      """
        START n = node(*)
        OPTIONAL MATCH n-[r]-()
        WHERE (ID(n)>0)
          DELETE n, r;
      """).execute()
    result
  }

}
