package actors

import actors.DeviceActor._
import akka.actor.Props

object ServoActor {
  def props(name: String): Props = Props(classOf[ServoActor], name)
}

class ServoActor(name: String) extends DeviceActor(name) {
  def receive = {
    case _ => sender ! UnsupportedAction
  }
}