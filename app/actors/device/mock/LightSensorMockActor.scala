package actors.device.mock

import actors.DeviceActor._
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Random
import play.api.Play.current
import play.api.Play.current
import akka.actor.Props
import actors.device.LightSensorActor
import actors.MeshnetBase._
import java.nio.ByteBuffer

object LightSensorMockActor {
  def props(name: String): Props = Props(classOf[LightSensorMockActor], name)
}

class LightSensorMockActor(name: String) extends LightSensorActor(name, null, -1) {

  // mock actor avoids interaction with hw
  override def preStart = {
    // periodically ask light value to the device
    Akka.system.scheduler.schedule(3 seconds, 3 seconds, self, RefreshStatus)
  }

  override def askLightToDevice = {
    // instead of querying the device, create a response directly
    self ! FromDeviceMessage(-1, -1, null)
  }

  override def parseFromDeviceMessage(msg: FromDeviceMessage): Int = {
    // given a response from the device, return read value (since we are just 
    // mocking, only returning a random value is enough)
    Random.nextInt(1023)
  }

}
