package controllers

import jp.t2v.lab.play2.auth._
import models.Utilisateur
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LoginLogout extends Controller with LoginLogout with OptionalAuthElement with AuthConfigImpl {
  val loginForm = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    ){
      (email, password) =>Utilisateur(email,password,"")
    }{
      utilisateur => Some(utilisateur.mail,utilisateur.mdp)
    }
  )


  def connexion = StackAction {
    implicit request =>
      val maybeUser :Option[User] = loggedIn
      if(maybeUser.isDefined){
        Redirect(routes.Application.mapage())
      }
      else{
        Ok(views.html.connexion())
      }
  }

  def logout = Action.async {
    implicit request =>
      gotoLogoutSucceeded
  }


  def authenticate = Action.async {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("Formulaire connexion mal rempli")
          Future.successful(Redirect(routes.LoginLogout.connexion()).flashing("error"->"Les champs ne sont pas renseignés correctement !"))
        },
        user => {
          Logger.debug("Formulaire connexion bien rempli")
          Future(Utilisateur.authenticate(user.mail,user.mdp)).flatMap {
            case Some(_) =>
              Logger.debug("Compte Existant")
              gotoLoginSucceeded(user.mail)
            case None =>
              Logger.debug("L'auth n'a pas marché, et le compte mail existe pas donc le compte n'existe pas")
              authorizationFailed(request)
          }
        }
      )
  }

  val inscriptionForm = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "pseudo"->text
    ){
      (email, password,pseudo) =>Utilisateur(email,password,pseudo)
    }{
      utilisateur => Some(utilisateur.mail,utilisateur.mdp,utilisateur.pseudo)
    }
  )


  def inscription =StackAction{
    implicit request =>
    val maybeUser :Option[User] = loggedIn
    if(maybeUser.isDefined){
      Redirect(routes.Application.mapage())
    }
    else{
      Ok(views.html.inscription())
    }
  }

  def inscriptionsubmit = StackAction{
    implicit request =>
      inscriptionForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("Compte mal renseigné")
          Logger.debug(s"Bad registration !! : $formWithErrors")
          Redirect(routes.LoginLogout.inscription()).flashing("error" -> "Les champs sont mal renseignés  !")
        },
        admin => {
          Logger.debug("Compte bien renseigné")
          val mayBeUser: Option[Utilisateur] = Utilisateur.get(admin.mail)
         if (mayBeUser.isDefined) {
         Logger.debug("Compte déjà existant ")
         Redirect(routes.LoginLogout.inscription()).flashing("error" -> "Ce compte mail est déjà associé à un compte SEMANTAG !")
         }
        else{
          Logger.debug("Compte non existant !")
          Utilisateur.create(admin)
          Logger.debug("Compte bien crée")
          Redirect(routes.LoginLogout.inscription()).flashing("success" -> "Vous êtes maintenant membre de SEMANTAG !")
        }
        }
      )
  }



}
