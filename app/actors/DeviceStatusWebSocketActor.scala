package actors

import actors.DeviceActor._
import actors.DomoscalaActor.GetDevice
import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json._
import play.libs.Akka

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
    // look for our "root" actor, and look for given device actor
    val domo = Akka.system.actorSelection("user/domoscala")
    domo ! GetDevice(buildingId, roomId, deviceId)
  }

  def init: Receive = LoggingReceive {
    case devActor: ActorRef =>

      val sub = context.system.eventStream.subscribe(self, classOf[DeviceStatus])
//      log.info("Subscribing to dev " + devActor.path.name + " with succ? " + sub)

      context.become(main(devActor))
  }

  def main(deviceRef: ActorRef): Receive = LoggingReceive {
    // receiving from event stream, need to filter since I'm checking for a 
    // specific event source actor device
    case status: DeviceStatus if (status.pathName.get == deviceRef.path.name) =>
      //    case status: DeviceStatus => // only for debugging purpose..
      out ! Json.toJson(status)
  }

  // initially, we have to wait the ActorRef of our related DeviceActor.
  def receive = init

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
  }
}