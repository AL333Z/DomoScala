package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json._
import play.libs.Akka
import actors.DomoscalaActor.GetDevices

object RoomStatusWebSocketActor {
  def props(out: ActorRef, buildingId: String, roomId: String) =
    Props(new RoomStatusWebSocketActor(out, buildingId, roomId))
}

/**
 * An instance of RoomStatusWebSocketActor will be created for each web socket
 * client asking for push event from a single room. This actor generates child
 * actors to get events from devices.
 */
class RoomStatusWebSocketActor(out: ActorRef, buildingId: String,
  roomId: String) extends Actor with ActorLogging {

  override def preStart() = {
    // look for our "root" actor, and look for given room actor
    val domo = Akka.system.actorSelection("user/domoscala")
    domo ! GetDevices(buildingId, roomId)
  }

  def init: Receive = LoggingReceive {
    case devices: Map[_, _] =>
      devices.asInstanceOf[Map[String, ActorRef]].map {
        case (id, actor) =>
          context.actorOf(DeviceStatusWebSocketActor.props(self, buildingId, roomId, id))
      }
      context.become(main)
  }

  def main: Receive = LoggingReceive {
    // receiving from devices, forwarding to out
    case msg => out forward msg
  }

  // initially, we have to wait to receive devices of selected room
  def receive = init

}