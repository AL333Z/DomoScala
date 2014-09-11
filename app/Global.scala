import actors.DomoscalaActor.AddBuilding
import actors.device.mock.{ ButtonMockActor, LightSensorMockActor, ThermometerMockActor, BulbMockActor }
import actors.device.{ ButtonActor, LightSensorActor, ThermometerActor, BulbActor }
import actors.{ Building, Room, DomoscalaActor, MeshnetBase }
import akka.actor.Props
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka
import actors.Dev
import actors.DeviceActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("**********************************************************")
    Logger.info("******************Application has started.****************")
    Logger.info("**********************************************************")

    val domoscalaActor = Akka.system.actorOf(DomoscalaActor.props("domoscala"), "domoscala")

    Logger.info("Looking for Meshnet base (Arduino connected with USB)...")

    // check if there are some good port, and start the system
    MeshnetBase.getGoodPort.map { port =>

      Logger.info("Using port: " + port.getName)
      val mesh = Akka.system.actorOf(MeshnetBase.props(port, domoscalaActor))

    }.getOrElse {

      Logger.info("Meshnet base not detected, running DomoScala in simulation mode.")

      // Create mock device actors

      val bulb0 = Dev("Bulb0", DeviceActor.bulbType, Akka.system.actorOf(BulbMockActor.props("Bulb0")))
      val button0 = Dev("Button0", DeviceActor.buttonType, Akka.system.actorOf(ButtonMockActor.props("Button0")))

      val room0 = Room("Room0", Set(bulb0, button0))

      val bulb1 = Dev("Bulb1", DeviceActor.bulbType, Akka.system.actorOf(BulbMockActor.props("Bulb1")))
      val temp = Dev("Thermometer0", DeviceActor.tempType, Akka.system.actorOf(ThermometerMockActor.props("Thermometer0")))
      val light = Dev("LightSensor0", DeviceActor.lightType, Akka.system.actorOf(LightSensorMockActor.props("LightSensor0")))

      val room1 = Room("Room1", Set(bulb1, temp, light))

      val building = Building("Building0", Set(room0, room1))
      domoscalaActor ! AddBuilding(building)
    }
  }

  override def onStop(app: Application) {
    Logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    Logger.info("+++++++++++++++++Application shutdown...++++++++++++++++++")
    Logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  }

}
