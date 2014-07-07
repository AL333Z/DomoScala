package controllers

import actors.DeviceActor
import actors.DeviceActor.On
import akka.actor.{PoisonPill, Props}
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  def index = Action {

    val myActor = Akka.system.actorOf(Props[DeviceActor], name = "mydevice")
    myActor ! On
    myActor ! PoisonPill

    Ok(views.html.index("Your new application is ready."))

  }

}