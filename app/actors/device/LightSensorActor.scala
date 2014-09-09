package actors.device

import java.nio.{ ByteOrder, ByteBuffer }

import actors.DeviceActor._
import actors.MeshnetBase.{ FromDeviceMessage, ToDeviceMessage, SubscribeToMessagesFromDevice }
import akka.actor.{ ActorRef, Props, ActorLogging }
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import actors.DeviceActor
import akka.event.LoggingReceive

object LightSensorActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[LightSensorActor], name, meshnetActor, deviceId)
}

class LightSensorActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) with ActorLogging {

  val meshnetCommand = 4

  def receive = LoggingReceive { main(-1) }

  def main(lightValue: Int): Receive = {
    case GetStatus => sender ! LightValue(lightValue)
    case RefreshStatus => askLightToDevice
    case hwMsg: FromDeviceMessage => {
      parseFromDeviceMessage(hwMsg) match {
        case -1 => // nothing to do
        case newLightValue: Int => {

          // publish new value
          Akka.system.eventStream.publish(LightValue(newLightValue, Some(self.path.name)))
          log.debug("received new light value and now publishing: " + newLightValue)

          // update behavior with new value
          context.become(main(newLightValue))
        }
      }
    }
    case _ => sender ! UnsupportedAction
  }

  override def preStart = {
    // subscribe to device messages
    meshnetActor ! SubscribeToMessagesFromDevice(deviceId)

    // periodically ask light value to the device
    Akka.system.scheduler.schedule(3 seconds, 3 seconds, self, RefreshStatus)
  }

  /**
   * Send a Meshnet message to the device asking a temperature reading from the
   * temperature sensor
   */
  def askLightToDevice {
    meshnetActor ! ToDeviceMessage(deviceId, meshnetCommand, Array[Byte](0))
  }

  def parseFromDeviceMessage(msg: FromDeviceMessage): Int = {
    if (msg.command == meshnetCommand) {
      // parsing Meshnet message
      val buf = ByteBuffer.wrap(msg.data)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      buf.position(3)

      // common light value of a room: 450
      // this is the light value, from 0 to 1023 (it's the raw ADC reading from the photocell)
      val analogRead = buf.getShort.toInt & 0xffff
      analogRead
    } else {
      // returning -1 as generic error
      -1
    }
  }
}