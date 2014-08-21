package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive

object SwitchActor {
  def props(name: String): Props = Props(classOf[SwitchActor], name)
}

class SwitchActor(name: String) extends DeviceActor(name) {
  def receive = LoggingReceive {
    //TODO add implementation
    case On => sender ! Ok
    case Off => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}