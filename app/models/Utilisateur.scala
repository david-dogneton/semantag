package models

import org.anormcypher.{CypherRow, CypherResultRow, CypherStatement, Cypher}
import play.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 17/03/14
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
case class Utilisateur(mail: String, mdp: String, pseudo: String, nbCoeurs: Int = 0)

object Utilisateur {
  def create(utilisateur: Utilisateur): Boolean = {
    val result: Boolean = Cypher( """
        create (user: Utilisateur {
        mail: {mailDonne},
        mdp: {mdpDonne},
        pseudo: {pseudoDonne},
        nbCoeurs: {nbCoeursDonne}});
                                  """).on("mailDonne" -> utilisateur.mail, "mdpDonne" -> utilisateur.mdp, "pseudoDonne" -> utilisateur.pseudo, "nbCoeursDonne" -> utilisateur.nbCoeurs).execute()

    result
  }

  def get(adresseMail: String): Option[Utilisateur] = {
    val result: List[Utilisateur] = Cypher(
      """
        Match (n:Utilisateur)
        where n.mail = {mailDonne}
        return n.mail as mail,
          n.mdp as mdp,
          n.pseudo as pseudo,
          n.nbCoeurs as nbCoeurs;
        """).on("mailDonne" -> adresseMail)().collect{
      case CypherRow(mail: String,
      mdp: String,
      pseudo: String,
      nbCoeurs: BigDecimal) =>
      new Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt)
      case _ => throw new Exception("Utilisateur not found")
    }.toList
    result match {
      case Nil => None
      case head :: tail => Some(head)
    }
  }


  def authenticate(adresseMail: String, mdp :String): Option[Utilisateur] = {
    val result: CypherRow = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} and n.mdp = {mdpDonne} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail, "mdpDonne" -> mdp).apply().head
    result match {
      case CypherRow(mail : String, mdp : String, pseudo : String, nbCoeurs : BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }


  def setMail(ancienMail: String, nouveauMail: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} set n.mail = {nouveauMail} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> ancienMail, "nouveauMail" -> nouveauMail).apply().head
    result match {
      case CypherRow(mail : String, mdp : String, pseudo : String, nbCoeurs : BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def setPseudo(adresseMail: String, nouveauPseudo: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} set n.pseudo = {nouveauPseudo} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail, "nouveauPseudo" -> nouveauPseudo).apply().head
    result match {
      case CypherRow(mail : String, mdp : String, pseudo : String, nbCoeurs : BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def setMdp(adresseMail: String, nouveauMdp: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} set n.mdp = {nouveauMdp} return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail, "nouveauMdp" -> nouveauMdp).apply().head
    result match {
      case CypherRow(mail : String, mdp : String, pseudo : String, nbCoeurs : BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def incrementerNbCoeurs(adresseMail: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} set n.nbCoeurs = n.nbCoeurs + 1 return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail).apply().head
    result match {
      case CypherRow(mail : String, mdp : String, pseudo : String, nbCoeurs : BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def decrementerNbCoeurs(adresseMail: String): Option[Utilisateur] = {
    val result: CypherRow = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} set n.nbCoeurs = n.nbCoeurs - 1 return n.mail as mail, n.mdp as mdp, n.pseudo as pseudo, n.nbCoeurs as nbCoeurs;").on("mailDonne" -> adresseMail).apply().head
    result match {
      case CypherRow(mail : String, mdp : String, pseudo : String, nbCoeurs : BigDecimal) => Some(Utilisateur(mail, mdp, pseudo, nbCoeurs.toInt))
      case _ => None
    }
  }

  def delete(adresseMail: String): Boolean = {
    val result: Boolean = Cypher( "Match (n:Utilisateur) where n.mail = {mailDonne} delete n;").on("mailDonne" -> adresseMail).execute()
    result
  }



}
