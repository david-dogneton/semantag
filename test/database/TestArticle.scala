package database

import org.scalatest._
import models._
import org.joda.time.DateTime
import org.anormcypher.{Cypher, CypherStatement}

/**
 * Created by Administrator on 18/03/14.
 */
class TestArticle extends FunSuite with ShouldMatchers {


//  test("creationSites") {
//    val siteA = Site("http://www.lexpress.fr/rss/alaune.xml", "L'Express", "A la une")
//    assert(Site.create(siteA))
//    val siteB = Site("http://rss.lefigaro.fr/lefigaro/laune", "Le Figaro", "A la une")
//    assert(Site.create(siteB))
//    val siteC = Site("http://rss.lemonde.fr/c/205/f/3050/index.rss", "Le Monde", "A la une")
//    assert(Site.create(siteC))
//    val siteD = Site("http://rss.nouvelobs.com/c/32262/fe.ed/tempsreel.nouvelobs.com/rss.xml", "Le Nouvel Observateur", "A la une")
//    assert(Site.create(siteD))
//  }
//
//  test("creationA") {
//
//    val siteA: Option[Site] = Site.get("http://www.lexpress.fr/rss/alaune.xml")
//    siteA match {
//      case Some(site) => val article = Article("titre 1", "auteur 1", "description 1", new DateTime(), "url 1", site)
//        assert(Article.create(article))
//      case None =>
//    }
//
//  }
//
//  test("creationB") {
//    val siteB = Site.get("http://rss.lefigaro.fr/lefigaro/laune")
//    siteB match {
//      case Some(site) =>
//        val article = Article("titre 2", "auteur 2", "description 2", new DateTime(), "url 2", site, "image 2")
//        assert(Article.create(article))
//        val article2 = Article("titre 3", "auteur 3", "description 3", new DateTime(), "url 3", site, "image 3")
//        assert(Article.create(article2))
//      case None => println("site not found")
//    }
//  }
//
//  test("getArticle") {
//    val article = Article.getArticle("url 1")
//    println("article : " + article)
//  }
//
//  test("estLieCreation") {
//    val articleOpt = Article.getArticle("url 1")
//    val article2Opt = Article.getArticle("url 3")
//    articleOpt match {
//      case Some(articleA) =>
//        article2Opt match {
//          case Some(articleB) => assert(EstLie.create(articleA, articleB, 10))
//          case None => println("article2Opt not found")
//        }
//      case None => println("articleOpt not found")
//    }
//
//  }
//
//  test("getPonderation") {
//    val articleOpt = Article.getArticle("url 1")
//    val article2Opt = Article.getArticle("url 3")
//    articleOpt match {
//      case Some(articleA) =>
//        article2Opt match {
//          case Some(articleB) => assert(EstLie.getPonderation(articleA, articleB) == 10)
//          case None => println("article2Opt not found")
//        }
//      case None => println("articleOpt not found")
//    }
//  }
//
//  test("getPonderationFailure") {
//    val articleOpt = Article.getArticle("url 1")
//    val article2Opt = Article.getArticle("url 2")
//    articleOpt match {
//      case Some(articleA) =>
//        article2Opt match {
//          case Some(articleB) => assert(EstLie.getPonderation(articleA, articleB) == -1)
//          case None => println("article2Opt not found")
//        }
//      case None => println("articleOpt not found")
//    }
//  }
//
//  test("creationEntite") {
//    val entite = new Entite("entite 1", "url entite 1")
//    assert(Entite.create(entite))
//  }
//
//  test("getEntite") {
//    val entite = Entite.get("url entite 1")
//    println("Entite : " + entite)
//  }
//
//  test("creation tag") {
//    val entiteOpt = Entite.get("url entite 1")
//    val article2Opt = Article.getArticle("url 2")
//    entiteOpt match {
//      case Some(entite) =>
//        article2Opt match {
//          case Some(article2) => assert(models.Tag.create(article2, entite, 10))
//          case None =>
//        }
//      case None => println("entite not found")
//    }
//  }
//
//  test("get quantite") {
//    val entiteOpt = Entite.get("url entite 1")
//    val article2Opt = Article.getArticle("url 2")
//    entiteOpt match {
//      case Some(entite) =>
//        article2Opt match {
//          case Some(article2) => assert(models.Tag.getQuantite(article2, entite) == 10)
//          case None =>
//        }
//      case None => println("entite not found")
//    }
//  }
//
//  test("get quantite failure") {
//    val entiteOpt = Entite.get("url entite 1")
//    val article2Opt = Article.getArticle("url 3")
//    entiteOpt match {
//      case Some(entite) =>
//        article2Opt match {
//          case Some(article2) => assert(models.Tag.getQuantite(article2, entite) == -1)
//          case None =>
//        }
//      case None => println("entite not found")
//    }
//  }
//
//  test("creation type") {
//    assert(Type.create(new Type("denomination type 1")))
//  }
//
//  test("get type") {
//    val typeT = Type.get("denomination type 1")
//    println("typeT : " + typeT)
//  }
//
//  test("get all sites") {
//    val sites = Site.getAll()
//    println("sites : " + sites)
//  }
//
//  test("a pour type") {
//
//    val typeTOpt = Type.get("denomination type 1")
//    val entiteOpt = Entite.get("url entite 1")
//    entiteOpt match {
//      case Some(entite) =>
//        typeTOpt match {
//          case Some(typeT) => assert(APourType.create(entite, typeT))
//          case None => println("type not found")
//        }
//      case None => println("article not found")
//    }
//  }

//  test("count sites") {
//    val sites = Site.getAll()
//    val request =Cypher(
//      """
//        MATCH (a)-[:`appartient`]->(b) RETURN distinct(b)
//      """
//    )()
//
//    assert(sites.size == request.size)
//  }



  //  test("delete") {
  //    val article = Article.deleteArticle("url 1")
  //    println("article : "+article)
  //  }

  test("getAllArticle") {
    val list = Article.getLastArticle()
    list.foreach(println)
    println("list : "+list.size)
  }

//  test("getArticle") {
//    val art = Article.getArticle("http://www.science.gouv.fr/fr/actualites/bdd/res/4937/plato-un-telescope-spatial-pour-decouvrir-des-systemes-planetaires-semblables-au-notre/")
//    println("art : "+art)
//  }

}
