package models

import org.anormcypher.Cypher
import play.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class Note(utilisateur: Utilisateur, article: Article, nbEtoiles: Int, aCoeur: Boolean = false)

object Note {
  def create(note: Note): Boolean = {
    val resultat = Cypher(
      """
         match (user: Utilisateur), (article: Article)
         where user.mail = {mailUser} and article.url = {urlArt}
         create (user)-[r:note {nbEtoiles : {nbEtoiles}, aCoeur : {aCoeur}}]->(article)
      """
    ).on("mailUser" -> note.utilisateur.mail,
      "urlArt" -> note.article.url,
      "nbEtoiles" -> note.nbEtoiles,
      "aCoeur" -> note.aCoeur
    ).execute()
    Logger.debug("Résultat de la création de la note : "+resultat)
    AppreciationEntite.majAvecCreate(note)
    AppreciationDomaine.majAvecCreate(note)
    AppreciationSite.majAvecCreate(note)
    resultat
  }

  def createWithIdArticle(utilisateur: Utilisateur, idArticle: Int, nbEtoiles: Int, aCoeur: Boolean = false): Boolean = {
    val articleOpt = Article.getById(idArticle)
    articleOpt match {
      case Some(article) => {
        val note = new Note(utilisateur, article, nbEtoiles, aCoeur)
        val resultat = Cypher(
          """
         match (user: Utilisateur), (article: Article)
         where user.mail = {mailUser} and article.url = {urlArt}
         create (user)-[r:note {nbEtoiles : {nbEtoiles}, aCoeur : {aCoeur}}]->(article)
          """
        ).on("mailUser" -> note.utilisateur.mail,
          "urlArt" -> note.article.url,
          "nbEtoiles" -> note.nbEtoiles,
          "aCoeur" -> note.aCoeur
        ).execute()
        AppreciationEntite.majAvecCreate(note)
        AppreciationDomaine.majAvecCreate(note)
        AppreciationSite.majAvecCreate(note)
        resultat
      }
      case None => throw new Exception("Article not found")
    }
  }

  def get(user: Utilisateur, article: Article): Option[Note] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:note]-(article: Article {url : {urlArt}})
         return r.nbEtoiles as nbEtoiles, r.aCoeur as aCoeur;
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url)().toList

    result match {
      case Nil => None
      case head :: tail => Some(Note(user, article, head[BigDecimal]("nbEtoiles").toInt, head[Boolean]("aCoeur")))
    }
  }

  def setNbEtoiles(user: Utilisateur, article: Article, changementNbEtoiles: Int): Option[Note] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationEntite]-(entite: Entite{url : {urlArt}})
         set r.nbEtoiles = r.nbEtoiles + {nbEtoiles}
         return r.nbEtoiles as nbEtoiles, r.aCoeur as aCoeur;
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url,
      "nbEtoiles" -> changementNbEtoiles)().toList

    result match {
      case Nil => None
      case head :: tail => {
        var note = Note(user, article, head[BigDecimal]("nbEtoiles").toInt, head[Boolean]("aCoeur"))
        AppreciationEntite.majSansCreate(note, changementNbEtoiles)
        AppreciationSite.majSansCreate(note, false, changementNbEtoiles)
        Some(note)
      }
    }
  }

  def aCoeur(user: Utilisateur, article: Article): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:note]-(article: Article{url : {urlArt}})
         return r.aCoeur as aCoeur;
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url)().toList

    result match {
      case Nil => throw new Exception("AppreciationEntite node doesn't exist.")
      case head :: tail => head[Boolean]("aCoeur")
    }
  }

  def setACoeur(user: Utilisateur, article: Article): Option[Note] = {

    val aCoeurList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:note]-(article: Article{url : {urlArt}})
         return r.aCoeur as aCoeur;
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url)().toList

    aCoeurList match {
      case Nil => None
      case head :: tail => {
        val aCoeur = !head[Boolean]("aCoeur")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:note]-(article: Article{url : {urlArt}})
         set r.aCoeur = {nouvACoeur}
         return r.nbEtoiles as nbEtoiles, r.aCoeur as aCoeur;
          """
        ).on("mailUser" -> user.mail,
          "urlArt" -> article.url,
          "nouvACoeur" -> aCoeur)().toList

        result match {
          case Nil => None
          case head :: tail => {
             var note = Note(user, article, head[BigDecimal]("nbEtoiles").toInt, head[Boolean]("aCoeur"))
            AppreciationEntite.majSansCreate(note, 0, true, aCoeur)
            AppreciationDomaine.majSansCreate(note, true, aCoeur)
            AppreciationSite.majSansCreate(note, false, 0, true, aCoeur)
            Some(note)
          }
        }
      }
    }
  }

  def delete(user: Utilisateur, article: Article): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:note]-(article: Article{url : {urlArt}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url).execute()
    result
  }


}
