package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive

object SpeakerActor {
  def props(name: String): Props = Props(classOf[SpeakerActor], name)
}

class SpeakerActor(name: String) extends DeviceActor(name) {
  def receive = LoggingReceive {
    case PlayBeep => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}