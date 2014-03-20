package models

import org.anormcypher.{CypherRow, Cypher}

/**
 * Created by Administrator on 20/03/14.
 */
case class Type(denomination: String) {

}

object Type {

  def create(typeT : Type): Boolean = {
    Cypher(
      """
        create (type: Type {
          denomination: {denomination}
        })
      """
    ).on("denomination" -> typeT.denomination).execute()
  }

  def get(denomination : String): Option[Type] = {

    val result: List[Type] = Cypher(
      """
        Match (type:Type) where type.denomination= {denomination}
        return  type.denomination as denomination;
      """).on("denomination" -> denomination)().collect {
      case CypherRow(denomination: String) => new Type(denomination)
      case _ => throw new IllegalArgumentException("Mauvais format du type")
    }.toList

    result match {
      case Nil => None
      case head::tail => Some(head)
    }
  }
}
