package controllers

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
import play.api.libs.json._

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  // look for our "root" actor
  val domo = Akka.system.actorSelection("user/domoscala")

  def index = Action {
    Ok(views.html.index("Your new application is ready." + domo.pathString))
  }

  def reqPushDeviceStatus(buildingId: String, roomId: String, deviceId: String) =
    WebSocket.acceptWithActor[String, JsValue] { request =>
      out => {     
    	Logger.debug("Push req received from request: " + request.toString)
        DeviceStatusWebSocketActor.props(out, buildingId, roomId, deviceId)
      }
    }

  def getBuildings = Action.async {
    val timeoutFuture = getTimeoutFuture
    val buildingsFuture = getBuildingFuture
    Future.firstCompletedOf(Seq(buildingsFuture, timeoutFuture)).map {
      case res: Set[_] => Ok(Json.obj(
        "status" -> "OK",
        "buildings" -> res.asInstanceOf[Set[Building]]))
      case Failed(err) => BadRequest(err.getMessage)
      case t: String => InternalServerError(t)
    }
  }

  def getRooms(buildingId: String) = Action.async {
    val timeoutFuture = getTimeoutFuture
    val roomsFuture = getRoomsFuture(buildingId)
    Future.firstCompletedOf(Seq(roomsFuture, timeoutFuture)).map {
      case res: Set[_] => Ok(Json.obj(
        "status" -> "OK",
        "rooms" -> res.asInstanceOf[Set[Room]]))
      case Failed(err) => BadRequest(err.getMessage)
      case t: String => InternalServerError(t)
    }
  }

  def getDevices(buildingId: String, roomId: String) = Action.async {
    val timeoutFuture = getTimeoutFuture
    val devicesFuture = getDevicesFuture(buildingId, roomId)
    Future.firstCompletedOf(Seq(devicesFuture, timeoutFuture)).map {
      case res: Map[_, _] => Ok(Json.obj(
        "status" -> "OK",
        "devices" -> res.asInstanceOf[Map[String, ActorRef]].map {
          case (id, actor) => (id, actor.path.toString)
        }))
      case Failed(err) => BadRequest(err.getMessage)
      case t: String => InternalServerError(t)
    }
  }

  def getDeviceStatus(buildingId: String, roomId: String, deviceId: String) = TODO

  def setDevicesStatus(buildingId: String, roomId: String, deviceId: String) = TODO

  /**
   * Utilities, to get futures
   */
  def getTimeoutFuture = Promise.timeout("Timeout elapsed.", 5.second)

  def getBuildingFuture: Future[Set[Building]] =
    (domo ? GetBuildings).mapTo[Set[Building]]

  def getRoomsFuture(buildingId: String): Future[Set[Room]] =
    (domo ? GetRooms(buildingId)).mapTo[Set[Room]]

  def getDevicesFuture(buildingId: String, roomId: String): Future[Map[String, ActorRef]] =
    (domo ? GetDevices(buildingId, roomId)).mapTo[Map[String, ActorRef]]

}
