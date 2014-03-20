package models

import org.anormcypher.Cypher

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 20/03/14
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
case class AppreciationEntite(utilisateur : Utilisateur, entite : Entite, quantite : Int, nbCoeurs : Int, estFavori : Boolean = false)

object AppreciationEntite {
  def create(appreciationEntite: AppreciationEntite): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (entite: Entite)
         where user.mail = {mailUser} and entite.url = {urlEnt}
         create (user)-[r:appreciationEntite {quantite : {quantite}, nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(entite)
      """
    ).on("mailUser" -> appreciationEntite.utilisateur.mail,
      "urlEnt" -> appreciationEntite.entite.url,
      "quantite" -> appreciationEntite.quantite,
      "nbCoeurs" -> appreciationEntite.nbCoeurs,
      "estFavori" -> appreciationEntite.estFavori
    ).execute()
  }
}
