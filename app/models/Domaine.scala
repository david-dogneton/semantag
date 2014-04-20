package models

import org.anormcypher.{CypherRow, Cypher}

case class Domaine(nom : String) {

}

object Domaine {

  def create(domaine: Domaine): Boolean = {
    Cypher(
      """
        create (domaine: Domaine {
          nom: {nom}
        })
      """
    ).on("nom" -> domaine.nom).execute()
  }

  def get(nom: String): Option[Domaine] = {

    val result: List[Domaine] = Cypher(
      """
        Match (domaine: Domaine) where domaine.nom = {nom}
        return  domaine.nom as nom;
      """).on("nom" -> nom)().collect {
      case CypherRow(nom: String) => new Domaine(nom)
      case _ => throw new IllegalArgumentException("Mauvais format du domaine")
    }.toList

    result match {
      case Nil => None
      case head::tail => Some(head)
    }
  }
}
