package actors

import actors.DeviceActor._
import akka.actor.Props

object BulbActor {
  def props(name: String): Props = Props(classOf[BulbActor], name)
}

class BulbActor(name: String) extends DeviceActor(name) {
  def receive = {
    case On => sender ! Ok
    case Off => sender ! Ok
    case SetActivationValue(value) => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}