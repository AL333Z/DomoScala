package actors.device.mock

import actors.DeviceActor._
import akka.actor.{ActorLogging, Actor}

/**
 * A fake bulb device that just print something in the log when its state changes
 */
class BulbMockActor extends Actor with ActorLogging {

  def receive = {
    case On =>
      log.info("light switched ON")
      sender ! Ok
    case Off =>
      log.info("light switched OFF")
      sender ! Ok
    case SetActivationValue(value) => value match {
      case value if (value < 0.0 || value > 1.0) =>
        sender ! Failed(new Exception("Value out of valid range [0.0 ... 1.0]!"))
      case value =>
        log.info("light set to intensity "+(value*100).toInt+"%")
        sender ! Ok
    }
    case _ => sender ! UnsupportedAction
  }

}
