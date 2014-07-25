package actors

import akka.actor._
import actors.DeviceActor._
import play.libs.Akka
import actors.DomoscalaActor.GetDevice
import play.api.libs.json._

object DeviceStatusWebSocketActor {
  def props(out: ActorRef, buildingId: String, roomId: String, devId: String) =
    Props(new DeviceStatusWebSocketActor(out, buildingId, roomId, devId))
}

/**
 * An instance of DeviceStatusWebSocketActor will be created for each web socket
 * client. This actor subscribe itself to the Akka Event Bus, and updates its
 * client with a fresh device status value.
 */
class DeviceStatusWebSocketActor(out: ActorRef, buildingId: String,
  roomId: String, deviceId: String) extends Actor with ActorLogging {

  override def preStart() = {
    println("Subscribing to DevicStatus events..")
    val sub = context.system.eventStream.subscribe(self, classOf[DeviceStatus])
    println("Subscribed? " + sub)

    // look for our "root" actor, and look for given device actor
    val domo = Akka.system.actorSelection("user/domoscala")
    domo ! GetDevice(buildingId, roomId, deviceId)
  }

  def init: Receive = {
    case devActor: ActorRef =>
      println("Received device actor" + devActor.toString +
        "Going to listen to events and pushing out..")
      context.become(main(devActor))
  }

  def main(deviceRef: ActorRef): Receive = {
    // receiving from event stream, need to filter since I'm checking for a 
    // specific source actor device
    case status: DeviceStatus if (status.pathName.get == deviceRef.path.name) =>
      out ! Json.toJson(status)
  }

  // initially, we have to wait the ActorRef of our related DeviceActor.
  def receive = init

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
  }
}