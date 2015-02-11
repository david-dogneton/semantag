package models

import org.anormcypher.{CypherRow, Cypher}
import org.joda.time.DateTime

/**
 * @author David Dogneton, Romain De Oliveira, Maxime Gautré, Thibault Goustat
 *
 *         Classe Article.
 *         Classe décrivant les propriétés d'un article.
 *
 * @param titre titre de l'article
 * @param auteur auteur de l'article
 * @param description description de l'article
 * @param date date de publication de l'article
 * @param url url de l'article
 * @param site site d'où provient l'article
 * @param image image de l'article (si elle existe)
 * @param consultationsJour nombre de consultations par jour de l'article (par défaut 0)
 * @param consultationsSemaine nombre de consultations par semaine de l'article (par défaut 0)
 * @param consultationsSemaineDerniere nombre de consultations de l'article lors de la semaine précédente (par défaut 0)
 * @param consultationsMois nombre de consultations par mois de l'article (par défaut 0)
 * @param consultations nombre de consultations totales de l'article (par défaut 0)
 * @param totalEtoiles nombre total d'étoiles de l'article
 * @param nbEtoiles nombre d'étoiles de l'article
 * @param nbCoeurs nombre de coeurs de l'article
 * @param id identifiant de l'article, il vaut -1 lorsque l'article n'a pas encore été inséré
 *
 */
case class Article(titre: String,
                   auteur: String,
                   description: String,
                   date: DateTime,
                   url: String,
                   site: Site,
                   image: String = "",
                   consultationsJour: Int = 0,
                   consultationsSemaine: Int = 0,
                   consultationsSemaineDerniere: Int = 0,
                   consultationsMois: Int = 0,
                   consultations: Int = 0,
                   totalEtoiles: Int = 0,
                   nbEtoiles: Int = 0,
                   nbCoeurs: Int = 0,
                   id: Int = -1
                    ) {

}

/**
 * Objet compagnon d'un article.
 *
 * Elle contient des méthodes d'insertions, de suppression d'un article.
 */
object Article {

  /**
   * Insertion d'un article dans la base de données.
   *
   * Cet article contient les propriétés du site auquel il est rattaché. Avant l'insertion, il n'a pas d'id.
   * Après l'insertion, l'article aura un identifiant à condition que l'article soit bien inséré.
   *
   * @param article article que l'on souhaite insérer
   * @return On renvoie l'article avec son identifiant s'il s'est bien inséré, sinon on renvoie None.
   */
  def insert(article: Article): Option[Article] = {

    val dateF = article.date.toString()

    Cypher(
      """
        match (site : Site)
        where ID(site) = {idSite}
        create unique (article: Article {
          titre: {titre},
          auteur: {auteur},
          description: {description},
          date: {date},
          url: {url},
          image: {image},
          consultationsJour: {consultationsJour},
          consultationsSemaine: {consultationsSemaine},
          consultationsSemaineDerniere: {consultationsSemaineDerniere},
          consultationsMois: {consultationsMois},
          consultations: {consultationsMois},
          totalEtoiles: {totalEtoiles},
          nbEtoiles: {nbEtoiles},
          nbCoeurs: {nbCoeurs}
        })-[r: appartient]->(site)
        return id(article);
      """
    ).on("titre" -> article.titre,
      "auteur" -> article.auteur,
      "description" -> article.description,
      "date" -> dateF,
      "url" -> article.url,
      "image" -> article.image,
      "consultationsJour" -> article.consultationsJour,
      "consultationsSemaine" -> article.consultationsSemaine,
      "consultationsSemaineDerniere" -> article.consultationsSemaineDerniere,
      "consultationsMois" -> article.consultationsMois,
      "consultations" -> article.consultations,
      "totalEtoiles" -> article.totalEtoiles,
      "nbEtoiles" -> article.nbEtoiles,
      "nbCoeurs" -> article.nbCoeurs,
      "idSite" -> article.site.id
    )().collect {
      case CypherRow(id: BigDecimal) => Some(new Article(article.titre,
        article.auteur,
        article.description,
        new DateTime(dateF),
        article.url,
        article.site,
        article.image,
        article.consultationsJour,
        article.consultationsSemaine,
        article.consultationsSemaineDerniere,
        article.consultationsMois,
        article.consultations,
        article.totalEtoiles,
        article.nbEtoiles,
        article.nbCoeurs,
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
   * @return Renvoie la liste des articles ayant respecté les conditions de la requête
   */
  def getArticles[A](args: (String, A), criteria: String, restriction: String): Stream[Option[Article]] = {

    Cypher(
      """
        Match (site:Site)--(article:Article)
      """ + criteria + """
        return  article.titre,
                article.auteur,
                article.description,
                article.date,
                article.url,
                site.url,
                site.nom,
                site.type,
                ID(site),
                article.image,
                article.consultationsJour,
                article.consultationsSemaine,
                article.consultationsSemaineDerniere,
                article.consultationsMois,
                article.consultations,
                article.totalEtoiles,
                article.nbEtoiles,
                article.nbCoeurs,
                ID(article)
                       """ + restriction).on(args)().collect {

      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      nomSite: String,
      typeSite: String,
      idSite: BigDecimal,
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
        Some(new Article(
          titre,
          auteur,
          description,
          new DateTime(date),
          url,
          new Site(urlSite, nomSite, typeSite, idSite.toInt),
          image,
          consultationsJour.toInt,
          consultationsSemaine.toInt,
          consultationsSemaineDerniere.toInt,
          consultationsMois.toInt,
          consultations.toInt,
          totalEtoiles.toInt,
          nbEtoiles.toInt,
          nbCoeurs.toInt,
          id.toInt))
      case _ => None
    }
  }

  /**
   * Méthode incrémentant le nombre de consultations d'un article (du jour, de la semaine, du mois et au total).
   * @param url URL de l'article à modifier
   * @return Some(article) ou None, en fonction de si l'article a été trouvé en BDD ou non
   */
  def incrNbConsultations(url: String): Option[Article] = {

    val result: List[Article] = Cypher(
      """
        Match (site:Site)--(article:Article {url: {url}})
        set article.consultationsJour = article.consultationsJour + 1,
                article.consultationsSemaine = article.consultationsSemaine + 1,
                article.consultationsMois = article.consultationsMois + 1,
                article.consultations = article.consultations + 1
        return  article.titre as titre,
                article.auteur as auteur,
                article.description as description,
                article.date as date,
                article.url as url,
                site.url as urlSite,
                site.nom as nomSite,
                site.type as typeSite,
                ID(site) as idSite,
                article.image as image,
                article.consultationsJour as cJ,
                article.consultationsSemaine as cS,
                article.consultationsSemaineDerniere as cSD,
                article.consultationsMois as cM,
                article.consultations as coS,
                article.totalEtoiles as totE,
                article.nbEtoiles as nbE,
                article.nbCoeurs as nbC,
                ID(article) as idArticle
      """).on("url" -> url)().collect {
      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      nomSite: String,
      typeSite: String,
      idSite: BigDecimal,
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
          new Site(urlSite, nomSite, typeSite, idSite.toInt),
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
      case _ => throw new IllegalArgumentException("Mauvais format pour l'article")
    }.toList

    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }

  /**
   * Méthode incrémentant le nombre de coeurs d'un article.
   * @param url URL de l'article à modifier
   * @return Some(article) ou None, en fonction de si l'article a été trouvé en BDD ou non
   */
  def incrNbCoeurs(url: String): Option[Article] = {

    val result: List[Article] = Cypher(
      """
        Match (site:Site)--(article:Article {url: {url}})
        set article.nbCoeurs = article.nbCoeurs + 1
        return  article.titre as titre,
                article.auteur as auteur,
                article.description as description,
                article.date as date,
                article.url as url,
                site.url as urlSite,
                site.nom as nomSite,
                site.type as typeSite,
                ID(site) as idSite,
                article.image as image,
                article.consultationsJour as cJ,
                article.consultationsSemaine as cS,
                article.consultationsSemaineDerniere as cSD,
                article.consultationsMois as cM,
                article.consultations as coS,
                article.totalEtoiles as totE,
                article.nbEtoiles as nbE,
                article.nbCoeurs as nbC,
                ID(article) as idArticle
      """).on("url" -> url)().collect {
      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      nomSite: String,
      typeSite: String,
      idSite: BigDecimal,
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
          new Site(urlSite, nomSite, typeSite, idSite.toInt),
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
      case _ => throw new IllegalArgumentException("Mauvais format pour l'article")
    }.toList

    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }



  /**
   * Méthode décrémentant le nombre de coeurs d'un article.
   * @param url URL de l'article à modifier
   * @return Some(article) ou None, en fonction de si l'article a été trouvé en BDD ou non
   */
  def decrNbCoeurs(url: String): Option[Article] = {

    val result: List[Article] = Cypher(
      """
        Match (site:Site)--(article:Article {url: {url}})
        set article.nbCoeurs = article.nbCoeurs - 1
        return  article.titre as titre,
                article.auteur as auteur,
                article.description as description,
                article.date as date,
                article.url as url,
                site.url as urlSite,
                site.nom as nomSite,
                site.type as typeSite,
                ID(site) as idSite,
                article.image as image,
                article.consultationsJour as cJ,
                article.consultationsSemaine as cS,
                article.consultationsSemaineDerniere as cSD,
                article.consultationsMois as cM,
                article.consultations as coS,
                article.totalEtoiles as totE,
                article.nbEtoiles as nbE,
                article.nbCoeurs as nbC,
                ID(article) as idArticle
      """).on("url" -> url)().collect {
      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      nomSite: String,
      typeSite: String,
      idSite: BigDecimal,
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
          new Site(urlSite, nomSite, typeSite, idSite.toInt),
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
      case _ => throw new IllegalArgumentException("Mauvais format pour l'article")
    }.toList

    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }


  /**
   * Renvoie l'article correspondant à l'identifiant passé en paramètre.
   * On se sert de la méthode générique "getArticles" pour pouvoir récupérer l'article. Comme l'identifiant est unique,
   * on récupère le premier élément du stream renvoyé par la méthode getArticles
   *
   * @param id identifiant de l'article
   * @return Renvoie l'article correspondant à l'identifiant passé en paramètre.
   */
  def getById(id: Int): Option[Article] = {
    val stream = getArticles("param" -> id, "where ID(article) = {param}", ";")
    if (stream.isEmpty)
      None
    else
      stream.head
  }

  /**
   * Renvoie l'article correspondant à l'url passée en paramètre.
   * On se sert de la méthode générique "getArticles" pour pouvoir récupérer l'article. Comme l'url est unique,
   * on récupère le premier élément du stream renvoyé par la méthode getArticles
   * @param url url de l'article
   * @return Renvoie l'article correspondant à l'url passée en paramètre.
   */
  def getByUrl(url: String): Option[Article] = {

    val stream = getArticles("param" -> url, "where article.url = {param}", ";")
    if (stream.isEmpty)
      None
    else
      stream.head
  }


  /**
   * Méthode qui recherche la chaîne passée en paramètre dans le titre de chaque article de la base de données.
   * On cherche si le paramètre est contenu dans le titre (équivalent d'un contains). On transforme d'abord la chaîne en minuscule
   * pour ne pas tenir compte de la casse.
   *
   * @param rechercheUtilisateur chaîne de caractères à rechercher
   * @return Renvoie la liste des articles qui contiennent l'élément recherché dans leur titre.
   */
  def rechercheDansTitre(rechercheUtilisateur: String): List[Article] = {

    val critereRecherche = ".*" + rechercheUtilisateur.toLowerCase + ".*"
    val result: List[Option[Article]] = getArticles("param" -> critereRecherche, "where lower(article.titre) =~ {param} ", " ORDER BY article.date DESC ;").toList
    result map {
      case Some(article) => article
      case None => throw new NoSuchElementException("article vide")
    }
  }

  /**
   *
   * @return Récupère les 30 articles les plus récents
   */
  def getLastArticle: List[Article] = {

    val result = getArticles("" -> "", "", "ORDER BY article.date DESC LIMIT 100;").toList
    result.map(_.get)
  }

  /**
   * Méthode de suppresion d'un article
   * @param id identifiant de l'article
   * @return Vrai si la requête s'est bien déroulée, faux sinon.
   */
  def delete(id: Int) = {

    val result: Boolean = Cypher(
      """
Match (article:Article) where ID(article) = {id} delete article;
      """).on("id" -> id).execute()

    println("result : " + result)
    result
  }

  /**
   * Liste toutes les entités liées à un article.
   * @param article L'article à étudier.
   * @return Some d'une liste d'entités (celles liées à l'article), ou None si l'article n'a pas été trouvé.
   */
  def getEntitesLiees(article: Article): Option[List[Entite]] = {
    val result: List[Entite] = Cypher(
      """
        match (entite: Entite)-[r:tag]-(article: Article {url : {url}})
                return entite.nom as nom,
                  entite.url as url,
                  entite.apparitionsJour as apparitionsJour,
                  entite.apparitionsSemaine as apparitionsSemaine,
                  entite.apparitionsSemaineDerniere as apparitionsSemaineDerniere,
                  entite.apparitionsMois as apparitionsMois,
                  entite.apparitions as apparitions;
      """).on("url" -> article.url)().collect {
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

  /**
   * Liste tous les domaines liées à un article.
   * @param article L'article à étudier.
   * @return Some d'une liste de domaines (ceux liés à l'article), ou None si l'article n'a pas été trouvé.
   */
  def getDomainesLies(article: Article): Option[List[Domaine]] = {
    val result: List[Domaine] = Cypher(
      """
        match (article: Article {url : {url}})-[r:aPourDomaine]->(domaine: Domaine)
                return domaine.nom as nom;
      """).on("url" -> article.url)().collect {
      case CypherRow(nom: String) =>
        new Domaine(nom)
      case _ => throw new IllegalArgumentException("Mauvais format du domaine")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  /**
   * Renvoie les 5 articles les plus consultés du jour
   */
  def lesPlusConsulteesJour() {
    val result: List[Option[Article]] = getArticles("" -> "", "", "ORDER BY article.consultationsJour DESC Limit 5;").toList
    result.map(_.get)
  }

  /**
   * Renvoie les 5 articles les plus consultés de la semaine
   */
  def lesPlusConsulteesSemaine() {

    val result: List[Option[Article]] = getArticles("" -> "", "", "ORDER BY article.consultationsSemaine DESC Limit 5;").toList
    result.map(_.get)
  }

  /**
   * Renvoie les 5 artciles les plus consultés de la semaine dernière
   */
  def lesPlusConsulteesSemaineDerniere() {

    val result: List[Option[Article]] = getArticles("" -> "", "", "ORDER BY article.consultationsSemaineDerniere DESC Limit 5;").toList
    result.map(_.get)
  }

  /**
   * Renvoie les 5 articles les plus consultés du mois
   */
  def lesPlusConsulteesMois() {

    val result: List[Option[Article]] = getArticles("" -> "", "", "ORDER BY article.consultationsMois DESC Limit 5;").toList
    result.map(_.get)
  }

  /**
   * Renvoie les 5 articles les plus consultés toute période confondue
   */
  def lesPlusConsultees() {

    val result: List[Option[Article]] = getArticles("" -> "", "", "ORDER BY article.consultations DESC Limit 5;").toList
    result.map(_.get)
  }
}
