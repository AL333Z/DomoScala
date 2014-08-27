package actors.device

import actors.DeviceActor._
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive

object SwitchActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[SwitchActor], name, meshnetActor, deviceId)
}

class SwitchActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {
  def receive = LoggingReceive {
    //TODO add implementation
    case On => sender ! Ok
    case Off => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}