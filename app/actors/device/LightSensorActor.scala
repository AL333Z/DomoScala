package actors.device

import actors.DeviceActor._
import akka.actor.Props
import akka.actor.ActorLogging
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random
import actors.DeviceActor
import akka.event.LoggingReceive

object LightSensorActor {
  def props(name: String): Props = Props(classOf[LightSensorActor], name)
}

class LightSensorActor(name: String) extends DeviceActor(name) with ActorLogging {

  //TODO publish values from a hot observable :), not from a scheduler...
  // scheduler doesnt run when testing
  Akka.system.scheduler.schedule(2000 milliseconds, 2000 milliseconds) {
    println("Gonna pubblish new light value..")
    Akka.system.eventStream.publish(LightValue(Random.nextFloat, Some(self.path.name)))
  }

  def receive = LoggingReceive {
    //TODO replace with some great behavior ;)
    case GetLightValue => sender ! LightValue(1.0)
    case _ => sender ! UnsupportedAction
  }
}