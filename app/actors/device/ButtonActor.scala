package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive

object ButtonActor {
  def props(name: String): Props = Props(classOf[ButtonActor], name)
}

class ButtonActor(name: String) extends DeviceActor(name) {
  def receive = LoggingReceive {
    // TODO add some amazing behavior ;)
    case Click => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}