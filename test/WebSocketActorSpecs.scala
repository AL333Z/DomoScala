import actors._
import actors.DomoscalaActor._
import actors.DomoscalaActor
import actors.DeviceActor._
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import play.api.Play
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.testkit.TestProbe
import akka.actor.ActorRef
import scala.util.Random
import play.api.libs.json.JsValue

class WebSocketActorSpecs extends PlaySpec with OneAppPerSuite {

  implicit val system = Akka.system(app)

  "The WebSocketActorSpecs" must {
    "start the FakeApplication" in {
      Play.maybeApplication mustBe Some(app)
    }
  }

  "The WebSocketActor" when {
    "the app is started and the actor system is running" must {
      "respond timely when new values are produced" in {
        val chosenBuilding = "Building0"
        val chosenRoom = "Room1"
        val chosenDev = "LightSensor0"
        val maxTimeout = 6 seconds
        val testProbe = TestProbe()

        val wsActor = system.actorOf(DeviceStatusWebSocketActor.props(testProbe.ref,
          chosenBuilding, chosenRoom, chosenDev))

        val domo = Await.result(system.actorSelection("user/domoscala").resolveOne, maxTimeout)
        testProbe.send(domo, GetDevice(chosenBuilding, chosenRoom, chosenDev))
        val deviceActor = testProbe.expectMsgClass(maxTimeout, classOf[ActorRef])

        // pubblish some values
        system.eventStream.publish(SoundValue(Random.nextFloat, Some(deviceActor.path.name)))
        system.eventStream.publish(TemperatureValue(Random.nextFloat, Some(deviceActor.path.name)))
        system.eventStream.publish(LightValue(Random.nextFloat, Some(deviceActor.path.name)))

        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])
        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])
        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])

      }
    }
  }

  "The DeviceStatusWebSocketActor" when {
    "the app is started and the actor system is running" must {
      "respond timely when new values are produced" in {
        val chosenBuilding = "Building0"
        val chosenRoom = "Room1"
        val chosenDev = "LightSensor0"
        val maxTimeout = 6 seconds
        val testProbe = TestProbe()

        val wsActor = system.actorOf(DeviceStatusWebSocketActor.props(testProbe.ref,
          chosenBuilding, chosenRoom, chosenDev))

        val domo = Await.result(system.actorSelection("user/domoscala").resolveOne, maxTimeout)
        testProbe.send(domo, GetDevice(chosenBuilding, chosenRoom, chosenDev))
        val deviceActor = testProbe.expectMsgClass(maxTimeout, classOf[ActorRef])

        // pubblish some values
        system.eventStream.publish(SoundValue(Random.nextFloat, Some(deviceActor.path.name)))
        system.eventStream.publish(TemperatureValue(Random.nextFloat, Some(deviceActor.path.name)))
        system.eventStream.publish(LightValue(Random.nextFloat, Some(deviceActor.path.name)))

        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])
        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])
        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])

      }
    }
  }

  "The RoomStatusWebSocketActor" when {
    "the app is started and the actor system is running" must {
      "respond timely when new values are produced from its devices" in {
        val chosenBuilding = "Building0"
        val chosenRoom = "Room1"

        val maxTimeout = 3 seconds
        val testProbe = TestProbe()

        val wsActor = system.actorOf(RoomStatusWebSocketActor.props(testProbe.ref,
          chosenBuilding, chosenRoom))

        val domo = Await.result(system.actorSelection("user/domoscala").resolveOne, maxTimeout)
        testProbe.send(domo, GetDevices(chosenBuilding, chosenRoom))
        val deviceActors = testProbe.expectMsgClass(maxTimeout, classOf[Map[String, ActorRef]])

        // pubblish some values
        system.eventStream.publish(SoundValue(Random.nextFloat, Some(deviceActors("LightSensor0").path.name)))
        system.eventStream.publish(SoundValue(Random.nextFloat, Some(deviceActors("LightSensor0").path.name)))

        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])
        testProbe.expectMsgClass(maxTimeout, classOf[JsValue])

      }
    }
  }

}