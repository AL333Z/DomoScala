package controllers

import actors.{MeshnetToDeviceMessage, MeshnetBase, DeviceActor}
import actors.DeviceActor.On
import akka.actor.{ PoisonPill, Props }
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  def index = Action {

    val act = Akka.system.actorOf(Props[MeshnetBase], name = "meshnetbase")
    act ! MeshnetToDeviceMessage(123, 1, Array(1.toByte))

    Ok(views.html.index("Your new application is ready."))
  }

}