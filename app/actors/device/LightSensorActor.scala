package actors.device

import java.nio.{ByteOrder, ByteBuffer}

import actors.DeviceActor._
import actors.MeshnetBase.{FromDeviceMessage, ToDeviceMessage, SubscribeToMessagesFromDevice}
import akka.actor.{ActorRef, Props, ActorLogging}
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

  /**
   * This is a sort of cached light value
   */
  var currLight: Option[Double] = None

  def receive = LoggingReceive {
    case GetLightValue => currLight.foreach(sender ! LightValue(_))
    case AskLightValue => askLightToDevice()
    case msg: FromDeviceMessage =>
    case _ => sender ! UnsupportedAction
  }

  override def preStart() = {
    meshnetActor ! SubscribeToMessagesFromDevice(deviceId)
    Akka.system.scheduler.schedule(3 seconds, 3 seconds, self, AskLightValue) // periodically ask light value to the device
  }

  case object AskLightValue

  val meshnetCommand = 4

  /**
   * Send a Meshnet message to the device asking a temperature reading from the temperature sensor
   */
  def askLightToDevice(){
    meshnetActor ! ToDeviceMessage(deviceId, meshnetCommand, Array[Byte](0))
  }

  def parseFromDeviceMessage(msg: FromDeviceMessage){
    if(msg.command == meshnetCommand){

      // parsing Meshnet message
      val buf = ByteBuffer.wrap(msg.data)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      buf.position(3)
      val analogRead = buf.getShort.toInt & 0xffff // this is the light value, from 0 to 1023 (it's the raw ADC reading from the photocell)

      // common light value of a room: 450

      currLight = Some(analogRead)
      Akka.system.eventStream.publish(LightValue(analogRead, Some(self.path.name)))
      println("light value: "+analogRead)
    }
  }
}