package models

import org.anormcypher.{CypherRow, Cypher}
import org.joda.time.DateTime
import play.api.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class RecommandationParLike(utilisateur: Utilisateur, article: Article, entite: Entite, ponderation: Double)

object RecommandationParLike {
  def create(recommandationParLike: RecommandationParLike): Boolean = {
    val resultCreate: Boolean = Cypher( """
        create (recommandationParLike: RecommandationParLike {
        ponderation: {ponderation},
        urlArticle: {urlArticle},
        urlEntite: {urlEntite},
        mailUser: {mailUser}});
                                        """).on("ponderation" -> recommandationParLike.ponderation, "urlArticle" -> recommandationParLike.article.url, "urlEntite" -> recommandationParLike.entite.url, "mailUser" -> recommandationParLike.utilisateur.mail).execute()

    if (resultCreate) {
      val otherResult: Boolean = Cypher(
        """
         match (user: Utilisateur), (recommandationParLike: RecommandationParLike)
         where user.mail = {mailUser} and recommandationParLike.mailUser = {mailUser}
         create (user)-[r:lienRecommandationParLikeUser]->(recommandationParLike)
        """
      ).on("mailUser" -> recommandationParLike.utilisateur.mail).execute()
      if (!otherResult) false
      val otherResult2: Boolean = Cypher(
        """
         match (article: Article), (recommandationParLike: RecommandationParLike)
         where article.url = {urlArticle} and recommandationParLike.urlArticle = {urlArticle}
         create (article)-[r:lienRecommandationParLikeArticle]->(recommandationParLike)
        """
      ).on("urlArticle" -> recommandationParLike.article.url).execute()
      if (!otherResult2) false
      Cypher(
        """
         match (entite: Entite), (recommandationParLike: RecommandationParLike)
         where entite.url = {urlEntite} and recommandationParLike.urlEntite = {urlEntite}
         create (entite)-[r:lienRecommandationParLikeEntite]->(recommandationParLike)
        """
      ).on("urlEntite" -> recommandationParLike.entite.url).execute()
    }
    false
  }

  def get(user: Utilisateur, article: Article, entite: Entite): Option[RecommandationParLike] = {
    val result: CypherRow = Cypher("Match (n:RecommandationParLike) where n.mailUser = {mailDonne} return n.ponderation as ponderation;").on("mailDonne" -> user.mail, "urlArticle" -> article.url, "urlEntite" -> entite.url).apply().head
    result match {
      case CypherRow(ponderation: BigDecimal) => Some(RecommandationParLike(user, article, entite, ponderation.toDouble))
      case _ => None
    }
  }

  def setPonderation(user: Utilisateur, article: Article, entite: Entite, nouvellePonderation: Double): Option[RecommandationParLike] = {
    val result: CypherRow = Cypher("Match (n:RecommandationParLike) where n.mailUser = {mailDonne} set n.ponderation = {nouvPonderation} return n.ponderation as ponderation;").on("mailDonne" -> user.mail, "urlArticle" -> article.url, "urlEntite" -> entite.url, "nouvPonderation" -> nouvellePonderation).apply().head
    result match {
      case CypherRow(ponderation: BigDecimal) => Some(RecommandationParLike(user, article, entite, ponderation.toDouble))
      case _ => None
    }
  }

  def delete(user: Utilisateur, site: Site): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationSite]-(site: Site{url : {urlSite}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "urlSite" -> site.url).execute()
    result
  }

  /**
   * Récupère les RecommandationParLike les plus pertinentes pour un utilisateur donné (en fonction de leur propriété "pondération").
   * @param user l'utilisateur étudié
   * @param nbRecommandations le nombre de recommandations à sélectionner
   * @return Some d'une liste de RecommandationParLike (comportant maximum "nbRecommandations" éléments), ou None si les informations n'ont pas été trouvées en BDD
   */
  def getTopRecommandationsParLike(user: Utilisateur, nbRecommandations: Int): Option[List[RecommandationParLike]] = {
    val result: List[RecommandationParLike] = Cypher(
      """
        match (user: Utilisateur {mail : {mailUser}})-[r:lienRecommandationParLikeUser]-(reco: RecommandationParLike)
                 return reco.mailUser as mailUser,
                 reco.urlArticle as urlArticle,
                 reco.urlEntite as urlEntite,
                 reco.ponderation as ponderation
                 order by r.ponderation DESC
                 limit {nbRecommandations};
      """).on("mailUser" -> user.mail, "nbRecommandations" -> nbRecommandations)().collect {
      case CypherRow(mailUser: String,
      urlArticle: String,
      urlEntite: String,
      ponderation: BigDecimal) => {
        var optionUser = Utilisateur.get(mailUser)
        optionUser match {
          case Some(user) => {
            var optionArticle = Article.getByUrl(urlArticle)
            optionArticle match {
              case Some(article) => {
                var optionEntite = Entite.getByUrl(urlEntite)
                optionEntite match {
                  case Some(entite) => {
                    new RecommandationParLike(user, article, entite, ponderation.toInt)
                  }
                  case None => throw new IllegalArgumentException("Entité non trouvable")
                }
              }
              case None => throw new IllegalArgumentException("Article non trouvable")
            }
          }
          case None => throw new IllegalArgumentException("Utilisateur non trouvable")
        }
      }
      case _ => throw new IllegalArgumentException("Mauvais format de l'entite")
    }.toList

    result match {
      case Nil => None
      case _ => Some(result)
    }
  }

  /**
   * Construit les instances de "RecommandationParLike" pour un utilisateur donné. Établit une pondération basée sur celle de la relation "EstLie" entre deux articles (60%), de la place du site dans les favoris de l'utilisateur (30%) et de la présence d'une entité dans les favoris de l'utilisateur (10%).
   * @param user l'utilisateur étudié
   * @return vrai si la création s'est bien déroulée, faux sinon.
   */
  def buildRecommandationsParLike(user: Utilisateur): Boolean = {
    var res = true
    val listeEntitesFavOpt = Utilisateur.getTopEntites(user, 50)
    val listeArticlesFavOpt = Utilisateur.getTopsArticles(user, 50)
    val listeSitesFavOpt = Utilisateur.getTopsSites(user, 30)
    val listeArticlesLusOpt = Utilisateur.getArticlesLus(user)
    listeEntitesFavOpt match {
      case Some(listeEntitesFav) => {
        listeArticlesFavOpt match {
          case Some(listeArticlesFav) => {
            listeArticlesLusOpt match {
              case Some(listeArticlesLus) => {
                listeSitesFavOpt match {
                  case Some(listeSitesFav) => {
                    for (article <- listeArticlesFav) {
                      val listeArticlesLiesOpt = EstLie.getByIdWithPonderation(article.id)
                      listeArticlesLiesOpt match {
                        case Some(listeArticlesLies) => {
                          for (articleLie <- listeArticlesLies) {
                            if (!listeArticlesLus.contains(articleLie._1)) {
                              var ponderation = 0.6 * articleLie._2
                              if (listeSitesFav.contains(articleLie._1.site)) {
                                ponderation += 0.3 * (listeSitesFav.indexOf(articleLie._1.site) / listeSitesFav.size)
                              }
                              val listeEntitesArticleLie = Tag.getTagsOfArticles(articleLie._1)
                              for (entite <- listeEntitesArticleLie) {
                                if (listeEntitesFav.contains(entite._1)) {
                                  RecommandationParLike.create(new RecommandationParLike(user, articleLie._1, entite._1, ponderation + 0.1))
                                }
                              }
                            }
                          }
                        }
                        case None => {}
                      }
                    }
                  }
                  case None => None
                }
              }
              case None => None
            }
          }
          case None => None
        }
      }
      case None => None
    }
    res
  }

}
