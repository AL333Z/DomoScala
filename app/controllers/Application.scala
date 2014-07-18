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
import actors._
import actors.DomoscalaActor._
import play.api.libs.concurrent.Promise
import akka.actor.ActorRef
import actors.DeviceActor._

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  // look for our "root" actor
  val domo = Akka.system.actorSelection("user/domoscala")

  def index = Action {
    Ok(views.html.index("Your new application is ready." + domo.pathString))
  }

  def getRooms(buildingId: String) = Action.async {
    val timeoutFuture = getTimeoutFuture
    val roomsFuture = getRoomsFuture(buildingId)
    Future.firstCompletedOf(Seq(roomsFuture, timeoutFuture)).map {
      case res: Set[_] => Ok(res.toString)
      case Failed(err) => BadRequest(err.getMessage())
      case t: String => InternalServerError(t)
    }
  }

  def getDevices(buildingId: String, roomId: String) = Action.async {
    val timeoutFuture = getTimeoutFuture
    val devicesFuture = getDevicesFuture(buildingId, roomId)
    Future.firstCompletedOf(Seq(devicesFuture, timeoutFuture)).map {
      case res: Map[_, _] => Ok(res.toString)
      case Failed(err) => BadRequest(err.getMessage())
      case t: String => InternalServerError(t)
    }
  }

  def getDeviceStatus(buildingId: String, roomId: String, deviceId: String) = TODO

  def setDevicesStatus(buildingId: String, roomId: String, deviceId: String) = TODO

  /**
   * Utilities, to get futures
   */
  def getTimeoutFuture = Promise.timeout("Timeout elapsed.", 5.second)

  def getRoomsFuture(buildingId: String): Future[Set[Room]] =
    (domo ? GetRooms(buildingId)).asInstanceOf[Future[Set[Room]]]

  def getDevicesFuture(buildingId: String, roomId: String): Future[Any] =
    (domo ? GetDevices(buildingId, roomId)).asInstanceOf[Future[Set[ActorRef]]]

}