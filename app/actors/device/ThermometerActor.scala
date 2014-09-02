package actors.device

import java.nio.{ByteOrder, ByteBuffer}

import actors.DeviceActor._
import actors.MeshnetBase.{FromDeviceMessage, SubscribeToMessagesFromDevice, ToDeviceMessage}
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._


object ThermometerActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[ThermometerActor], name, meshnetActor, deviceId)
}

class ThermometerActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {

  /**
   * This is a sort of cached temperature value
   */
  var currTemp: Option[Double] = None


  override def preStart() = {
    meshnetActor ! SubscribeToMessagesFromDevice(deviceId)
    Akka.system.scheduler.schedule(2 seconds, 3 seconds, self, AskTemp) // periodically ask temperature to the device
  }

  case object AskTemp

  def receive = LoggingReceive {
    case GetTemperature => currTemp.foreach(sender ! TemperatureValue(_))
    case AskTemp => askTemperatureToDevice()
    case msg: FromDeviceMessage => parseFromDeviceMessage(msg)
    case _ => sender ! UnsupportedAction
  }

  val meshnetCommand = 3

  /**
   * Send a Meshnet message to the device asking a temperature reading from the temperature sensor
   */
  def askTemperatureToDevice(){
    meshnetActor ! ToDeviceMessage(deviceId, meshnetCommand, Array[Byte](0))
  }

  /**
   * Handle a message coming from the Meshnet device that hopefully contains a temperature value
   */
  def parseFromDeviceMessage(msg: FromDeviceMessage){
    if(msg.command == meshnetCommand){

      // very low level scary stuff!!
      val buf = ByteBuffer.wrap(msg.data)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      buf.position(3)
      val analogRead = buf.getShort.toInt & 0xffff // this is the ADC reading from the thermistor
      val pad = 11000d; // balance/pad resistor resistance value
      val thermistorResistance = (1024 * pad / analogRead.toDouble) - pad
      val logRes = Math.log(thermistorResistance)
      val tempKelvin = 1 / (0.001129148 + (0.000234125 * logRes) + (0.0000000876741 * math.pow(logRes,3))) // magical numbers, yay!!
      val tempCelsius = tempKelvin - 273.15

      // back to Scala...
      currTemp = Some(tempCelsius)
      Akka.system.eventStream.publish(TemperatureValue(tempCelsius, Some(self.path.name)))
      println("temperature: "+tempCelsius)
    }
  }
}
