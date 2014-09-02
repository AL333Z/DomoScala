package actors.device.mock

import actors.DeviceActor.{UnsupportedAction, GetTemperature, TemperatureValue}
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import scala.util.Random
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current


class ThermometerMockActor extends Actor{

  override def preStart = {
    Akka.system.scheduler.schedule(2 seconds, 3 seconds, self, SendTemp) // periodically send fake temperature reading
  }

  case object SendTemp

  def receive = {
    case SendTemp =>
      Akka.system.eventStream.publish(TemperatureValue(fakeTemp, Some(self.path.name)))
    case GetTemperature =>
      sender ! TemperatureValue(fakeTemp)
    case _ => sender ! UnsupportedAction
  }

  def fakeTemp = 10 + (Random.nextDouble()*20)

}
