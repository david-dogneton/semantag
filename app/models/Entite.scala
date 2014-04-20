package models

import org.anormcypher.{CypherResultRow, CypherRow, Cypher}

/**
 * @author David Dogneton, Romain De Oliveira, Maxime Gautré, Thibault Goustat
 *
 *         Classe Entité.
 *         Classe décrivant les propriétés d'une entité aussi appelée tag.
 *
 * @param nom nom de l'entité
 * @param url url de l'entite, permet d'obtenir plus de résultat avec DBpedia
 * @param apparitionsJour nombre d'apparitions dans la journée
 * @param apparitionsSemaine nombre d'apparitions dans la semaine
 * @param apparitionsSemaineDerniere nombre d'apparitions durant la semaine précédente
 * @param apparitionsMois nombre d'apparitions dans le mois
 * @param apparitions nombre d'apparitions toute période confondue
 * @param id identifiant unique d'une entité
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

/**
 * Objet compagnon d'une entité.
 *
 * Elle contient des méthodes d'insertion, de suppression d'une entité.
 */
object Entite {

  /**
   * Méthode d'insertion d'une entité dans la base de données
   *
   * Avant l'insertion, l'entité n'a pas d'id.
   * Après l'insertion, l'entité aura un identifiant à condition que l'entité soit bien insérée.
   *
   * @param entite l'entité à insérer
   * @return On renvoie l'entité avec son identifiant si elle s'est bien insérée, sinon on renvoie None.
   */
  def insert(entite: Entite): Option[Entite] = {

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

  /**
   * Méthode générique qui exécute une requête en prenant en paramètre, un argument, un critère de recherche et une restriction
   *
   * @param args argument de la requête
   * @param criteria clause where de la requête
   * @param restriction clause de limitation ou de tri
   * @tparam A le type de l'argument peut être n'importe quoi (un entier, un double, un string)
   * @return Renvoie la liste des entités ayant respecté les conditions de la requête
   */
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

  /**
   * Méthode qui récupère une entité par son identifiant
   * On se sert de la méthode générique "getEntites" pour pouvoir récupérer l'entité. Comme l'identifiant est unique,
   * on récupère le premier élément du stream renvoyé par la méthode getEntite
   *
   * @param id identifiant de l'entité
   * @return L'entité si elle existe
   */
  def getById(id: Int): Option[Entite] = {

    val result = getEntites("param"-> id, "where ID(entite) = {param}",";")
    if(result.isEmpty)
      None
    else
      result.head
  }

  /**
   * Méthode qui récupère une entité par son url
   * On se sert de la méthode générique "getEntites" pour pouvoir récupérer l'entité. Comme l'url est unique,
   * on récupère le premier élément du stream renvoyé par la méthode getEntite
   *
   * @param url identifiant de l'entité
   * @return L'entité si elle existe
   */
  def getByUrl(url: String): Option[Entite] = {
    val result = getEntites("param"-> url, "where entite.url = {param}",";")
    if(result.isEmpty)
      None
    else
      result.head
  }

  /**
   * Recherche à partir d'une chaîne de caractère si une entité existe
   *
   * On cherche si le paramètre est contenu dans le nom de l'entité (équivalent d'un contains). On transforme d'abord la chaîne en minuscule
   * pour ne pas tenir compte de la casse.
   *
   * @param rechercheUtilisateur chaîne de caractères à rechercher
   * @return Renvoie la liste des entités qui contiennent l'élément recherché dans leur nom.
   *
   */
  def rechercheDansNom(rechercheUtilisateur : String): List[Entite] = {

    val critereRecherche = ".*"+rechercheUtilisateur.toLowerCase+".*"
    val result: List[Option[Entite]] = getEntites("param" -> critereRecherche , "where lower(entite.nom) =~ {param} ",";").toList
    result map {
      case Some(entite) => entite
      case None => throw new NoSuchElementException("entité vide")
    }
  }

  /**
   * @return Renvoie la liste des dix entités les plus présentes dans la journée dans des articles
   */
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

  /**
   * Augmente de 1 tous les champs "apparitions" d'une entité lorsqu'un artcile possède cette entité
   * @param entite l'entité sur laquelle nous incrémentons ses apparitions
   * @return On renvoie un stream
   */
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
