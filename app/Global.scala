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
    Logger.info("**********************************************************")
    Logger.info("******************Application has started.****************")
    Logger.info("**********************************************************")

    Logger.info("Initializing Meshnet...")

    // check if there are some good port, and start the system
    MeshnetBase.getGoodPort.map { port =>

      // starting meshnet actor with given good port found
      val mesh = Akka.system.actorOf(MeshnetBase.props(port, "meshnetbase"))

      // starting and initializing domoscala actor
      val domo = Akka.system.actorOf(DomoscalaActor.props("domoscala"))
      domo ! Some(mesh)

      Logger.info("Initialized Meshnet and DomoScala actors.")
    }.getOrElse {
      
      // just playing, starting and initializing domoscala actor with demo conf
      val domo = Akka.system.actorOf(Props[DomoscalaActor], name = "domoscala")
      domo ! None

      Logger.info("Meshnet not detected, running DomoScala in simulation mode.")
    }
  }

  override def onStop(app: Application) {
    Logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    Logger.info("+++++++++++++++++Application shutdown...++++++++++++++++++")
    Logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  }

}
