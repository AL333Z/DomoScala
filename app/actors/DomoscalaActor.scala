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

case class Dev(val id: String, val devType: String, actorRef: ActorRef)
object Dev extends ((String, String, ActorRef) => Dev) {
  // implicit Json serializer
  implicit val DevieToJson: Writes[Dev] = (
    (__ \ "id").write[String] ~ (__ \ "devType").write[String]) {
      (device: Dev) =>
        (device.id, device.devType)
    }
}

case class Room(id: String, devices: Set[Dev])
object Room extends ((String, Set[Dev]) => Room) {

  // implicit Json serializer
  implicit val RoomToJson: Writes[Room] = (
    (__ \ "id").write[String] ~ (__ \ "devices").write[Set[Dev]]) {
      (room: Room) => (room.id, room.devices)
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
        case Some(dev) =>
          sender ! dev
        case None =>
      }

    case AddBuilding(building) =>
      buildings += building

    // implementation
    case GetDeviceStatus(buildingId, roomId, deviceId) =>
      val requestor = sender.actorRef
      getDevice(buildings, buildingId, roomId, deviceId, requestor) match {
        case Some(device) =>
          (device.actorRef ? GetStatus).mapTo[DeviceStatus].map {
            case status: DeviceStatus => { requestor ! status }
          }
        case None => // doing nothing, a timeout will fire
      }

    case SetDeviceStatus(buildingId, roomId, deviceId, status: DeviceStatus) =>
      val requestor = sender.actorRef
      getDevice(buildings, buildingId, roomId, deviceId, requestor) match {
        case Some(device) =>
          (device.actorRef ? status).mapTo[Any]
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
    sender: ActorRef): Option[Set[Dev]] = {

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
    deviceId: String, sender: ActorRef): Option[Dev] = {

    getDevices(buildings, buildingId, roomId, sender) match {
      case Some(devicesSet) =>
        if (!devicesSet.filter(_.id == deviceId).isEmpty) {
          Some(devicesSet.filter(_.id == deviceId).head)
        } else {
          sender ! Failed(new Throwable("No device with given device id."))
          None
        }
      case None => None
    }
  }

}
