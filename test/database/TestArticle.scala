package database
import org.scalatest._
import models.{Entite, EstLie, Site, Article}
import org.joda.time.DateTime

/**
 * Created by Administrator on 18/03/14.
 */
class TestArticle extends FunSuite with ShouldMatchers {


  test("creationSites") {
    val siteA = Site("http://www.lexpress.fr/rss/alaune.xml","L'Express", "A la une")
    assert(Site.create(siteA))
    val siteB = Site("http://rss.lefigaro.fr/lefigaro/laune","Le Figaro", "A la une")
    assert(Site.create(siteB))
    val siteC = Site("http://rss.lemonde.fr/c/205/f/3050/index.rss","Le Monde", "A la une")
    assert(Site.create(siteC))
    val siteD = Site("http://rss.nouvelobs.com/c/32262/fe.ed/tempsreel.nouvelobs.com/rss.xml","Le Nouvel Observateur", "A la une")
    assert(Site.create(siteD))
  }

  test("creationA") {

    val siteA = Site.get("http://www.lexpress.fr/rss/alaune.xml")
    val article = Article("titre 1", "auteur 1", "description 1", new DateTime(),"image 1","url 1", siteA)
    assert(Article.create(article))
  }

  test("creationB") {
    val siteB = Site.get("http://rss.lefigaro.fr/lefigaro/laune")
    val article = Article("titre 2", "auteur 2", "description 2", new DateTime(),"image 2","url 2", siteB)
    assert(Article.create(article))
    val article2 = Article("titre 3", "auteur 3", "description 3", new DateTime(),"image 3","url 3", siteB)
    assert(Article.create(article2))
  }

  test("getArticle") {
    val article = Article.getArticle("url 1")
    println("article : "+article)
  }

  test("estLieCreation") {
    val article = Article.getArticle("url 1")
    val article2 = Article.getArticle("url 3")
    assert(EstLie.create(article, article2, 10))
  }

  test("getPonderation") {
    val article = Article.getArticle("url 1")
    val article2 = Article.getArticle("url 3")
    assert(EstLie.getPonderation(article, article2) == 10)
  }

  test("getPonderationFailure") {
    val article = Article.getArticle("url 1")
    val article2 = Article.getArticle("url 2")
    assert(EstLie.getPonderation(article, article2) == -1)
  }

  test("creationEntite") {
    val entite = new Entite("entite 1","url entite 1")
    assert(Entite.create(entite))
  }

  test("getEntite") {
     val entite = Entite.get("url entite 1")
     println("Entite : "+entite)
  }

  test("creation tag") {
    val entite = Entite.get("url entite 1")
    val article2 = Article.getArticle("url 2")
    assert(models.Tag.create(article2, entite, 10))
  }

  test("get quantite") {
    val entite = Entite.get("url entite 1")
    val article2 = Article.getArticle("url 2")
    assert(models.Tag.getQuantite(article2, entite) == 10)
  }

  test("get quantite failure") {
    val entite = Entite.get("url entite 1")
    val article2 = Article.getArticle("url 3")
    assert(models.Tag.getQuantite(article2, entite) == -1)
  }

//  test("delete") {
//    val article = Article.deleteArticle("url 1")
//    println("article : "+article)
//  }

}
