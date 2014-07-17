package controllers

import actors.{ MeshnetToDeviceMessage, MeshnetBase, DeviceActor }
import actors.DeviceActor.On
import akka.actor.{ PoisonPill, Props }
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getRooms(buildingId: String) = Action.async {
    val timeoutFuture = play.api.libs.concurrent.Promise.timeout("Opss", 5.second)
    Future.firstCompletedOf(Seq(futureResult, timeoutFuture)).map {
      case res: Result => res
      case t: String => InternalServerError(t)
    }
  }

  def getDevices(buildingId: String, roomId: String) = Action.async {
    val act = Akka.system.actorOf(Props[MeshnetBase], name = "meshnetbase")
    val future = act ? "asdf"
    future map {
      case res => Ok
    } recover {
      case _ => Ok
    }
  }

  def getDeviceStatus(buildingId: String, roomId: String, deviceId: String) = TODO

  def setDevicesStatus(buildingId: String, roomId: String, deviceId: String) = TODO

  //
  // Trying...
  //
  def intensiveComputation: Int = {
    Thread.sleep(2000)
    41
  }

  val futureValue: Future[Int] = scala.concurrent.Future(intensiveComputation)
  val futureResult = futureValue map { pi =>
    Ok("value computed: " + pi)
  } recover {
    case _ => InternalServerError
  }

}