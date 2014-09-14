import actors.DeviceActor._
import actors.device.mock.{ BulbMockActor, LightSensorMockActor, ThermometerMockActor }
import akka.actor.Props
import akka.testkit.TestProbe
import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }
import play.api.Play
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import actors.device.mock._
import akka.util.Timeout
import org.openqa.selenium.internal.seleniumemulation.GetValue
import scala.concurrent.Await
import actors.DomoscalaActor._
import actors._

class DomoScalaSpecs extends PlaySpec with OneAppPerSuite {

  implicit val system = Akka.system(app)
  implicit val timeout = Timeout(5 seconds)
  val duration = timeout.duration
  lazy val domo = Await.result(Akka.system.actorSelection("user/domoscala").resolveOne, duration)

  def expectUnsupportedAction(actor: TestProbe) = {
    actor.expectMsg(5 seconds, UnsupportedAction)
  }

  "A GetBuildings request" must {
    "respond with a set of building" in {
      val testProbe = TestProbe()
      testProbe.send(domo, GetBuildings)
      testProbe.expectMsgAnyClassOf(duration, classOf[Set[Building]])
    }
  }

  "A GetRooms request" must {
    "respond with a set of rooms" in {
      val testProbe = TestProbe()
      testProbe.send(domo, GetRooms("Building0"))
      testProbe.expectMsgAnyClassOf(duration, classOf[Set[Room]])
    }
  }

  "A GetDevices request" must {
    "respond with a set of devices" in {
      val testProbe = TestProbe()
      testProbe.send(domo, GetDevices("Building0", "Room0"))
      testProbe.expectMsgAnyClassOf(duration, classOf[Set[Dev]])
    }
  }

  "A GetDevice request" must {
    "respond with some device, if it exists in the system, or none" in {
      val testProbe = TestProbe()
      testProbe.send(domo, GetDevice("Building0", "Room0", "Bulb0"))
      testProbe.expectMsgAnyClassOf(duration, classOf[Dev])
    }
  }

  "A GetDeviceStatus request" must {
    "respond with a device status, if the device exists" in {
      val testProbe = TestProbe()
      testProbe.send(domo, GetDeviceStatus("Building0", "Room0", "Bulb0"))
      testProbe.expectMsgAnyClassOf(duration, classOf[DeviceStatus])
    }
  }

}