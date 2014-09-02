package actors.device.mock

import actors.DeviceActor._
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random
import play.api.Play.current
import play.api.Play.current


class LightSensorMockActor extends Actor {

  override def preStart = {
    Akka.system.scheduler.schedule(3 seconds, 3 seconds, self, SendLight) // periodically send fake light reading
  }

  case object SendLight

  def receive = {
    case SendLight =>
      Akka.system.eventStream.publish(LightValue(fakeLight, Some(self.path.name)))
    case GetLightValue =>
      sender ! LightValue(fakeLight)
    case _ => sender ! UnsupportedAction
  }

  def fakeLight = Random.nextInt(1023)

}
