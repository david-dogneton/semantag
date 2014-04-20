package models

import org.anormcypher.Cypher
import scala.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class AppreciationEntite(utilisateur: Utilisateur, entite: Entite, quantite: Int, nbCoeurs: Int, estFavori: Boolean = false)

object AppreciationEntite {
  def create(appreciationEntite: AppreciationEntite): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (entite: Entite)
         where user.mail = {mailUser} and entite.url = {urlEnt}
         create (user)-[r:appreciationEntite {quantite : {quantite}, nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(entite)
      """
    ).on("mailUser" -> appreciationEntite.utilisateur.mail,
      "urlEnt" -> appreciationEntite.entite.url,
      "quantite" -> appreciationEntite.quantite,
      "nbCoeurs" -> appreciationEntite.nbCoeurs,
      "estFavori" -> appreciationEntite.estFavori
    ).execute()
  }

  def get(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def setQuantite(user: Utilisateur, entite: Entite, changementQuantite: Int): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.quantite = r.quantite + {changementQuantite}
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url,
      "changementQuantite" -> changementQuantite)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def incrNbCoeurs(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.nbCoeurs = r.nbCoeurs + 1
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbCoeurs(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def estFavori(user: Utilisateur, entite: Entite): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    result match {
      case Nil => throw new Exception("AppreciationEntite node doesn't exist.")
      case head :: tail => head[Boolean]("estFavori")
    }
  }

  def getNbCoeurs(entite: Entite): Int = {

    val result = Cypher(
      """
         match (user: Utilisateur)-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         return count(r) as nbCoeurs;
      """
    ).on("urlEnt" -> entite.url)().toList

    result match {
      case Nil => throw new Exception("AppreciationEntite node doesn't exist.")
      case head :: tail => head[Int]("nbCoeurs")
    }
  }

  def setFavori(user: Utilisateur, entite: Entite): Option[AppreciationEntite] = {

    val estFavoriList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url)().toList

    estFavoriList match {
      case Nil => None
      case head :: tail => {
        val estFavori = !head[Boolean]("estFavori")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         set r.estFavori = {nouvFavori}
         return r.quantite as quantite, r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
          """
        ).on("mailUser" -> user.mail,
          "urlEnt" -> entite.url,
          "nouvFavori" -> estFavori)().toList

        result match {
          case Nil => None
          case head :: tail => Some(AppreciationEntite(user, entite, head[BigDecimal]("quantite").toInt, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
        }
      }
    }
  }

  def delete(user: Utilisateur, entite: Entite): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlEnt}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlEnt" -> entite.url).execute()
    result
  }

  /**
   * Met à jour l'appréciation d'une entité pour un utilisateur, lors de la création d'une nouvelle note. Sert au système de recommandation. Agit sur le nombre de coeurs de l'AppreciationEntite et sa propriété "estFavori".
   * @param note note créée
   */
  def majAvecCreate(note: Note) = {
    var entitesOpt = Article.getEntitesLiees(note.article)
    entitesOpt match {
      case Some(entites) => {
        entites.map(elt => {
          var appreciationEntiteOpt = AppreciationEntite.get(note.utilisateur, elt)
          appreciationEntiteOpt match {
            case Some(appreciationEntite) => {
              AppreciationEntite.setQuantite(note.utilisateur, elt, note.nbEtoiles)
              if (note.aCoeur) AppreciationEntite.incrNbCoeurs(note.utilisateur, elt)
            }
            case None => {
              var nbCoeurs = 0
              if (note.aCoeur) nbCoeurs = 1
              AppreciationEntite.create(new AppreciationEntite(note.utilisateur, elt, note.nbEtoiles, nbCoeurs))
            }
          }

        })
      }
      case None => throw new Exception("Liste d'entités non trouvée")
    }
  }

  /**
   * Met à jour l'appréciation d'une entité pour un utilisateur, lors de la modification d'une note. Sert au système de recommandation. Agit sur le nombre de coeurs de l'AppreciationEntite et sa propriété "estFavori".
   * @param note note modifiée
   * @param changementNbEtoiles Le nombre d'étoiles à ajouter ou enlever à l'AppreciationEntite
   * @param setCoeur booléen stipulant s'il faut changer le nombre de coeurs
   * @param aCoeur booléen stipulant si l'AppreciationEntite va recevoir un nouveau coeur (true) ou en "perdre" un (false)
   */
  def majSansCreate(note: Note, changementNbEtoiles: Int = 0, setCoeur: Boolean = false, aCoeur: Boolean = false) = {
    var entitesOpt = Article.getEntitesLiees(note.article)
    entitesOpt match {
      case Some(entites) => {
        entites.map(elt => {
          var appreciationEntiteOpt = AppreciationEntite.get(note.utilisateur, elt)
          appreciationEntiteOpt match {
            case Some(appreciationEntite) => {
              AppreciationEntite.setQuantite(note.utilisateur, elt, changementNbEtoiles)
              if (setCoeur) {
                if (aCoeur) AppreciationEntite.incrNbCoeurs(note.utilisateur, elt)
                else AppreciationEntite.decrNbCoeurs(note.utilisateur, elt)
              }
            }
            case None => throw new Exception("AppreciationEntite non trouvée")
          }
        })
      }
      case None => throw new Exception("Liste d'entités non trouvée")
    }
    false
  }


}
