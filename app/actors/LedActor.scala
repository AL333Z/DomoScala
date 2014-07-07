package actors

import actors.DeviceActor._
import akka.actor.Props

object LedActor {
  def props(name: String): Props = Props(classOf[LedActor], name)
}

class LedActor(name: String) extends DeviceActor(name) {
  def receive = {
    case _ => sender ! UnsupportedAction
  }
}