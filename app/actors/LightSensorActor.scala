package actors

import actors.DeviceActor._
import akka.actor.Props

object LightSensorActor {
  def props(name: String): Props = Props(classOf[LightSensorActor], name)
}

class LightSensorActor(name: String) extends DeviceActor(name) {
  def receive = {
    //TODO replace with some great behavior ;)
//    case GetLightValue => sender ! LightValue(1.0)
    case _ => sender ! UnsupportedAction
  }
}