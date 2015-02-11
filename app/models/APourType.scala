package models

import org.anormcypher.Cypher


case class APourType(entite: Entite, typeT: Type) {

}

object APourType {

  def create(entite: Entite, typeT: Type): Boolean = {

    Cypher(
      """
         match (entite: Entite), (type: Type)
         where entite.url = {url} and type.denomination = {denomination}
         create (entite)-[r:aPourType]->(type)
      """
    ).on("url" -> entite.url,
        "denomination" -> typeT.denomination
      ).execute()
  }

  def createTypeAndRel(entite: Entite, typeT: Type): Boolean = {

    Cypher(
      """
         match (entite: Entite)
         where entite.url = {url}
         create (entite)-[r:aPourType]->(type: Type {
                  denomination: {denomination}
         })
      """
    ).on("url" -> entite.url,
        "denomination" -> typeT.denomination
      ).execute()

  }

  def create(aPourType: APourType): Boolean = {
    create(aPourType.entite, aPourType.typeT)
  }


}
