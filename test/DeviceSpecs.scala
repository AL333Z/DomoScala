import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Subscribe
import scala.concurrent.duration._
import actors.DeviceActor
import actors.DeviceActor._
import actors.BulbActor
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import akka.testkit.TestProbe

@RunWith(classOf[JUnitRunner])
class DevicesSpec(_system: ActorSystem) extends TestKit(_system)
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("DomoscalaDevicesSpec"))

  override def afterAll: Unit = system.shutdown()

  "A Bulb" should "respond properly only to On/Off/SetActivationValue" in {

    val testProbe = TestProbe()
    val bulbProbe = system.actorOf(BulbActor.props("bulb1"))

    testProbe.send(bulbProbe, On)
    testProbe.expectMsg(5.second, Ok)

    testProbe.send(bulbProbe, Off)
    testProbe.expectMsg(5.second, Ok)

    testProbe.send(bulbProbe, SetActivationValue(0.5))
    testProbe.expectMsg(5.second, Ok)

    testProbe.send(bulbProbe, GetLightValue)
    testProbe.expectMsg(5 seconds, UnsupportedAction)

    testProbe.send(bulbProbe, GetTemperature)
    testProbe.expectMsg(5 seconds, UnsupportedAction)

    testProbe.send(bulbProbe, MoveServo)
    testProbe.expectMsg(5 seconds, UnsupportedAction)

    testProbe.send(bulbProbe, PlayBeep)
    testProbe.expectMsg(5 seconds, UnsupportedAction)

    testProbe.send(bulbProbe, GetSoundValue)
    testProbe.expectMsg(5 seconds, UnsupportedAction)

    testProbe.send(bulbProbe, GetSwitchState)
    testProbe.expectMsg(5 seconds, UnsupportedAction)

    testProbe.send(bulbProbe, Click)
    testProbe.expectMsg(5 seconds, UnsupportedAction)
  }

}