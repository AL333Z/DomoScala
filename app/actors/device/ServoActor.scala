package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor
import akka.actor.actorRef2Scala

object ServoActor {
  def props(name: String): Props = Props(classOf[ServoActor], name)
}

class ServoActor(name: String) extends DeviceActor(name) {
  def receive = {
    case MoveServo => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}