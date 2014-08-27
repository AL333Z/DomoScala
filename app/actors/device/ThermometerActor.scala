package actors.device

import actors.DeviceActor._
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random


object ThermometerActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[ThermometerActor], name, meshnetActor, deviceId)
}

class ThermometerActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {

  Akka.system.scheduler.schedule(1000 milliseconds, 2000 milliseconds) {
    println("Gonna pubblish new temperature value..")
    Akka.system.eventStream.publish(TemperatureValue(Random.nextFloat, Some(self.path.name)))
  }

  def receive = LoggingReceive {
    case GetTemperature => sender ! TemperatureValue(30.0f)
    case _ => sender ! UnsupportedAction
  }
}
