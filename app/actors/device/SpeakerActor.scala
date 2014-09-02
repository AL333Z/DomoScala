package actors.device

import actors.DeviceActor._
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive

object SpeakerActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[SpeakerActor], name, meshnetActor, deviceId)
}

class SpeakerActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {
  def receive = LoggingReceive {
    case PlayBeep => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}