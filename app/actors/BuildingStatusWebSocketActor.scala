package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json._
import play.libs.Akka
import actors.DomoscalaActor.GetRooms
import actors.RoomStatusWebSocketActor

object BuildingStatusWebSocketActor {
  def props(out: ActorRef, buildingId: String) =
    Props(new BuildingStatusWebSocketActor(out, buildingId))
}

/**
 * An instance of BuildingStatusWebSocketActor will be created for each web socket
 * client asking for push event from a building. This actor generates child
 * actors to get events from rooms.
 */
class BuildingStatusWebSocketActor(out: ActorRef, buildingId: String) extends Actor with ActorLogging {

  override def preStart() = {
    // look for our "root" actor, and look for given room actor
    val domo = Akka.system.actorSelection("user/domoscala")
    domo ! GetRooms(buildingId)
  }

  def init: Receive = LoggingReceive {
    case res: Set[_] =>
      res.asInstanceOf[Set[Room]].foreach { room =>
        context.actorOf(RoomStatusWebSocketActor.props(self, buildingId, room.id))
      }

      context.become(main)
  }

  def main: Receive = LoggingReceive {
    // receiving from rooms, forwarding to out
    case msg => out forward msg
  }

  // initially, we have to wait to receive devices of selected room
  def receive = init

}