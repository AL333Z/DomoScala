package actors

import actors.DeviceActor._
import akka.actor.Props

object SpeakerActor {
  def props(name: String): Props = Props(classOf[SpeakerActor], name)
}

class SpeakerActor(name: String) extends DeviceActor(name) {
  def receive = {
//    case PlayBeep => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}