package actors.device.mock

import actors.DeviceActor.UnsupportedAction
import akka.actor.Actor
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import akka.actor.Props
import actors.device.ButtonActor

object ButtonMockActor {
  def props(name: String): Props = Props(classOf[ButtonMockActor], name)
}

class ButtonMockActor(name: String) extends ButtonActor(name, null, -1) {
  override def preStart = {}
}
