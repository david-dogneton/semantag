package database
import org.scalatest._
import models.{Site, Article}
import java.util.Date

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
    val article = Article("titre 1", "auteur 1", "description 1", new Date(),"image 1","url 1", siteA)
    assert(Article.create(article))
  }

  test("creationB") {
    val siteB = Site.get("http://rss.lefigaro.fr/lefigaro/laune")
    val article = Article("titre 2", "auteur 2", "description 2", new Date(),"image 2","url 2", siteB)
    assert(Article.create(article))
  }

  test("get") {
    val article = Article.getArticle("url 1")
    println("article : "+article)
  }

//  test("delete") {
//    val article = Article.deleteArticle("url 1")
//    println("article : "+article)
//  }

}
