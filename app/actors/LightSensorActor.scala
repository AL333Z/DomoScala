package actors

import actors.DeviceActor._
import akka.actor.Props

object LightSensorActor {
  def props(name: String): Props = Props(classOf[LightSensorActor], name)
}

class LightSensorActor(name: String) extends DeviceActor(name) {
  def receive = {
    case _ => sender ! UnsupportedAction
  }
}