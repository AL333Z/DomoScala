package actors.device

import actors.DeviceActor._
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive

object ThermometerActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[ThermometerActor], name, meshnetActor, deviceId)
}

class ThermometerActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {
  def receive = LoggingReceive {
    case GetTemperature => sender ! TemperatureValue(30.0f)
    case _ => sender ! UnsupportedAction
  }
}