package actors.device.mock

import actors.DeviceActor.{ UnsupportedAction, TemperatureValue }
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import scala.util.Random
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import actors.DeviceActor._
import akka.actor.Props
import actors.device.ThermometerActor
import actors.MeshnetBase._
import java.nio.ByteBuffer

object ThermometerMockActor {
  def props(name: String): Props = Props(classOf[ThermometerMockActor], name)
}

class ThermometerMockActor(name: String) extends ThermometerActor(name, null, -1) {

  // mock actor avoids interaction with hw
  override def preStart = {
    // periodically ask temp value to the device
    Akka.system.scheduler.schedule(3 seconds, 3 seconds, self, RefreshStatus)
  }

  override def askTemperatureToDevice = {
    // instead of querying the device, create a response directly
    self ! FromDeviceMessage(-1, -1, null)
  }

  override def parseFromDeviceMessage(msg: FromDeviceMessage): Double = {
    // given a response from the device, return read value (since we are just 
    // mocking, only returning a random value is enough)
    val value = (10.0f + (Random.nextDouble() * 20))
    val roundedValue = (math rint value * 100) / 100
    roundedValue
  }
}
