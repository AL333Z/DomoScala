package actors

import actors.DeviceActor._
import akka.actor.Props

object SoundSensorActor {
  def props(name: String): Props = Props(classOf[SoundSensorActor], name)
}

class SoundSensorActor(name: String) extends DeviceActor(name) {
  def receive = {
//    case GetSoundValue => sender ! SoundValue(1.0)
    case _ => sender ! UnsupportedAction
  }
}