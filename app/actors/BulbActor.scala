package actors

import actors.DeviceActor._
import akka.actor.Props

object BulbActor {
  def props(name: String): Props = Props(classOf[BulbActor], name)
}

class BulbActor(name: String) extends DeviceActor(name) {
  def receive = {
    //TODO replace with some great behavior ;)
    case On => sender ! Ok
    case Off => sender ! Failed(new Throwable("A"))
    case SetActivationValue(value) => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}