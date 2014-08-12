package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive

object ThermometerActor {
  def props(name: String): Props = Props(classOf[ThermometerActor], name)
}

class ThermometerActor(name: String) extends DeviceActor(name) {
  def receive = LoggingReceive {
    case GetTemperature => sender ! Temperature(30.0f)
    case _ => sender ! UnsupportedAction
  }
}