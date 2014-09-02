import actors.DeviceActor._
import actors.device.mock.{BulbMockActor, LightSensorMockActor, ThermometerMockActor}
import akka.actor.Props
import akka.testkit.TestProbe
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Play
import play.api.libs.concurrent.Akka

import scala.concurrent.duration._

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
    "respond properly only to On/Off/SetActivationValue" in {

      val testProbe = TestProbe()
      val buld = system.actorOf(Props(classOf[BulbMockActor]))

      def expectAnyValidBulbMsg = {
        testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], Ok.getClass)
      }

      def sendValidActionAndExpectValidBulbMsg(msg: AnyRef) = {
        testProbe.send(buld, msg)
        expectAnyValidBulbMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(buld, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidBulbMsg(On)
      sendValidActionAndExpectValidBulbMsg(Off)
      sendValidActionAndExpectValidBulbMsg(SetActivationValue(0.5))
      sendValidActionAndExpectValidBulbMsg(SetActivationValue(10.5))

      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
    }

  }

  // TODO the button is only a sensor! when the user physically click it, it sends a message that we should handle
  /*"A ButtonActor" must {
    "respond properly only to Click" in {

      val testProbe = TestProbe()
      val button = system.actorOf(ButtonActor.props("button1"))

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

      sendValidActionAndExpectValidButtonMsg(Click)

      sendUnsupportedMsgAndExpectUnsupportedAction(On)
      sendUnsupportedMsgAndExpectUnsupportedAction(Off)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
    }
  }*/

  "A LightSensorActor" must {
    "respond properly only to GetLightValue" in {

      val testProbe = TestProbe()
      val lightSensor = system.actorOf(Props(classOf[LightSensorMockActor]))

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

      sendValidActionAndExpectValidLightSensorMsg(GetLightValue)

      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
      sendUnsupportedMsgAndExpectUnsupportedAction(On)
      sendUnsupportedMsgAndExpectUnsupportedAction(Off)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
    }
  }

  // TODO remove, we don't use servos
  /*"A ServoActor" must {
    "respond properly only to MoveServo" in {

      val testProbe = TestProbe()
      val servo = system.actorOf(ServoActor.props("servo1"))

      def expectAnyValidServoMsg = {
        testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], Ok.getClass)
      }

      def sendValidActionAndExpectValidServoMsg(msg: AnyRef) = {
        testProbe.send(servo, msg)
        expectAnyValidServoMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(servo, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidServoMsg(MoveServo)

      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
      sendUnsupportedMsgAndExpectUnsupportedAction(On)
      sendUnsupportedMsgAndExpectUnsupportedAction(Off)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
    }
  }*/

  // TODO remove, we don't use sound sensors
  /*"A SoundSensorActor" must {
    "respond properly only to GetSoundValue" in {

      val testProbe = TestProbe()
      val soundSensor = system.actorOf(SoundSensorActor.props("soundSensor1"))

      def expectAnyValidSoundSensorMsg = {
        testProbe.expectMsgAnyClassOf(5.second,
          classOf[Failed], classOf[SoundValue])
      }

      def sendValidActionAndExpectValidSoundSensorMsg(msg: AnyRef) = {
        testProbe.send(soundSensor, msg)
        expectAnyValidSoundSensorMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(soundSensor, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidSoundSensorMsg(GetSoundValue)

      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
      sendUnsupportedMsgAndExpectUnsupportedAction(On)
      sendUnsupportedMsgAndExpectUnsupportedAction(Off)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
    }
  }*/

  // TODO remove, we don't use speakers
  /*"A SpeakerActor" must {
    "respond properly only to PlayBeep" in {

      val testProbe = TestProbe()
      val speaker = system.actorOf(SpeakerActor.props("speaker1"))

      def expectAnyValidSpeakerMsg = {
        testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], Ok.getClass)
      }

      def sendValidActionAndExpectValidSpeakerMsg(msg: AnyRef) = {
        testProbe.send(speaker, msg)
        expectAnyValidSpeakerMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(speaker, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidSpeakerMsg(PlayBeep)

      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
      sendUnsupportedMsgAndExpectUnsupportedAction(On)
      sendUnsupportedMsgAndExpectUnsupportedAction(Off)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
    }
  }*/

  // TODO remove, we don't use switches
  /*"A SwitchActor" must {
    "respond properly only to On/Off" in {

      val testProbe = TestProbe()
      val switch = system.actorOf(SwitchActor.props("switch1"))

      def expectAnyValidSwitchMsg = {
        testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], Ok.getClass)
      }

      def sendValidActionAndExpectValidSwitchMsg(msg: AnyRef) = {
        testProbe.send(switch, msg)
        expectAnyValidSwitchMsg
      }

      def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
        testProbe.send(switch, msg)
        expectUnsupportedAction(testProbe)
      }

      sendValidActionAndExpectValidSwitchMsg(On)
      sendValidActionAndExpectValidSwitchMsg(Off)

      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
    }
  }*/

  "A ThermometerActor" must {
    "respond properly only to GetTemperature" in {

      val testProbe = TestProbe()
      val thermometer = system.actorOf(Props(classOf[ThermometerMockActor]))

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

      sendValidActionAndExpectValidThermometerMsg(GetTemperature)

      sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
      sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(Click)
      sendUnsupportedMsgAndExpectUnsupportedAction(On)
      sendUnsupportedMsgAndExpectUnsupportedAction(Off)
      sendUnsupportedMsgAndExpectUnsupportedAction(SetActivationValue(0.5))
      sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
      sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
    }
  }
}