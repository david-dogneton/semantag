package models

import org.anormcypher.Cypher

case class AppreciationDomaine(utilisateur: Utilisateur, domaine: Domaine, nbCoeurs: Int, estFavori: Boolean = false)

object AppreciationDomaine {
  def create(appreciationDomaine: AppreciationDomaine): Boolean = {
    Cypher(
      """
         match (user: Utilisateur), (domaine: Domaine)
         where user.mail = {mailUser} and domaine.nom = {nomDom}
         create (user)-[r:appreciationDomaine {nbCoeurs : {nbCoeurs}, estFavori : {estFavori}}]->(domaine)
      """
    ).on("mailUser" -> appreciationDomaine.utilisateur.mail,
      "nomDom" -> appreciationDomaine.domaine.nom,
      "nbCoeurs" -> appreciationDomaine.nbCoeurs,
      "estFavori" -> appreciationDomaine.estFavori
    ).execute()
  }

  def get(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine {nom : {nomDom}})
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => None
      case head :: tail =>
        Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def incrNbCoeurs(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         set r.nbCoeurs = r.nbCoeurs + 1
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def decrNbCoeurs(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         set r.nbCoeurs = r.nbCoeurs - 1
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => None
      case head :: tail => Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
    }
  }

  def estFavori(user: Utilisateur, domaine: Domaine): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    result match {
      case Nil => throw new Exception("AppreciationDomaine node doesn't exist.")
      case head :: tail => head[Boolean]("estFavori")
    }
  }

  def setFavori(user: Utilisateur, domaine: Domaine): Option[AppreciationDomaine] = {

    val estFavoriList = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         return r.estFavori as estFavori;
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom)().toList

    estFavoriList match {
      case Nil => None
      case head :: tail =>
        val estFavori = !head[Boolean]("estFavori")
        val result = Cypher(
          """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         set r.estFavori = {nouvFavori}
         return r.nbCoeurs as nbCoeurs, r.estFavori as estFavori;
          """
        ).on("mailUser" -> user.mail,
          "nomDom" -> domaine.nom,
          "nouvFavori" -> estFavori)().toList

        result match {
          case Nil => None
          case head :: tail => Some(AppreciationDomaine(user, domaine, head[BigDecimal]("nbCoeurs").toInt, head[Boolean]("estFavori")))
        }
    }
  }

  def delete(user: Utilisateur, domaine: Domaine): Boolean = {

    val result = Cypher(
      """
         match (user: Utilisateur {mail : {mailUser}})-[r:appreciationDomaine]-(domaine: Domaine{nom : {nomDom}})
         delete r
      """
    ).on("mailUser" -> user.mail,
      "nomDom" -> domaine.nom).execute()
    result
  }

  /**
   * Met à jour l'appréciation d'un domaine pour un utilisateur, lors de la création d'une nouvelle note. Sert au système de recommandation. Agit sur le nombre de coeurs de l'AppreciationDomaine et sa propriété "estFavori".
   * @param note note créée
   */
  def majAvecCreate(note: Note) {
    val domainesOpt = APourDomaine.getDomainesLies(note.article)
    domainesOpt match {
      case Some(domaines) =>
        domaines.map(elt => {
          val appreciationDomaineOpt = AppreciationDomaine.get(note.utilisateur, elt)
          appreciationDomaineOpt match {
            case Some(appreciationDomaine) =>
              if (note.aCoeur) AppreciationDomaine.incrNbCoeurs(note.utilisateur, elt)
            case None =>
              var nbCoeurs = 0
              if (note.aCoeur) nbCoeurs = 1
              AppreciationDomaine.create(new AppreciationDomaine(note.utilisateur, elt, nbCoeurs))
          }
        })
      case None => throw new Exception("Liste de domaines non trouvée")
    }
  }

  /**
   * Met à jour l'appréciation d'un domaine pour un utilisateur, lors de la modification d'une note. Sert au système de recommandation. Agit sur le nombre de coeurs de l'AppreciationDomaine et sa propriété "estFavori".
   * @param note note modifiée
   * @param setCoeur booléen stipulant s'il faut changer le nombre de coeurs
   * @param aCoeur booléen stipulant si l'AppreciationDomaine va recevoir un nouveau coeur (true) ou en "perdre" un (false)
   */
  def majSansCreate(note: Note, setCoeur: Boolean = false, aCoeur: Boolean = false) {
    val domainesOpt = APourDomaine.getDomainesLies(note.article)
    domainesOpt match {
      case Some(domaines) =>
        domaines.map(elt => {
          val appreciationDomaineOpt = AppreciationDomaine.get(note.utilisateur, elt)
          appreciationDomaineOpt match {
            case Some(appreciationDomaine) =>
              if (setCoeur) {
                if (aCoeur) AppreciationDomaine.incrNbCoeurs(note.utilisateur, elt)
                else AppreciationDomaine.decrNbCoeurs(note.utilisateur, elt)
              }
            case None => throw new Exception("AppreciationDomaine non trouvée")
          }
        })
      case None => throw new Exception("Liste de domaines non trouvée")
    }
  }
}
