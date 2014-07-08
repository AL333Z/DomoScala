package actors

import actors.DeviceActor._
import akka.actor.Props

object BulbActor {
  def props(name: String): Props = Props(classOf[BulbActor], name)
}

class BulbActor(name: String) extends DeviceActor(name) {
  def receive = {
    //TODO replace with some great behavior ;)
//    case On => sender ! Ok
//    case Off => sender ! Failed(new Throwable("A"))
//    case SetActivationValue(value) => value match {
//      case value if (value < 0.0 || value > 1.0) =>
//        sender ! Failed(new Exception("Value out of valid range [0.0 ... 1.0]!"))
//      case _ => sender ! Ok
//    }
    case _ => sender ! UnsupportedAction
  }
}