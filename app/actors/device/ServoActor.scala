package actors.device

import actors.DeviceActor._
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive

object ServoActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[ServoActor], name, meshnetActor, deviceId)
}

class ServoActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {
  def receive = LoggingReceive {
    // TODO
    case MoveServo => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}