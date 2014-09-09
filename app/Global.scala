import actors.DomoscalaActor.AddBuilding
import actors.device.mock.{ ButtonMockActor, LightSensorMockActor, ThermometerMockActor, BulbMockActor }
import actors.device.{ ButtonActor, LightSensorActor, ThermometerActor, BulbActor }
import actors.{ Building, Room, DomoscalaActor, MeshnetBase }
import akka.actor.Props
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka

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

      val bulb0 = Akka.system.actorOf(BulbMockActor.props("Bulb0"))
      val button0 = Akka.system.actorOf(ButtonMockActor.props("Button0"))
      val room0 = Room("Room0", Map("Bulb0" -> bulb0, "Button0" -> button0))

      val bulb1 = Akka.system.actorOf(BulbMockActor.props("Bulb1"))
      val temp = Akka.system.actorOf(ThermometerMockActor.props("Thermometer0"))
      val light = Akka.system.actorOf(LightSensorMockActor.props("LightSensor0"))

      val room1 = Room("Room1", Map("Bulb1" -> bulb1, "Thermometer0" -> temp, "LightSensor0" -> light))

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
