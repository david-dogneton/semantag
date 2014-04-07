package controllers


import models.{Utilisateur, Permission}
import scala.concurrent.{Future, ExecutionContext}

import play.api.mvc.{SimpleResult, RequestHeader, Controller}
import jp.t2v.lab.play2.auth.AuthConfig

import jp.t2v.lab.play2.auth.CacheIdContainer
import scala.reflect._

trait AuthConfigImpl extends AuthConfig with Controller{

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = String

  /**
   * A type that represents a user in your application.
   * `User`, `Account` and so on.
   */
  type User = Utilisateur

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   *
   * sealed trait Permission
   * case object Administrator extends Permission
   * case object NormalUser extends Permission
   */
  type Authority = Permission

  /**
   * A `ClassTag` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  val idTag: ClassTag[Id] = classTag[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = Future(Utilisateur.get(id))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.mapage))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.Application.index).flashing("success"->"Vous avez été déconnecté avec succès"))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.LoginLogout.connexion))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[SimpleResult] =
    Future.successful(Redirect(routes.LoginLogout.connexion).flashing("error"->"Ce compte n'existe pas, ou le mot de passe est incorrect !"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (user, authority) match {
      case _    =>  true
    }
  }

  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)
}