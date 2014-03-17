package models.database
import org.anormcypher._

/**
 * Created by Administrator on 15/03/14.
 */
object Country {

  def create(): Boolean = {
    val result = Cypher("""
  create (germany {name:"Germany", population:81726000, type:"Country", code:"DEU"}),
         (france {name:"France", population:65436552, type:"Country", code:"FRA", indepYear:1790}),
         (monaco {name:"Monaco", population:32000, type:"Country", code:"MCO"});
                        """).execute()
    result
  }


  def getNodesOfFrance(): List[(String, String)] = {
    val cypherQuery: CypherStatement = Cypher(
      """
    start n=node(*)
    match n-->m
    where n.code = 'FRA';
    return n,m;
      """
    )
    val countries = cypherQuery.apply().map(row =>
      row[String]("code") -> row[String]("name")
    ).toList

    countries
  }
}
