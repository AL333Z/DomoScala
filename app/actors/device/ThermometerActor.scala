package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor
import akka.event.LoggingReceive
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random


object ThermometerActor {
  def props(name: String): Props = Props(classOf[ThermometerActor], name)
}

class ThermometerActor(name: String) extends DeviceActor(name) {

  Akka.system.scheduler.schedule(1000 milliseconds, 2000 milliseconds) {
    println("Gonna pubblish new temperature value..")
    Akka.system.eventStream.publish(TemperatureValue(Random.nextFloat, Some(self.path.name)))
  }

  def receive = LoggingReceive {
    case GetTemperature => sender ! TemperatureValue(30.0f)
    case _ => sender ! UnsupportedAction
  }
}