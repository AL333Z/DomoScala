package actors

import akka.actor.{Stash, FSM, Actor}
import com.mattibal.meshnet.{Device, SerialRXTXComm, Layer3Base}
import gnu.io._
import play.libs.Akka
import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
 * This actor represent a MeshNet base, something capable of running a JVM (for example a Raspberry Pi)
 * that act as a coordinator of (a part of) a MeshNet network.
 *
 * If you want to send of receive messages with devices that are currently connected to a certain MeshNet base, you have to
 * talk with that MeshnetBase actor.
 */
class MeshnetBase extends Actor {

  val layer3Base = new Layer3Base

  override def preStart() = {

    val ports = CommPortIdentifier.getPortIdentifiers.asInstanceOf[java.util.Enumeration[CommPortIdentifier]].toVector
    println("Available serial ports: " + ports.map(_.getName))
    val goodPorts = ports.filter(x => x.getName.contains("tty.usbmodem") || x.getName.contains("ttyACM"))

    val serialComm = new SerialRXTXComm(goodPorts(0), layer3Base)
    val networkSetupThread = new layer3Base.NetworkSetupThread
    networkSetupThread.run() // dirty hack to launch the legacy java code in the actor thread

    //Akka.system.scheduler.scheduleOnce(4000 milliseconds, self, NetworkSetupCompleted)
    Thread.sleep(4000)
  }


  def receive = {

    case MeshnetToDeviceMessage(destinationId, command, data) => {
      val device = Device.getDeviceFromUniqueId(destinationId)
      device.sendCommand(command, data)
    }
  }
}


// A message that can be sent to a MeshNet device. All real messages should extend from this.
case class MeshnetToDeviceMessage(destinationId: Int, command: Int, data: Array[Byte])


