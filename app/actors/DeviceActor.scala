package actors

import akka.actor.{ Actor, ActorLogging }
import scala.concurrent.duration.Duration

object DeviceActor {

  /**
   * DeviceStatus represents an abstract device status related to a single
   * device actor. This class take the pathName of the actor that generates the
   * status message, since when an actor publish a message on the akka event
   * bus, the sender ActorRef is not preserved.
   */
  abstract class DeviceStatus(val pathName: String)

  class SetActivation(value: Double)
  case object On extends SetActivation(1.0)
  case object Off extends SetActivation(0.0)
  case class SetActivationValue(value: Double) extends SetActivation(value) {
    override def equals(obj: Any) = {
      this.value == obj.asInstanceOf[SetActivationValue].value
    }
  }

  case object GetLightValue

  case class LightValue(lux: Double, override val pathName: String)
    extends DeviceStatus(pathName) {

    override def equals(obj: Any) = {
      this.lux == obj.asInstanceOf[LightValue].lux
    }
  }

  case object GetTemperature

  case class Temperature(celsiusTemp: Double, override val pathName: String)
    extends DeviceStatus(pathName) {

    override def equals(obj: Any) = {
      this.celsiusTemp == obj.asInstanceOf[Temperature].celsiusTemp
    }
  }

  case class MoveServo(degrees: Double) {
    override def equals(obj: Any) = {
      this.degrees == obj.asInstanceOf[MoveServo].degrees
    }
  }

  case class PlayBeep(duration: Duration) {
    override def equals(obj: Any) = {
      this.duration == obj.asInstanceOf[PlayBeep].duration
    }
  }

  object GetSoundValue
  case class SoundValue(decibels: Double, override val pathName: String)
    extends DeviceStatus(pathName) {

    override def equals(obj: Any) = {
      this.decibels == obj.asInstanceOf[SoundValue].decibels
    }
  }

  case object Click // to button

  case object UnsupportedAction
  case object Ok
  case class Failed(error: Throwable) {
    override def equals(obj: Any) = {
      val that = obj.asInstanceOf[Failed]
      this.error.getMessage == that.error.getMessage
    }
  }
}

abstract class DeviceActor(name: String) extends Actor with ActorLogging