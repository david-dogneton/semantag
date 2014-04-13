package models

import org.anormcypher.{CypherRow, CypherResultRow, CypherStatement, Cypher}
import play.Logger
import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 17/03/14
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
case class Utilisateur(mail: String, mdp: String, pseudo: String, nbCoeurs: Int = 0)

object Utilisateur {
  def create(utilisateur: Utilisateur): Boolean = {
    val result: Boolean = Cypher( """
        create (user: Utilisateur {
        mail: {mailDonne},
        mdp: {mdpDonne},
        pseudo: {pseudoDonne},
        nbCoeurs: {nbCoeursDonne}});
                                  """).on("mailDonne" -> utilisateur.mail, "mdpDonne" -> utilisateur.mdp, "pseudoDonne" -> utilisateur.pseudo, "nbCoeursDonne" -> utilisateur.nbCoeurs).execute()

    result
  }

  def get(adresseMail: String): Option[Utilisateur] = {
    val result: List[Utilisateur] = Cypher(
      """
        Match (n:Utilisateur)
        where n.mail = {mailDonne}
        return n.mail as mail,
          n.mdp as mdp,
          n.pseudo as pseudo,
          n.nbCoeurs as nbCoeurs;
      """).on("mailDonne" -> adresseMail)().collect {
      case CypherRow(mail: String,
      mdp: String,
      pseudo: String,
      nbCoeurs: BigDecimal) =>
        new Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt)
      case _ => throw new Exception("Utilisateur not found")
    }.toList
    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }


  def authenticate(adresseMail: String, mdp: String): Option[Utilisateur] = {
    val result: List[Utilisateur] = Cypher(
      """
        Match (n:Utilisateur)
        where n.mail = {mailDonne} and n.mdp = {mdpDonne}
        return n.mail as mail,
          n.mdp as mdp,
          n.pseudo as pseudo,
          n.nbCoeurs as nbCoeurs;
      """).on("mailDonne" -> adresseMail, "mdpDonne" -> mdp)().collect {
      case CypherRow(mail: String,
      mdp: String,
      pseudo: String,
      nbCoeurs: BigDecimal) =>
        new Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt)
      case _ => throw new Exception("Utilisateur not found")
    }.toList
    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }


  def setMail(ancienMail: String, nouveauMail: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher("Match (n:Utilisateur) where n.mail = {mailDonne} set n.mail = {nouveauMail} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> ancienMail, "nouveauMail" -> nouveauMail).apply().head
    result match {
      case CypherRow(mail: String, mdp: String, pseudo: String, nbCoeurs: BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def setPseudo(adresseMail: String, nouveauPseudo: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher("Match (n:Utilisateur) where n.mail = {mailDonne} set n.pseudo = {nouveauPseudo} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail, "nouveauPseudo" -> nouveauPseudo).apply().head
    result match {
      case CypherRow(mail: String, mdp: String, pseudo: String, nbCoeurs: BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def setMdp(adresseMail: String, nouveauMdp: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher("Match (n:Utilisateur) where n.mail = {mailDonne} set n.mdp = {nouveauMdp} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail, "nouveauMdp" -> nouveauMdp).apply().head
    result match {
      case CypherRow(mail: String, mdp: String, pseudo: String, nbCoeurs: BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def incrementerNbCoeurs(adresseMail: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher("Match (n:Utilisateur) where n.mail = {mailDonne} set n.nbCoeurs = n.nbCoeurs + 1 return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail).apply().head
    result match {
      case CypherRow(mail: String, mdp: String, pseudo: String, nbCoeurs: BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def decrementerNbCoeurs(adresseMail: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher("Match (n:Utilisateur) where n.mail = {mailDonne} set n.nbCoeurs = n.nbCoeurs - 1 return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail).apply().head
    result match {
      case CypherRow(mail: String, mdp: String, pseudo: String, nbCoeurs: BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def delete(adresseMail: String): Boolean = {
    val result: Boolean = Cypher("Match (n:Utilisateur) where n.mail = {mailDonne} delete n;").on("mailDonne" -> adresseMail).execute()
    result
  }

  def getArticlesLus(utilisateur: Utilisateur): Option[List[Article]] = {

    val result: List[Article] = Cypher(
      """
      Match (user:Utilisateur {mail: {mailUser}})-[r1:consultation]-(article:Article)--(site:Site)
                return  article.titre as titre,
                        article.auteur as auteur,
                        article.description as description,
                        article.date as date,
                        article.url as url,
                        site.url as urlSite,
                        article.image as image,
                        article.consultationsJour as consultationsJour,
                        article.consultationsSemaine as consultationsSemaine,
                        article.consultationsSemaineDerniere as consultationsSemaineDerniere,
                        article.consultationsMois as consultationsMois,
                        article.consultations as consultations,
                        article.totalEtoiles as totalEtoiles,
                        article.nbEtoiles as nbEtoiles,
                        article.nbCoeurs as nbCoeurs;
      """).on("mailUser" -> utilisateur.mail)().collect {
      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      image: String,
      consultationsJour: BigDecimal,
      consultationsSemaine: BigDecimal,
      consultationsSemaineDerniere: BigDecimal,
      consultationsMois: BigDecimal,
      consultations: BigDecimal,
      totalEtoiles: BigDecimal,
      nbEtoiles: BigDecimal,
      nbCoeurs: BigDecimal,
      id: BigDecimal) => {
        var siteOpt = Site.getByUrl(urlSite)
        siteOpt match {
          case Some(site) => {
            new Article(
              titre,
              auteur,
              description,
              new DateTime(date),
              url,
              site,
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
          }
          case None => throw new Exception("Site not found")
        }
      }
    }.toList

    result match {
      case Nil => Some(result)
      case _ => None
    }

  }

  def getAllArticlesNonLus(utilisateur: Utilisateur, site: Site): Option[List[Article]] = {
    val listeArticlesDuSiteOpt = Site.getAllArticles(site)
    listeArticlesDuSiteOpt match {
      case Some(listeArticlesDuSite) => {
        val result: List[Article] = Cypher(
          """
      Match (user:Utilisateur {mail: {mailUser}})-[r1:consultation]-(article:Article)--(site:Site {url: {urlSite}})
                return  article.titre as titre,
                        article.auteur as auteur,
                        article.description as description,
                        article.date as date,
                        article.url as url,
                        site.url as urlSite,
                        article.image as image,
                        article.consultationsJour as consultationsJour,
                        article.consultationsSemaine as consultationsSemaine,
                        article.consultationsSemaineDerniere as consultationsSemaineDerniere,
                        article.consultationsMois as consultationsMois,
                        article.consultations as consultations,
                        article.totalEtoiles as totalEtoiles,
                        article.nbEtoiles as nbEtoiles,
                        article.nbCoeurs as nbCoeurs;
          """).on("urlSite" -> site.url, "mailUser" -> utilisateur.mail)().collect {
          case CypherRow(titre: String,
          auteur: String,
          description: String,
          date: String,
          url: String,
          urlSite: String,
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
              site,
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
        }.toList

        result match {
          case Nil => Some(listeArticlesDuSite)
          case _ => Some(listeArticlesDuSite diff result)
        }
      }
      case None => None
    }

  }

  def getSitesLesPlusConsultes(utilisateur: Utilisateur, nbMax: Int): Option[List[(Site, Int)]] = {
    val listeArticlesOpt = Utilisateur.getArticlesLus(utilisateur)
    var listeSites = List[(Site, Int)]()
    listeArticlesOpt match {
      case Some(listeArticles) => {
        for (article <- listeArticles) {
          if (listeSitesContains(listeSites, article.site)) {
            //listeSites = setValueListeSites(listeSites, article.site)
            listeSites.map {
              tmp => {
                if (tmp._1.equals(article.site)) {
                  (tmp._1, tmp._2 + 1)
                }
                else tmp
              }
            }
          }
          else {
            listeSites = (article.site, 1) :: listeSites
          }
        }
      }
      case None => None
    }
    Some(listeSites)

  }

  def getTopsSites(utilisateur: Utilisateur, nbMax: Int): Option[List[Site]] = {
    val result: List[Site] = Cypher(
      """
      match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site)
                return  site.url as url,
                        site.nom as nom,
                        site.type as type
                        order by r.nbCoeurs desc
                        limit {nbMax};
      """).on("nbMax" -> nbMax, "mailUser" -> utilisateur.mail)().collect {
      case CypherRow(url: String, nom: String, typeSite: String) => new Site(url, nom, typeSite)
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  def getTopsArticles(utilisateur: Utilisateur, nbMax: Int): Option[List[Article]] = {
    val result: List[Article] = Cypher(
      """
      Match (user:Utilisateur {mail: {mailUser}})-[r1:note]-(article:Article)--(site:Site)
                return  article.titre as titre,
                        article.auteur as auteur,
                        article.description as description,
                        article.date as date,
                        article.url as url,
                        site.url as urlSite,
                        article.image as image,
                        article.consultationsJour as consultationsJour,
                        article.consultationsSemaine as consultationsSemaine,
                        article.consultationsSemaineDerniere as consultationsSemaineDerniere,
                        article.consultationsMois as consultationsMois,
                        article.consultations as consultations,
                        article.totalEtoiles as totalEtoiles,
                        article.nbEtoiles as nbEtoiles,
                        article.nbCoeurs as nbCoeurs
                        order by r1.nbEtoiles DESC
                        limit {nbMax};
      """).on("mailUser" -> utilisateur.mail, "nbMax" -> nbMax)().collect {
      case CypherRow(titre: String,
      auteur: String,
      description: String,
      date: String,
      url: String,
      urlSite: String,
      image: String,
      consultationsJour: BigDecimal,
      consultationsSemaine: BigDecimal,
      consultationsSemaineDerniere: BigDecimal,
      consultationsMois: BigDecimal,
      consultations: BigDecimal,
      totalEtoiles: BigDecimal,
      nbEtoiles: BigDecimal,
      nbCoeurs: BigDecimal,
      id: BigDecimal) => {
        var siteOpt = Site.getByUrl(urlSite)
        siteOpt match {
          case Some(site) => {
            new Article(
              titre,
              auteur,
              description,
              new DateTime(date),
              url,
              site,
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
          }
          case None => throw new Exception("Site not found")
        }
      }
    }.toList

    result match {
      case Nil => Some(result)
      case _ => None
    }
  }


  def listeSitesContains(sites: List[(Site, Int)], siteCherche: Site): Boolean = {
    for (site <- sites) {
      if (site._1.equals(siteCherche)) true
    }
    false
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
                 order by r.nbCoeurs DESC
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

  def getTopEntitesPasFavories(user: Utilisateur, nbEntites: Int): Option[List[Entite]] = {
    val result: List[Entite] = Cypher(
      """
        match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite)
                 where r.estFavori = false
                 return entite.nom as nom,
                 entite.url as url,
                 entite.apparitionsJour as apparitionsJour,
                 entite.apparitionsSemaine as apparitionsSemaine,
                 entite.apparitionsSemaineDerniere as apparitionsSemaineDerniere,
                 entite.apparitionsMois as apparitionsMois,
                 entite.apparitions as apparitions
                 order by r.nbCoeurs DESC
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

}
