package models

import org.anormcypher.{CypherRow, Cypher}

case class Type(denomination: String, id : Int = -1) {

}

object Type {

  def create(typeT : Type): Option[Type] = {
    Cypher(
      """
        create (type: Type {
          denomination: {denomination}
        })
        return ID(type)
      """
    ).on("denomination" -> typeT.denomination)().collect {
      case CypherRow(id: BigDecimal) => Some(new Type(typeT.denomination, id.toInt))
      case _ => None
    }.head
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

  def getAll: List[Type] = {
    val result: List[Type] = Cypher(
      """
        Match (type:Type)
        return  distinct ID(type) as id, type.denomination as denomination ORDER BY type.denomination;
      """)().collect {
      case CypherRow(id: BigDecimal, denomination: String) => new Type(denomination, id.toInt)
      case _ => throw new IllegalArgumentException("Mauvais format du type")
    }.toList

    result
  }




}
