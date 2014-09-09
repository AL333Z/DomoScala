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
import actors.RoomStatusWebSocketActor
import actors.SystemStatusWebSocketActor

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  // look for our "root" actor
  val domo = Akka.system.actorSelection("user/domoscala")

  def index = Action {
    Ok(views.html.index("Your new application is ready." + domo.pathString))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Application.getBuildings,
        routes.javascript.Application.reqPushSystemStatus,
        routes.javascript.Application.reqPushBuildingStatus,
        routes.javascript.Application.reqPushRoomStatus,
        routes.javascript.Application.reqPushDeviceStatus))
      .as("text/javascript")
  }

  def reqPushSystemStatus =
    WebSocket.acceptWithActor[String, JsValue] { request =>
      out => {
        Logger.debug("System push req received from request: " + request.toString)
        SystemStatusWebSocketActor.props(out)
      }
    }

  def reqPushBuildingStatus(buildingId: String) =
    WebSocket.acceptWithActor[String, JsValue] { request =>
      out => {
        Logger.debug("Building push req received from request: " + request.toString)
        BuildingStatusWebSocketActor.props(out, buildingId)
      }
    }

  def reqPushRoomStatus(buildingId: String, roomId: String) =
    WebSocket.acceptWithActor[String, JsValue] { request =>
      out => {
        Logger.debug("Push req received from request: " + request.toString)
        RoomStatusWebSocketActor.props(out, buildingId, roomId)
      }
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
      case t: Throwable => InternalServerError(t.getMessage)
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
      case t: Throwable => InternalServerError(t.getMessage)
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
      case t: Throwable => InternalServerError(t.getMessage)
    }
  }

  def getDeviceStatus(buildingId: String, roomId: String, deviceId: String) = Action.async {
    val timeoutFuture = getTimeoutFuture
    val deviceStatusFuture = getDeviceStatusFuture(buildingId, roomId, deviceId)
    Future.firstCompletedOf(Seq(deviceStatusFuture, timeoutFuture)).map {
      case res: DeviceStatus => Ok(Json.obj(
        "status" -> "OK",
        "deviceStatus" -> res))
      case Failed(err) => BadRequest(err.getMessage)
      case t: Throwable => InternalServerError(t.getMessage)
    }
  }

  def setDevicesStatus(buildingId: String, roomId: String, deviceId: String) =
    Action.async(parse.json) { implicit request =>
      request.body.validate[DeviceStatus] match {
        case s: JsSuccess[DeviceStatus] => {
          val status: DeviceStatus = s.get

          // do something with place
          Logger.debug("Received device status: um: " + status.um + " value:" + status.value +
            " for device with id: " + deviceId +
            " in room: " + roomId +
            " in building: " + buildingId)

          val timeoutFuture = getTimeoutFuture
          val deviceStatusFuture = setDeviceStatusFuture(buildingId, roomId, deviceId, status)
          Future.firstCompletedOf(Seq(deviceStatusFuture, timeoutFuture)).map {
            case t: Throwable => InternalServerError(t.getMessage)
            case _ => Ok(Json.obj("status" -> "OK"))
          }
        }
        case e: JsError => {
          // error handling flow
          Future { BadRequest("Invalid Json payload.\n Error: " + e.errors) }
        }
      }
    }

  /**
   * Utilities, to get futures
   */
  def getTimeoutFuture = Promise.timeout(new Throwable("Timeout elapsed."), 5.second)

  def getBuildingFuture: Future[Set[Building]] =
    (domo ? GetBuildings).mapTo[Set[Building]]

  def getRoomsFuture(buildingId: String): Future[Set[Room]] =
    (domo ? GetRooms(buildingId)).mapTo[Set[Room]]

  def getDevicesFuture(buildingId: String, roomId: String): Future[Map[String, ActorRef]] =
    (domo ? GetDevices(buildingId, roomId)).mapTo[Map[String, ActorRef]]

  def getDeviceFuture(buildingId: String, roomId: String, deviceId: String): Future[ActorRef] =
    (domo ? GetDevice(buildingId, roomId, deviceId)).mapTo[ActorRef]

  def getDeviceStatusFuture(buildingId: String, roomId: String, deviceId: String): Future[DeviceStatus] =
    (domo ? GetDeviceStatus(buildingId, roomId, deviceId)).mapTo[DeviceStatus]

  def setDeviceStatusFuture(buildingId: String, roomId: String, deviceId: String,
    status: DeviceStatus) = Future {
    // send command, with 'fire and forget' semantic
    domo ! SetDeviceStatus(buildingId, roomId, deviceId, status)
  }
}
