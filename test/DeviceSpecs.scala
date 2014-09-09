import actors.DeviceActor._
import actors.device.mock.{ BulbMockActor, LightSensorMockActor, ThermometerMockActor }
import akka.actor.Props
import akka.testkit.TestProbe
import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }
import play.api.Play
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import actors.device.mock._
import org.openqa.selenium.internal.seleniumemulation.GetValue

class DevicesSpec extends PlaySpec with OneAppPerSuite {

  implicit val system = Akka.system(app)

  "The DevicesSpec" must {
    "start the FakeApplication" in {
      Play.maybeApplication mustBe Some(app)
    }
  }

  def expectUnsupportedAction(actor: TestProbe) = {
    actor.expectMsg(5 seconds, UnsupportedAction)
  }

  "A BulbActor" must {
    "respond properly only to ActivationValue/GetStatus/AskStatus messages" in {

      val testProbe = TestProbe()
      val bulb = system.actorOf(BulbMockActor.props("Bulb0"))

      def expectAnyValidBulbMsg = {
        testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], classOf[ActivationValue], Ok.getClass)
      }

      def sendValidActionAndExpectValidBulbMsg(msg: AnyRef) = {
        testProbe.send(bulb, msg)
        expectAnyValidBulbMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(bulb, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidBulbMsg(ActivationValue(0.5))
      sendValidActionAndExpectValidBulbMsg(ActivationValue(10.5))
      sendValidActionAndExpectValidBulbMsg(GetStatus)

      sendUnsupportedMsgAndExpectUnsupportedAction(LightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(TemperatureValue)
    }

  }

  /*
  // TODO the button is only a sensor! when the user physically click it, it sends a message that we should handle
  "A ButtonActor" must {
    "respond properly only to Click" in {

      val testProbe = TestProbe()
      val button = system.actorOf(ButtonMockActor.props("button1"))

      def expectAnyValidButtonMsg = {
        testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], Ok.getClass)
      }

      def sendValidActionAndExpectValidButtonMsg(msg: AnyRef) = {
        testProbe.send(button, msg)
        expectAnyValidButtonMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(button, msg)
        expectUnsupportedAction(testProbe)
      }
    }
  }
  */

  "A LightSensorActor" must {
    "respond properly only to GetStatus/AskStatus messages" in {

      val testProbe = TestProbe()
      val lightSensor = system.actorOf(LightSensorMockActor.props("Light0"))

      def expectAnyValidLightSensorMsg = {
        testProbe.expectMsgAnyClassOf(5.second,
          classOf[Failed], classOf[LightValue])
      }

      def sendValidActionAndExpectValidLightSensorMsg(msg: AnyRef) = {
        testProbe.send(lightSensor, msg)
        expectAnyValidLightSensorMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(lightSensor, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidLightSensorMsg(GetStatus)
      sendUnsupportedMsgAndExpectUnsupportedAction(ActivationValue(0.5))
    }
  }

  "A ThermometerActor" must {
    "respond properly only to GetStatus/AskStatus messages" in {

      val testProbe = TestProbe()
      val thermometer = system.actorOf(ThermometerMockActor.props("Temp0"))

      def expectAnyValidThermometerMsg = {
        testProbe.expectMsgAnyClassOf(5.second,
          classOf[Failed], classOf[TemperatureValue])
      }

      def sendValidActionAndExpectValidThermometerMsg(msg: AnyRef) = {
        testProbe.send(thermometer, msg)
        expectAnyValidThermometerMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(thermometer, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidThermometerMsg(GetStatus)
      sendUnsupportedMsgAndExpectUnsupportedAction(ActivationValue(0.5))
    }
  }
}