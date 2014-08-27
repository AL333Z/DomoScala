package actors

import akka.actor.{ActorRef, Actor, ActorLogging}
import scala.concurrent.duration.Duration
import play.api.libs.json._

/**
 * This object contains all the messages that can be exchanged between an actor
 * and device actors.
 */
object DeviceActor {

  /**
   * DeviceStatus represents an abstract device status related to a single
   * device actor. This class take the pathName of the actor that generates the
   * status message, since when an actor publish a message on the akka event
   * bus, the sender ActorRef is not preserved.
   */
  sealed abstract class DeviceStatus(val pathName: Option[String] = None)
  object DeviceStatus {
    implicit val baseImplicitWrites = new Writes[DeviceStatus] {
      def writes(devStatus: DeviceStatus): JsValue = devStatus match {
        case s: LightValue => LightValue.writes.writes(s)
        case s: TemperatureValue => TemperatureValue.writes.writes(s)
        case s: SoundValue => SoundValue.writes.writes(s)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Logical values /////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Generic class, used to set different logic values
   */
  sealed abstract class SetActivation(value: Double)

  /**
   * Message used to set On logic values
   */
  case object On extends SetActivation(1.0)

  /**
   * Message used to set Off logic values
   */
  case object Off extends SetActivation(0.0)
  case class SetActivationValue(value: Double) extends SetActivation(value) {
    override def equals(obj: Any) = {
      this.value == obj.asInstanceOf[SetActivationValue].value
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Light //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Message used to query current light value
   */
  case object GetLightValue

  /**
   * Response message, containing actual light value
   */
  case class LightValue(lux: Double,
    override val pathName: Option[String] = None) extends DeviceStatus(pathName) {

    override def equals(obj: Any) = {
      this.lux == obj.asInstanceOf[LightValue].lux
    }
  }

  /**
   * Companion object of LightValue
   */
  object LightValue {
    val writes = new Writes[LightValue] {
      def writes(lightValue: LightValue): JsValue = {
        Json.obj("lux" -> lightValue.lux)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Temperature ////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Message used to query current temperature value
   */
  case object GetTemperature

  /**
   * Response message, containing actual temperature value
   */
  case class TemperatureValue(celsiusTemp: Double,
    override val pathName: Option[String] = None) extends DeviceStatus(pathName) {

    override def equals(obj: Any) = {
      this.celsiusTemp == obj.asInstanceOf[TemperatureValue].celsiusTemp
    }
  }

  /**
   * Companion object of Temperature
   */
  object TemperatureValue {
    val writes = new Writes[TemperatureValue] {
      def writes(temp: TemperatureValue): JsValue = {
        Json.obj("temperature" -> temp.celsiusTemp)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Servo //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Message used to fire servo movement
   */
  case class MoveServo(degrees: Double) {
    override def equals(obj: Any) = {
      this.degrees == obj.asInstanceOf[MoveServo].degrees
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Beep ///////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Message used to play a beep
   */
  case class PlayBeep(duration: Duration) {
    override def equals(obj: Any) = {
      this.duration == obj.asInstanceOf[PlayBeep].duration
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Sound value ////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Message used to query current sound value
   */
  object GetSoundValue

  /**
   * Response message, containing actual sound value
   */
  case class SoundValue(decibels: Double,
    override val pathName: Option[String] = None) extends DeviceStatus(pathName) {

    override def equals(obj: Any) = {
      this.decibels == obj.asInstanceOf[SoundValue].decibels
    }
  }

  /**
   * Companion object of SoundValue
   */
  object SoundValue {
    val writes = new Writes[SoundValue] {
      def writes(soundValue: SoundValue): JsValue = {
        Json.obj("decibels" -> soundValue.decibels)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Click //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Message used to fire a click
   */
  case object Click // to button

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Simple Results /////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Response messege, returned when requested action is not supported
   */
  case object UnsupportedAction

  /**
   * Response messege, returned when requested action has been completed
   */
  case object Ok

  /**
   * Response messege, returned when requested action did failed
   */
  case class Failed(error: Throwable) {
    override def equals(obj: Any) = {
      val that = obj.asInstanceOf[Failed]
      this.error.getMessage == that.error.getMessage
    }
  }
}

abstract class DeviceActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends Actor with ActorLogging