package controllers

import play.api.mvc.Controller
import jp.t2v.lab.play2.auth.{AuthenticationElement, LoginLogout}

object UserController extends Controller with LoginLogout with AuthenticationElement with AuthConfigImpl {


}
