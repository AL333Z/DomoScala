package actors.device

import actors.DeviceActor._
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor.ActorLogging
import scala.util.Random
import actors.DeviceActor
import akka.actor.actorRef2Scala

object LightSensorActor {
  def props(name: String): Props = Props(classOf[LightSensorActor], name)
}

class LightSensorActor(name: String) extends DeviceActor(name) with ActorLogging {

  //TODO publish values from a hot observable :), not from a scheduler...
  Akka.system.scheduler.schedule(1000 milliseconds, 10000 milliseconds) {
    println("Gonna pubblish new value..")
    Akka.system.eventStream.publish(LightValue(Random.nextFloat, self.path.name))
  }

  def receive = {
    //TODO replace with some great behavior ;)
    //    case GetLightValue => sender ! LightValue(1.0)
    case _ => sender ! UnsupportedAction
  }
}