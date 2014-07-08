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

  def expectUnsupportedAction(actor: TestProbe) = {
    actor.expectMsg(5 seconds, UnsupportedAction)
  }

  "A Bulb" should "respond properly only to On/Off/SetActivationValue" in {

    val testProbe = TestProbe()
    val bulbProbe = system.actorOf(BulbActor.props("bulb1"))

    def expectAnyValidBulbMsg = {
      testProbe.expectMsgAnyOf(5.second, Ok, Failed(new Throwable("A")), SetActivationValue)
    }

    def sendValidActionAndExpectValidBulbMsg(msg: AnyRef) = {
      testProbe.send(bulbProbe, msg)
      expectAnyValidBulbMsg
    }

    def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
      testProbe.send(bulbProbe, msg)
      expectUnsupportedAction(testProbe)
    }

    sendValidActionAndExpectValidBulbMsg(On)
    sendValidActionAndExpectValidBulbMsg(Off)
    sendValidActionAndExpectValidBulbMsg(SetActivationValue(0.5))

    sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
    sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
    sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
    sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
    sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
    sendUnsupportedMsgAndExpectUnsupportedAction(GetSwitchState)
    sendUnsupportedMsgAndExpectUnsupportedAction(Click)
  }

}