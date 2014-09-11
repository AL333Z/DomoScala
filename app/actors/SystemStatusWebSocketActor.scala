package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.libs.json._
import play.libs.Akka
import actors.DomoscalaActor.GetBuildings

object SystemStatusWebSocketActor {
  def props(out: ActorRef) =
    Props(new SystemStatusWebSocketActor(out))
}

/**
 * An instance of SystemStatusWebSocketActor will be created for each web socket
 * client asking for push event for the whole system events. This actor
 * generates child actors to get events from buildings.
 */
class SystemStatusWebSocketActor(out: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    // look for our "root" actor, and get buildings
    val domo = Akka.system.actorSelection("user/domoscala")
    println("pre-start building.")
    domo ! GetBuildings
  }

  def init: Receive = LoggingReceive {
    case res: Set[_] =>
      res.asInstanceOf[Set[Building]].foreach { building =>
        context.actorOf(BuildingStatusWebSocketActor.props(self, building.id))
      }

      context.become(main)
  }

  def main: Receive = LoggingReceive {
    // receiving from buildings, forwarding to out
    case msg => out forward msg
  }

  // initially, we have to wait to receive buildings
  def receive = init

}