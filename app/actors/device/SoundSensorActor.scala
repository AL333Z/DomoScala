package actors.device

import actors.DeviceActor._
import akka.actor.Props
import actors.DeviceActor

import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random

object SoundSensorActor {
  def props(name: String): Props = Props(classOf[SoundSensorActor], name)
}

class SoundSensorActor(name: String) extends DeviceActor(name) {

  //TODO publish values from a hot observable :), not from a scheduler...
//  Akka.system.scheduler.schedule(1000 milliseconds, 10000 milliseconds) {
//    println("Gonna pubblish new sound value..")
//    Akka.system.eventStream.publish(SoundValue(Random.nextFloat, Some(self.path.name)))
//  }

  def receive = {
    case GetSoundValue => sender ! SoundValue(1.0)
    case _ => sender ! UnsupportedAction
  }
}