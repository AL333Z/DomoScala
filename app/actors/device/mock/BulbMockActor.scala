package actors.device.mock

import actors.DeviceActor._
import akka.actor.{ ActorLogging, Actor }
import actors.device.BulbActor
import akka.actor.Props

object BulbMockActor {
  def props(name: String): Props = Props(classOf[BulbMockActor], name)
}

/**
 * A fake bulb device that just print something in the log when its state changes
 */
class BulbMockActor(name: String) extends BulbActor(name, null, -1) {
  // mock actor avoids interaction with hw
  override def setPwmValue(pwmValue: Int) = {}
}
