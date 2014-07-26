package actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import actors.DomoscalaActor._
import actors.DeviceActor._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import actors.device._

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
  object GetBuildings
  case class GetRooms(buildingId: String)
  case class GetDevices(buildingId: String, roomId: String)
  case class GetDevice(buildingId: String, roomId: String, deviceId: String)
  case class GetDeviceStatus(buildingId: String, roomId: String, deviceId: String)
  case class SetDevicesStatus(buildingId: String, roomId: String, deviceId: String)
}

/**
 * This actor wraps the structure of the system.
 * It contains a `Set` of `Building`. This set can be also dynamically changed.
 * The structure of the system is returned only via message-passing.
 */
class DomoscalaActor extends Actor with ActorLogging {

  def receive = init

  def init: Receive = {
    case Some(meshnetActorRef) => {
      //TODO take some configuration from somewhere (DB, json file, ...)

      log.info("Time to do some serious implementation :)")

    }
    case None => {
      // we are just simultating stuff, create some demo actors
      val bulbActor0 = context.actorOf(BulbActor.props("Bulb0"))
      val bulbActor1 = context.actorOf(BulbActor.props("Bulb1"))
      val bulbActor2 = context.actorOf(BulbActor.props("Bulb2"))
      val bulbActor3 = context.actorOf(BulbActor.props("Bulb3"))
      val buttonActor0 = context.actorOf(ButtonActor.props("Button0"))
      val lightSensor0 = context.actorOf(LightSensorActor.props("LightSensor0"))
      val servo0 = context.actorOf(ServoActor.props("Servo0"))
      val soundSensor0 = context.actorOf(SoundSensorActor.props("SoundSensor0"))
      val speakeSensor0 = context.actorOf(SpeakerActor.props("SpeakerSensor0"))

      val room0 = new Room("Room0", Map("Bulb0" -> bulbActor0,
        "Button0" -> buttonActor0, "SoundSensor0" -> soundSensor0))
      val room1 = new Room("Room1", Map("Bulb1" -> bulbActor1,
        "LightSensor0" -> lightSensor0, "Servo0" -> servo0))
      val room2 = new Room("Room2", Map("Bulb2" -> bulbActor2,
        "Bulb3" -> bulbActor3, "SpeakerSensor0" -> speakeSensor0))

      val building = new Building("Building0", Set(room0, room1, room2))

      log.info("All demo actors created, starting main behavior..")

      context.become(main(Set(building), None))
    }
  }

  def main(buildings: Set[Building],
    meshnetActorRef: Option[ActorRef]): Receive = {

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

    //TODO implementation
    case GetDeviceStatus(buildingId, roomId, deviceId) =>
      getDevice(buildings, buildingId, roomId, deviceId, sender) match {
        case Some(devActorRef) =>
        case None =>
      }

    case SetDevicesStatus(buildingId, roomId, deviceId) =>
      getDevice(buildings, buildingId, roomId, deviceId, sender) match {
        case Some(devActorRef) =>
        case None =>
      }

    case _ => sender ! UnsupportedAction
    //TODO implement dynamic add-in of building/room/devices
  }

  /*
   * Utility methods, to get entities and manage limit cases
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