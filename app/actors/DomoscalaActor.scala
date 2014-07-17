package actors

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import actors.DomoscalaActor._
import actors.DeviceActor._

class Room(val id: String, val devices: Map[String, ActorRef])
class Building(val id: String, val rooms: Set[Room])

object DomoscalaActor {
  case class GetRooms(buildingId: String)
  case class GetDevices(buildingId: String, roomId: String)
  case class GetDeviceStatus(buildingId: String, roomId: String, deviceId: String)
  case class SetDevicesStatus(buildingId: String, roomId: String, deviceId: String)
}

class DomoscalaActor extends Actor with ActorLogging {

  def receive = init

  def init: Receive = {
    // initializing from sample data
    case _ => {

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

      context.become(main(Set(building)))
    }
  }

  def main(buildings: Set[Building]): Receive = {
    case GetRooms(buildingId) => getRooms(buildings, buildingId, sender)
    case GetDevices(buildingId, roomId) => getDevices(buildings, buildingId, roomId, sender)

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
  }

  /*
   * Utility methods, to get entities and manage limit cases
   */
  def getRooms(buildings: Set[Building], buildingId: String, sender: ActorRef): Option[Set[Room]] = {
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

  def getDevices(buildings: Set[Building], buildingId: String, roomId: String, sender: ActorRef): Option[Map[String, ActorRef]] = {
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

  def getDevice(buildings: Set[Building], buildingId: String, roomId: String, deviceId: String, sender: ActorRef): Option[ActorRef] = {
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