package models

import org.anormcypher.Cypher

/**
 * Created by Administrator on 20/03/14.
 */
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

  def create(aPourType: APourType): Boolean = {
    create(aPourType.entite, aPourType.typeT)
  }


}
