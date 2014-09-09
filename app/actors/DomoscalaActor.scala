package actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import actors.DomoscalaActor._
import actors.DeviceActor._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import akka.event.LoggingReceive
import akka.actor.Props
import actors.device.ThermometerActor
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class Room(id: String, devices: Map[String, ActorRef])
object Room extends ((String, Map[String, ActorRef]) => Room) {

  // implicit Json serializer
  implicit val RoomToJson: Writes[Room] = (
    (__ \ "id").write[String] ~ (__ \ "devices").write[Map[String, String]]) {
      (room: Room) =>
        (room.id,
          room.devices.map { case (id, actor) => (id, actor.path.toString) })
    }
}

case class Building(id: String, rooms: Set[Room])
object Building extends ((String, Set[Room]) => Building) {

  // implicit Json serializer
  implicit val BuildingToJson: Writes[Building] = (
    (__ \ "id").write[String] ~ (__ \ "rooms").write[Set[Room]]) {
      (building: Building) => (building.id, building.rooms)
    }
}

/**
 * Companion object of DomoscalaActor, that contains the messages used to query
 * the system
 */
object DomoscalaActor {
  def props(name: String): Props = Props(classOf[DomoscalaActor], name)

  object GetBuildings
  case class GetRooms(buildingId: String)
  case class GetDevices(buildingId: String, roomId: String)
  case class GetDevice(buildingId: String, roomId: String, deviceId: String)
  case class GetDeviceStatus(buildingId: String, roomId: String, deviceId: String)
  case class SetDeviceStatus(buildingId: String, roomId: String, deviceId: String, status: DeviceStatus)
  case class AddBuilding(building: Building)
}

/**
 * This actor wraps the structure of the system.
 * It contains a `Set` of `Building`. This set can be also dynamically changed.
 * The structure of the system is returned only via message-passing.
 * To initialize the system, the first message received can be:
 * - an ActorRef of the meshnet actor wrapper
 * - None, if we want to simulate a system (testing, demo, ...)
 */
class DomoscalaActor(name: String) extends Actor with ActorLogging {

  var buildings: Set[Building] = Set()
  implicit val timeout = Timeout(5 seconds)

  def receive = LoggingReceive {

    case GetBuildings => sender ! buildings

    case GetRooms(buildingId) =>
      getRooms(buildings, buildingId, sender).map(set => sender ! set)

    case GetDevices(buildingId, roomId) =>
      getDevices(buildings, buildingId, roomId, sender).map(m => sender ! m)

    case GetDevice(buildingId, roomId, deviceId) =>
      getDevice(buildings, buildingId, roomId, deviceId, sender) match {
        case Some(devActorRef) =>
          sender ! devActorRef
        case None =>
      }

    case AddBuilding(building) =>
      buildings += building

    // implementation
    case GetDeviceStatus(buildingId, roomId, deviceId) =>
      val requestor = sender.actorRef
      getDevice(buildings, buildingId, roomId, deviceId, requestor) match {
        case Some(devActorRef) =>
          (devActorRef ? GetStatus).mapTo[DeviceStatus].map {
            case status: DeviceStatus => { requestor ! status }
          }
        case None => // doing nothing, a timeout will fire
      }

    case SetDeviceStatus(buildingId, roomId, deviceId, status: DeviceStatus) =>
      val requestor = sender.actorRef
      getDevice(buildings, buildingId, roomId, deviceId, requestor) match {
        case Some(devActorRef) =>
          println("status " + status.value)
          (devActorRef ? status).mapTo[Any]
        case None => // doing nothing, a timeout will fire
      }

    case _ => sender ! UnsupportedAction
  }

  /*
   * Utility methods, to get entities and manage edge cases
   */
  def getRooms(buildings: Set[Building], buildingId: String,
    sender: ActorRef): Option[Set[Room]] = {

    buildings.filter(_.id == buildingId).toList match {
      case (x :: _) => Some(x.rooms)
      case Nil => {
        sender ! Failed(new Throwable("No rooms with given building id."))
        None
      }
      case _ => {
        sender ! Failed(new Throwable("WTF?!"))
        None
      }
    }
  }

  def getDevices(buildings: Set[Building], buildingId: String, roomId: String,
    sender: ActorRef): Option[Map[String, ActorRef]] = {

    getRooms(buildings, buildingId, sender) match {
      case Some(rooms) => rooms.filter(_.id == roomId).toList match {
        case (x :: _) => Some(x.devices)
        case Nil => {
          sender ! Failed(new Throwable("No devices with given room id."))
          None
        }
        case _ => {
          sender ! Failed(new Throwable("WTF?!"))
          None
        }
      }
      case None => None
    }
  }

  def getDevice(buildings: Set[Building], buildingId: String, roomId: String,
    deviceId: String, sender: ActorRef): Option[ActorRef] = {

    getDevices(buildings, buildingId, roomId, sender) match {
      case Some(devicesMap) => devicesMap.get(deviceId) match {
        case Some(devActorRef) => Some(devActorRef)
        case None => {
          sender ! Failed(new Throwable("No device with given device id."))
          None
        }
      }
      case None => None
    }
  }

}
