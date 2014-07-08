import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.TestKit
import scala.concurrent.duration._
import actors._
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

  "A BulbActor" should "respond properly only to On/Off/SetActivationValue" in {

    val testProbe = TestProbe()
    val buld = system.actorOf(BulbActor.props("bulb1"))

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

  "A ButtonActor" should "respond properly only to Click" in {

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

  "A LedActor" should "respond properly only to On/Off/SetActivationValue" in {

    val testProbe = TestProbe()
    val led = system.actorOf(LedActor.props("led1"))

    def expectAnyValidLedMsg = {
      testProbe.expectMsgAnyClassOf(5.second, classOf[Failed], Ok.getClass)
    }

    def sendValidActionAndExpectValidLedMsg(msg: AnyRef) = {
      testProbe.send(led, msg)
      expectAnyValidLedMsg
    }

    def sendUnsupportedMsgAndExpectUnsupportedAction(msg: AnyRef) = {
      testProbe.send(led, msg)
      expectUnsupportedAction(testProbe)
    }

    sendValidActionAndExpectValidLedMsg(On)
    sendValidActionAndExpectValidLedMsg(Off)
    sendValidActionAndExpectValidLedMsg(SetActivationValue(0.5))

    testProbe.send(led, SetActivationValue(1.5))
    testProbe.expectMsgAnyClassOf(5.second, classOf[Failed])

    sendUnsupportedMsgAndExpectUnsupportedAction(Click)
    sendUnsupportedMsgAndExpectUnsupportedAction(GetLightValue)
    sendUnsupportedMsgAndExpectUnsupportedAction(GetTemperature)
    sendUnsupportedMsgAndExpectUnsupportedAction(MoveServo)
    sendUnsupportedMsgAndExpectUnsupportedAction(PlayBeep)
    sendUnsupportedMsgAndExpectUnsupportedAction(GetSoundValue)
  }

  "A LightSensorActor" should "respond properly only to GetLightValue" in {

    val testProbe = TestProbe()
    val lightSensor = system.actorOf(LightSensorActor.props("lightSensor1"))

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

  "A ServoActor" should "respond properly only to MoveServo" in {

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

  "A SoundSensorActor" should "respond properly only to GetSoundValue" in {

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

  "A SpeakerActor" should "respond properly only to PlayBeep" in {

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

  "A SwitchActor" should "respond properly only to On/Off" in {

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

  "A ThermometerActor" should "respond properly only to GetTemperature" in {

    val testProbe = TestProbe()
    val thermometer = system.actorOf(ThermometerActor.props("thermometer1"))

    def expectAnyValidThermometerMsg = {
      testProbe.expectMsgAnyClassOf(5.second,
        classOf[Failed], classOf[Temperature])
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