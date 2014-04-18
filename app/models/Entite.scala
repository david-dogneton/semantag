package models

import org.anormcypher.{CypherResultRow, CypherRow, Cypher}

/**
 * Created by Administrator on 18/03/14.
 */
case class Entite(nom: String,
                  url: String,
                  apparitionsJour: Int = 0,
                  apparitionsSemaine: Int = 0,
                  apparitionsSemaineDerniere: Int = 0,
                  apparitionsMois: Int = 0,
                  apparitions: Int = 0,
                  id : Int = -1) {

}

object Entite {

  def create(entite: Entite): Option[Entite] = {

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
        return id(entite);
      """
    ).on("nom" -> entite.nom,
        "url" -> entite.url,
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

  private def getEntites[A](args : (String, A), criteria: String, restriction: String): Stream[Option[Entite]] = {

    Cypher(
      """
        Match  (entite:Entite) """+criteria+"""
        return  distinct entite.nom,
                entite.url,
                entite.apparitionsJour,
                entite.apparitionsSemaine,
                entite.apparitionsSemaineDerniere,
                entite.apparitionsMois,
                entite.apparitions,
                ID(entite)
      """+restriction).on(args)().collect {
      case CypherRow(nom: String,
      url: String,
      apparitionsJour: BigDecimal,
      apparitionsSemaine: BigDecimal,
      apparitionsSemaineDerniere: BigDecimal,
      apparitionsMois: BigDecimal,
      apparitions: BigDecimal,
      id: BigDecimal) =>
        Some(new Entite(nom,
          url,
          apparitionsJour.toInt,
          apparitionsSemaine.toInt,
          apparitionsSemaineDerniere.toInt,
          apparitionsMois.toInt,
          apparitions.toInt,
          id.toInt))
      case _ => None
    }
  }

  def getById(id: Int): Option[Entite] = {

    val result = getEntites("param"-> id, "where ID(entite) = {param}",";")
    if(result.isEmpty)
      None
    else
      result.head
  }

  def getByUrl(url: String): Option[Entite] = {
    val result = getEntites("param"-> url, "where entite.url = {param}",";")
    if(result.isEmpty)
      None
    else
      result.head
  }

  def rechercheDansNom(rechercheUtilisateur : String): List[Entite] = {
    val critereRecherche = ".*"+rechercheUtilisateur.toLowerCase+".*"
    val result: List[Option[Entite]] = getEntites("param" -> critereRecherche , "where lower(entite.nom) =~ {param} ",";").toList
    result map {
      case Some(entite) => entite
      case None => throw new NoSuchElementException("article vide")
    }
  }

  def getTopEntites(user: Utilisateur, nbEntites: Int): Option[List[Entite]] = {
    val result: List[Entite] = Cypher(
      """
        match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite)
                 return entite.nom as nom,
                 entite.url as url,
                 entite.apparitionsJour as apparitionsJour,
                 entite.apparitionsSemaine as apparitionsSemaine,
                 entite.apparitionsSemaineDerniere as apparitionsSemaineDerniere,
                 entite.apparitionsMois as apparitionsMois,
                 entite.apparitions as apparitions
                 order by r.quantite
                 limit {nbEntites};
      """).on("mailUser" -> user.mail, "nbEntites" -> nbEntites)().collect {
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

  def lesPlusTaggesDuJour(): List[Entite] = {
    val result = getEntites("" -> "", "", "ORDER BY entite.apparitionsJour DESC Limit 10;").toList
    result.map(_.get)
  }

  def topAnnotations(nombre:Int, idTypeEntite:Int): List[Entite] = {
    idTypeEntite match{
      case -1 =>
        val result = getEntites("" -> "", "", "ORDER BY entite.apparitions DESC Limit "+nombre+";").toList
        result.map(_.get)
      case _ =>
        val result = getEntites("" -> "", "-[aPourType]->(type) where ID(type) = "+idTypeEntite, "ORDER BY entite.apparitions DESC Limit "+nombre+";").toList
        result.map(_.get)
    }
  }

  def topAnnotationsMois(nombre:Int, idTypeEntite:Int): List[Entite] = {
    idTypeEntite match{
      case -1 =>
        val result = getEntites("" -> "", "", "ORDER BY entite.apparitionsMois DESC Limit "+nombre+";").toList
        result.map(_.get)
      case _ =>
        val result = getEntites("" -> "", "-[aPourType]->(type) where ID(type) = "+idTypeEntite, "ORDER BY entite.apparitionsMois DESC Limit "+nombre+";").toList
        result.map(_.get)
    }
  }

  def topAnnotationsSemaine(nombre:Int, idTypeEntite:Int): List[Entite] = {
    idTypeEntite match{
      case -1 =>
        val result = getEntites("" -> "", "", "ORDER BY entite.apparitionsSemaine DESC Limit "+nombre+";").toList
        result.map(_.get)
      case _ =>
        val result = getEntites("" -> "", "-[aPourType]->(type) where ID(type) = "+idTypeEntite, "ORDER BY entite.apparitionsSemaine DESC Limit "+nombre+";").toList
        result.map(_.get)
    }
  }

  def topAnnotationsJour(nombre:Int, idTypeEntite:Int): List[Entite] = {
    idTypeEntite match{
      case -1 =>
        val result = getEntites("" -> "", "", "ORDER BY entite.apparitionsJour DESC Limit "+nombre+";").toList
        result.map(_.get)
      case _ =>
        val result = getEntites("" -> "", "-[aPourType]->(type) where ID(type) = "+idTypeEntite, "ORDER BY entite.apparitionsJour DESC Limit "+nombre+";").toList
        result.map(_.get)
    }
  }

  def incrApparitions(entite : Entite): Stream[CypherResultRow] = {
    Cypher(
      """
        Match (entite:Entite) where ID(entite) = {id}
        set entite.apparitionsJour = entite.apparitionsJour + 1,
            entite.apparitionsSemaine = entite.apparitionsSemaine + 1,
            entite.apparitionsSemaineDerniere = entite.apparitionsSemaineDerniere + 1,
            entite.apparitionsMois = entite.apparitionsMois + 1,
            entite.apparitions = entite.apparitions + 1
        return  entite.nom,
                entite.url,
                entite.apparitionsJour,
                entite.apparitionsSemaine,
                entite.apparitionsSemaineDerniere,
                entite.apparitionsMois,
                entite.apparitions,
                ID(entite);
      """).on("id" -> entite.id)()
  }
}
