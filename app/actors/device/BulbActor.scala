package actors.device

import actors.DeviceActor._
import actors.MeshnetBase.ToDeviceMessage
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive
import play.libs.Akka
import scala.concurrent.duration._

object BulbActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[BulbActor], name, meshnetActor, deviceId)
}

class BulbActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {

  var isLampOn = false
  var intensityValue = 188 // this is a 8 bit PWM value, ranging from 0 to 255


  override def preStart() = {
    // test code to blink lights :)
    Akka.system.scheduler.schedule(1.6 seconds, 2 seconds, self, On)
    Akka.system.scheduler.schedule(2.6 seconds, 2 seconds, self, Off)
  }


  def receive = LoggingReceive {
    case On => {
      isLampOn = true
      setPwmValue(intensityValue)
      sender ! Ok
    }
    case Off => {
      isLampOn = false
      setPwmValue(0) // switch off lamp
      sender ! Ok
    }
    case SetActivationValue(value) => value match {
      case value if (value < 0.0 || value > 1.0) =>
        sender ! Failed(new Exception("Value out of valid range [0.0 ... 1.0]!"))
      case value =>
        intensityValue = (value * 255).toInt
        isLampOn = true
        setPwmValue(intensityValue)
        sender ! Ok
    }
    case _ => sender ! UnsupportedAction
  }


  val meshnetCommand = 2

  /**
   * This makes a low-level Meshnet message to be sent to the device
   */
  def setPwmValue(pwmValue: Int) = {
    meshnetActor ! ToDeviceMessage(deviceId, meshnetCommand, Array[Byte]((pwmValue-128).toByte))
  }
}