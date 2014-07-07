package actors

import actors.DeviceActor._
import akka.actor.Props

object ThermometerActor {
  def props(name: String): Props = Props(classOf[ThermometerActor], name)
}

class ThermometerActor(name: String) extends DeviceActor(name) {
  def receive = {
    case _ => sender ! UnsupportedAction
  }
}