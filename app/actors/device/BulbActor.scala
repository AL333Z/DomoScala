package actors.device

import actors.DeviceActor._
import actors.MeshnetBase.ToDeviceMessage
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive

object BulbActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[BulbActor], name, meshnetActor, deviceId)
}

class BulbActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {

  var isLampOn = false

  def receive = LoggingReceive {
    case On => {
      setLampState(true)
      sender ! Ok
    }
    case Off => {
      setLampState(false)
      sender ! Ok
    }
    case SetActivationValue(value) => value match {
      case value if (value < 0.0 || value > 1.0) =>
        sender ! Failed(new Exception("Value out of valid range [0.0 ... 1.0]!"))
      case value if (value < 0.5) =>
        setLampState(false)
        sender ! Ok
      case value if(value >= 0.5) =>
        setLampState(true)
        sender ! Ok
    }
    case _ => sender ! UnsupportedAction
  }

  def setLampState(lampOn: Boolean){
    meshnetActor ! lampSwitchMessage(lampOn)
    isLampOn = lampOn
  }

  /**
   * This makes a low-level Meshnet message to be sent to the device
   */
  def lampSwitchMessage(lampOn: Boolean) = {
    ToDeviceMessage(deviceId, 2, Array[Byte](
      lampOn match {
        case true => 60
        case false => 0
      }
    ))
  }
}