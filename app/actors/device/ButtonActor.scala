package actors.device

import actors.DeviceActor._
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive

object ButtonActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[ButtonActor], name, meshnetActor, deviceId)
}

class ButtonActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {
  def receive = LoggingReceive {
    // TODO add some amazing behavior ;)
    case Click => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}