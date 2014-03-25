package controllers

import play.api.mvc.{Action, Controller}
import jp.t2v.lab.play2.auth.{OptionalAuthElement, LoginLogout}
import models.{Utilisateur, Permission}
import play.api.data.Form
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LoginLogout extends Controller with LoginLogout with OptionalAuthElement with AuthConfigImpl {
  val loginForm = Form(
    mapping(
      "email" -> text,
      "password" -> text
    ){
      (email, password) =>Utilisateur(email,password,"")
    }{
      utilisateur => Some(utilisateur.mail,utilisateur.mdp)
    }
  )

  val inscriptionForm = Form(
    mapping(
      "email" -> text,
      "password" -> text,
      "pseudo"->text
    ){
      (email, password,pseudo) =>Utilisateur(email,password,pseudo)
    }{
      utilisateur => Some(utilisateur.mail,utilisateur.mdp,utilisateur.pseudo)
    }
  )

  def connexion = StackAction {
    implicit request =>
      val maybeUser :Option[User] = loggedIn
      if(maybeUser.isDefined){
        Logger.debug("Deja Loggué !!!")
        Redirect(routes.Application.mapage)
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
          Future.successful(BadRequest("Non."))
        },
        user => {
          Logger.debug("Formulaire connexion bien rempli")
          Future(Utilisateur.authenticate(user.mail,user.mdp)).flatMap {
            current =>
              current match {
                case Some(_) => {
                  Logger.debug("Compte Existant")
                  gotoLoginSucceeded(user.mail)
                }
                case None => {
                  Logger.debug("L'auth n'a pas marché, et le compte mail existe pas donc le compte n'existe pas")
                  authorizationFailed(request)
                }
              }
          }
        }
      )
  }





  def inscription =StackAction{
    implicit request =>
    val maybeUser :Option[User] = loggedIn
    if(maybeUser.isDefined){
      Logger.debug("Deja Loggué !!! => SE DELOGER !!")
      Redirect(routes.Application.mapage())
    }
    else{
      Ok(views.html.inscription())
    }
  }
  def inscriptionsubmit = Action{
    implicit request =>
      inscriptionForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("Compte mal renseigné")
          Logger.debug(s"Bad registration !! : ${formWithErrors}")
          BadRequest(views.html.inscription())
        },
        admin => {
          Logger.debug("Compte bien renseigné")
          Utilisateur.create(admin)
          Logger.debug("Compte bien crée")
          Redirect(routes.Application.mapage)
//          if(admin.id.isDefined){
//            Accounts.update(admin.id.get,admin)
//            Redirect(routes.Application.admin).flashing("success"->"Administrateur mis à jour avec succès")
//          }
//          else {
//            Accounts.insert(admin)
//            Redirect(routes.Application.admin).flashing("success"->"Administrateur inséré avec succès")
//          }
        }
      )
  }


}
