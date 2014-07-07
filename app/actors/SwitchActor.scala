package actors

import actors.DeviceActor._
import akka.actor.Props

object SwitchActor {
  def props(name: String): Props = Props(classOf[SwitchActor], name)
}

class SwitchActor(name: String) extends DeviceActor(name) {
  def receive = {
    case _ => sender ! UnsupportedAction
  }
}