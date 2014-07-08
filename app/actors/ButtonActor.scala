package actors

import actors.DeviceActor._
import akka.actor.Props

object ButtonActor {
  def props(name: String): Props = Props(classOf[ButtonActor], name)
}

class ButtonActor(name: String) extends DeviceActor(name) {
  def receive = {
    // TODO add some amazing behavior ;)
//    case Click => sender ! Ok
    case _ => sender ! UnsupportedAction
  }
}