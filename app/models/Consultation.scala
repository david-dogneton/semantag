package models

import org.anormcypher.Cypher
import org.joda.time.DateTime

case class Consultation(utilisateur: Utilisateur, article: Article, date: DateTime)

object Consultation {
  def create(consultation: Consultation): Boolean = {
    val result = Cypher(
      """
         match (user: Utilisateur), (article: Article)
         where user.mail = {mailUser} and article.url = {urlArt}
         create (user)-[r:consultation {date : {date}}]->(article)
      """
    ).on("mailUser" -> consultation.utilisateur.mail,
      "urlArt" -> consultation.article.url,
      "date" -> consultation.date.toString()
    ).execute()
    if (result) Article.incrNbConsultations(consultation.article.url)
    result
  }

  def get(user: Utilisateur, article: Article): Option[Consultation] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:consultation]-(article: Article {url : {urlArt}})
         return r.date as date;
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url)().toList

    result match {
      case Nil => None
      case head :: tail =>
        val date = head[String]("date")
        Some(Consultation(user, article, new DateTime(date)))
    }
  }

  def delete(user: Utilisateur, article: Article): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:consultation]-(article: Article{url : {urlArt}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlArt" -> article.url).execute()
    result
  }
}
