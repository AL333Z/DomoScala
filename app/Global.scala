import play.api._
import actors.{ MeshnetToDeviceMessage, MeshnetBase, DeviceActor }
import akka.actor.{ PoisonPill, Props }
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.Play.current
import akka.pattern.ask

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("*************************************************************")
    Logger.info("******************Application has started.*******************")
    Logger.info("*************************************************************")

    Logger.info("Initializing Meshnet...")
    try {
      val act = Akka.system.actorOf(Props[MeshnetBase], name = "meshnetbase")
      Logger.info("Initialized Meshnet.")
    } catch {
      case e: Exception => {
        Logger.info("Meshnet not detected, running in simulation mode.")
      }
    }
  }

  override def onStop(app: Application) {
    Logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    Logger.info("+++++++++++++++++Application shutdown...+++++++++++++++++++++")
    Logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  }

}
