package models.database

import java.util.Date

/**
 * Created by Administrator on 17/03/14.
 */
case class Article(titre: String,
                   auteur: String,
                   description: String,
                   date: Date,
                   image: String,
                   url: String,
                   consultationsJour: Int = 0,
                   consultationsSemaine: Int = 0,
                   consultationsSemaineDerniere: Int = 0,
                   consultationsMois: Int = 0,
                   consultations: Int = 0,
                   totalEtoiles: Int = 0,
                   nbEtoiles: Int = 0,
                   nbCoeurs: Int = 0) {

}

object Article {

  def create(article : Article) = {

  }
}
