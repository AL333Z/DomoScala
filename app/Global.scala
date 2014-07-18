import play.api._
import actors.{ MeshnetToDeviceMessage, MeshnetBase, DeviceActor }
import akka.actor.{ PoisonPill, Props }
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import play.api.Play.current
import akka.pattern.ask
import actors.DomoscalaActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("*************************************************************")
    Logger.info("******************Application has started.*******************")
    Logger.info("*************************************************************")

    Logger.info("Initializing Meshnet...")

    // check if there are some good port, and start the system
    MeshnetBase.getGoodPort.map { port =>
      // starting meshnet with given good port found
      val mesh = Akka.system.actorOf(Props[MeshnetBase], name = "meshnetbase")
      val domo = Akka.system.actorOf(Props[DomoscalaActor], name = "domoscala")
      domo ! Some(mesh)
      Logger.info("Initialized Meshnet.")
    }.getOrElse {
      // just playing
      val domo = Akka.system.actorOf(Props[DomoscalaActor], name = "domoscala")
      domo ! None
      Logger.info("Meshnet not detected, running in simulation mode.")
    }
  }

  override def onStop(app: Application) {
    Logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    Logger.info("+++++++++++++++++Application shutdown...+++++++++++++++++++++")
    Logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  }

}
