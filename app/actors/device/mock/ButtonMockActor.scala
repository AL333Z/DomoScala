package actors.device.mock

import actors.DeviceActor.UnsupportedAction
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current


class ButtonMockActor extends Actor {

  override def preStart = {
    Akka.system.scheduler.schedule(2 seconds, 4 seconds, self, SendClick) // periodically send fake temperature reading
  }

  case object SendClick

  def receive = {
    case SendClick =>
      // TODO what Akka message should I send to the event stream when the button is pressed?
      println("mock button virtually clicked")
    case _ => sender ! UnsupportedAction
  }
}
