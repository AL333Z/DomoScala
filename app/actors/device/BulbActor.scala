package actors.device

import actors.DeviceActor._
import actors.MeshnetBase.ToDeviceMessage
import akka.actor.{ ActorRef, Props, actorRef2Scala }
import actors.DeviceActor
import akka.event.LoggingReceive
import play.libs.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object BulbActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[BulbActor], name, meshnetActor, deviceId)
}

class BulbActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {

  // intensity is a 8 bit PWM value, ranging from 0 to 255
  val offIntensityValue = 0
  val onIntensityValue = 255
  val meshnetCommand = 2

  // when starting, all devices are off
  def receive = LoggingReceive { main(offIntensityValue) }

  def main(intensityValue: Int): Receive = {
    case ds: DeviceStatus => {
      val actValue = ds
      if (actValue.value < 0.0 || actValue.value > 1.0) {
        sender ! Failed(new Exception("Value out of valid range [0.0 ... 1.0]!"))
      } else {
        val newIntensityValue = (actValue.value * 255).toInt
        setPwmValue(newIntensityValue)
        log.debug("set new value " + newIntensityValue)

        // publish new value
        Akka.system.eventStream.publish(ActivationValue(newIntensityValue, Some(self.path.name)))
        log.debug("received new activation value and now publishing: " + newIntensityValue)

        context.become(main(newIntensityValue))
        sender ! Ok
      }
    }
    case GetStatus => sender ! new ActivationValue(intensityValue / 255.0f)
    case _ => sender ! UnsupportedAction
  }

  /**
   * This makes a low-level Meshnet message to be sent to the device
   */
  def setPwmValue(pwmValue: Int) = {
    //TODO find a better way to convert data to Java sh*t Byte stuff
    println("Sending command to meshnet..")
    meshnetActor ! ToDeviceMessage(deviceId, meshnetCommand, Array[Byte]((pwmValue - 128).toByte))
  }
}