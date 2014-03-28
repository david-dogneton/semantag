package models.database

import org.anormcypher._

/**
 * Created by Administrator on 15/03/14.
 */
object Country {

  def create(): Boolean = {
    val result: Boolean = Cypher( """
        create (germany: Country {name:"Germany", population:81726000, code:"DEU"}),
               (france: Country {name:"France", population:65436552, code:"FRA", indepYear:1789}),
               (italie: Country {name:"Italia", population:42345622, code:"ITA"})
                                  """).execute()

//    val res = Cypher(
//      """
//        match(a: Country)
//        where a.name= 'France'
//        create (Pierre : Person {name : "Pierre"})-[vit:RELTYPE]-> (a),
//        create (Bruno : Person {name : "Bruno"})-[vit:RELTYPE]-> (a),
//        create (Paolo : Person {name : "Paolo"})-[vit:RELTYPE]-> (a),
//        create (Jean : Person {name : "Jean"})-[vit:RELTYPE]-> (a)
//      """).execute()
//
//    println("res : "+res)
    result
  }

  def delete() = {
    val result: Boolean = Cypher(
      """
        START n = node(*)
        OPTIONAL MATCH n-[r]-()
        WHERE (ID(n)>0)
          DELETE n, r;
      """).execute()
    result
  }


  def getAllCountries(): List[(String, String, Double)] = {

    val allCountries = Cypher("Match (n:Country) return n.code as code, n.name as name, n.population as population;")

    val countries = allCountries.apply().map(row => (row[String]("code"),row[String]("name"),row[Double]("population"))
    ).toList
    countries
  }

  def getNodesOfFrance(): List[(String, String)] = {
    val cypherQuery: CypherStatement = Cypher(
      """
    START n=node(*)
    match n-->m
    where n.code = 'FRA'
    return n,m;
      """
    )
    val countries = cypherQuery.apply().map(row =>
      row[String]("code") -> row[String]("name")
    ).toList

    countries
  }
}
