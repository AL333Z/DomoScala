import actors.{DomoscalaActor, MeshnetBase}
import akka.actor.Props
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("**********************************************************")
    Logger.info("******************Application has started.****************")
    Logger.info("**********************************************************")

    val domoscalaActor = Akka.system.actorOf(DomoscalaActor.props("domoscala"))

    Logger.info("Initializing Meshnet...")

    // check if there are some good port, and start the system
    MeshnetBase.getGoodPort.map { port =>

      val mesh = Akka.system.actorOf(MeshnetBase.props(port, "meshnetbase", domoscalaActor))

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
