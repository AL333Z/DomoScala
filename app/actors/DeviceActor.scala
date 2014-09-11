package actors

import akka.actor.{ ActorRef, Actor, ActorLogging }
import scala.concurrent.duration.Duration
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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
  sealed abstract class AbstractDeviceStatus(val value: Double, val um: String, val pathName: Option[String] = None)
  class DeviceStatus(override val value: Double, override val um: String, override val pathName: Option[String] = None) extends AbstractDeviceStatus(value, um, pathName)

  object DeviceStatus {
    implicit val baseImplicitWrites = new Writes[DeviceStatus] {
      def writes(devStatus: DeviceStatus): JsValue = devStatus match {
        case s: LightValue => LightValue.writes(s)
        case s: TemperatureValue => TemperatureValue.writes(s)
        case s: SoundValue => SoundValue.writes(s)
        case s: ActivationValue => ActivationValue.writes(s)
      }
    }

    implicit val reads: Reads[DeviceStatus] = (
      (JsPath \ "value").read[Double] ~
      (JsPath \ "um").read[String] ~
      (JsPath \ "pathName").readNullable[String])((value: Double, um: String, pathName: Option[String]) =>
        new DeviceStatus(value, um, pathName))
  }

  /**
   * Message used to force refresh current device value, that may not be the most
   * updated. Sending RefreshStatus and then GetStatus guarantees that returned
   * value is up to date.
   */
  case object RefreshStatus

  /**
   * Message used to query current device value, that may not be the most
   * updated
   */
  case object GetStatus

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Logical values /////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Class used to set different logic values
   */
  case class ActivationValue(override val value: Double, override val pathName: Option[String] = None) extends DeviceStatus(value, "doubleValue", pathName) {
    override def equals(obj: Any) = {
      this.value == obj.asInstanceOf[ActivationValue].value
    }
  }

  object ActivationValue {
    def writes(activationValue: ActivationValue): JsValue = {
      Json.obj("value" -> activationValue.value, "um" -> activationValue.um)
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Light //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Response message, containing actual light value
   */
  case class LightValue(override val value: Double,
    override val pathName: Option[String] = None) extends DeviceStatus(value, "lux", pathName) {

    override def equals(obj: Any) = {
      this.value == obj.asInstanceOf[LightValue].value
    }
  }

  /**
   * Companion object of LightValue
   */
  object LightValue {
    def writes(lightValue: LightValue): JsValue = {
      Json.obj("value" -> lightValue.value, "um" -> lightValue.um)
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Temperature ////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Response message, containing actual temperature value
   */
  case class TemperatureValue(override val value: Double,
    override val pathName: Option[String] = None) extends DeviceStatus(value, "celsius", pathName) {

    override def equals(obj: Any) = {
      this.value == obj.asInstanceOf[TemperatureValue].value
    }
  }

  /**
   * Companion object of Temperature
   */
  object TemperatureValue {
    def writes(temp: TemperatureValue): JsValue = {
      Json.obj("value" -> temp.value, "um" -> temp.um)
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Sound value ////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Response message, containing actual sound value
   */
  case class SoundValue(override val value: Double,
    override val pathName: Option[String] = None) extends DeviceStatus(value, "decibels", pathName) {

    override def equals(obj: Any) = {
      this.value == obj.asInstanceOf[SoundValue].value
    }
  }

  /**
   * Companion object of SoundValue
   */
  object SoundValue {
    def writes(soundValue: SoundValue): JsValue = {
      Json.obj("value" -> soundValue.value, "um" -> soundValue.um)
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Simple Results /////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Response message, returned when requested action is not supported
   */
  case object UnsupportedAction

  /**
   * Response message, returned when requested action has been completed
   */
  case object Ok

  /**
   * Response message, returned when requested action did failed
   */
  case class Failed(error: Throwable) {
    override def equals(obj: Any) = {
      val that = obj.asInstanceOf[Failed]
      this.error.getMessage == that.error.getMessage
    }
  }

  val bulbType = "bulb"
  val tempType = "temp"
  val lightType = "light"
  val buttonType = "button"

}

abstract class DeviceActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends Actor with ActorLogging
