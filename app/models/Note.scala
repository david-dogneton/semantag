package models

import org.anormcypher.Cypher

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
    Cypher(
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
      case head :: tail => Some(Note(user, article, head[BigDecimal]("nbEtoiles").toInt, head[Boolean]("aCoeur")))
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
          case head :: tail => Some(Note(user, article, head[BigDecimal]("nbEtoiles").toInt, head[Boolean]("aCoeur")))
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
