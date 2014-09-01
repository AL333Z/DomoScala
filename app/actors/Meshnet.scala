package actors

import java.nio.ByteBuffer

import actors.DomoscalaActor.AddBuilding
import actors.MeshnetBase.{DeviceNotConnected, FromDeviceMessage, SubscribeToMessagesFromDevice, ToDeviceMessage}
import actors.device.{ButtonActor, LightSensorActor, ThermometerActor, BulbActor}
import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import com.mattibal.meshnet.devices.{LedTestDevice, Led1Analog2Device}
import com.mattibal.meshnet.{ Device, SerialRXTXComm, Layer3Base }
import gnu.io._
import scala.collection.JavaConversions._
import play.Logger
import scala.util.Try
import scala.util.Success
import scala.util.Failure


object MeshnetBase {

  def props(port: CommPortIdentifier, domoscalaActor: ActorRef): Props =
    Props(classOf[MeshnetBase], port, domoscalaActor)

  def getGoodPort: Option[CommPortIdentifier] = {

    val tryPorts = try {
      Try(CommPortIdentifier.getPortIdentifiers
        .asInstanceOf[java.util.Enumeration[CommPortIdentifier]].toVector)
    } catch {
      case e: Throwable => Failure(e)
    }

    tryPorts match {
      case Success(ports) => {
        Logger.debug("Available serial ports: " + ports.map(_.getName))
        val goodPorts = ports.filter(x => x.getName.contains("tty.usbmodem")
          || x.getName.contains("ttyACM"))

        goodPorts.toList match {
          case (x :: _) => Some(x)
          case Nil => None
        }
      }
      case Failure(ex) => {
        Logger.error("Error getting availlable ports. Missing RXTX? Message: " +
          ex.toString())
        None
      }
    }
  }


  // A message that can be sent to a MeshNet device. All real messages should extend from this.
  case class ToDeviceMessage(destinationId: Int, command: Int, data: Array[Byte])

  case class FromDeviceMessage(sourceDevId: Int, command: Int, data: Array[Byte])

  /**
   * The sender actor of this message will be subscribed to receive Meshnet messages coming from
   * the device specified as argument
   */
  case class SubscribeToMessagesFromDevice(deviceId: Int)

  /**
   * This is a notification that the requested operation (message send or receive subscription) has not been executed
   * because the Meshnet device identified by this deviceId is not currently connected to that MeshnetBase
   */
  case class DeviceNotConnected(deviceId: Int)

}




/**
 * This actor represent a MeshNet base, something capable of running a JVM (for
 * example a Raspberry Pi) that act as a coordinator of (a part of) a MeshNet
 * network.
 *
 * If you want to send or receive messages with devices that are currently
 * connected to a certain MeshNet base, you have to talk with this actor.
 */
class MeshnetBase(port: CommPortIdentifier, domoscalaActor: ActorRef) extends Actor with ActorLogging with Device.CommandReceivedListener {

  val layer3Base = new Layer3Base

  var subscribedActors: Map[Int, Set[ActorRef]] = Map()

  override def preStart() = {
    val serialComm = new SerialRXTXComm(port, layer3Base)
    Thread.sleep(3000)
    val networkSetupThread = new layer3Base.NetworkSetupThread
    networkSetupThread.run() // dirty hack to launch the legacy java code in the actor thread

    // this is blocking, but everything on this actor will be blocking... it just wastes 1 thread for each MeshNet base
    Thread.sleep(4000)

    createActorsFromDiscoveredDevices()
  }


  def receive = {

    case ToDeviceMessage(destinationId, command, data) => {
      getDeviceFromUniqueId(destinationId) match {
        case Some(device) => device.sendCommand(command, data)
        case None => sender() ! DeviceNotConnected(destinationId)
      }
    }

    case SubscribeToMessagesFromDevice(deviceId) => {
      getDeviceFromUniqueId(deviceId) match {
        case Some(device) => {
          device.addCommandReceivedListener(this)
          subscribedActors += ((deviceId, subscribedActors.getOrElse(deviceId, Set()) + context.sender()))
        }
        case None => sender() ! DeviceNotConnected(deviceId)
      }
    }

    case msg : FromDeviceMessage => {
      val subscribers = subscribedActors.get(msg.sourceDevId)
      subscribers.foreach(_.foreach(_ ! msg))
    }
  }


  /**
   * Callback from the Meshnet Java library thread, so I just send myself a message to avoid threading issues
   */
  override def onCommandReceived(command: Int, deviceId: Int, data: ByteBuffer): Unit = {
    context.self ! FromDeviceMessage(deviceId, command, data.array())
  }



  /**
   * A "scalification" of the legacy Java method...
   *
   * Use this instead of Device.getDeviceFromUniqueId()
   */
  def getDeviceFromUniqueId(deviceId: Int) : Option[Device] = {
    Device.getDeviceFromUniqueId(deviceId) match {
      case null => None
      case device: Device => Some(device)
    }
  }


  def createActorsFromDiscoveredDevices() {
    val devices = Device.getKnownDevices
    val rooms = devices.map(
      _ match {
        case device: Led1Analog2Device => {   // this is our battery-powered wireless test circuit
          val devId = device.getUniqueId
          val bulb = context.actorOf(BulbActor.props("Bulb1", self, devId), "Bulb1")
          val temp = context.actorOf(ThermometerActor.props("Thermometer0", self, devId), "Thermometer0")
          val light = context.actorOf(LightSensorActor.props("LightSensor0", self, devId), "LightSensor0")
          Room("Room1", Map("Bulb1" -> bulb, "Thermometer0" -> temp, "LightSensor0" -> light))
        }

        case device: Device => {       // this is our Arduino shield connected with USB to the computer (Meshnet base)
          val devId = device.getUniqueId
          val bulb = context.actorOf(BulbActor.props("Bulb0", self, devId), "Bulb")
          val button = context.actorOf(ButtonActor.props("Button0", self, devId), "Button0")
          Room("Room0", Map("Bulb0" -> bulb, "Button0" -> button))
        }
      }
    )
    val building = Building("Building0", Set()++rooms) // ++ needed to convert from mutable to immutable Set
    domoscalaActor ! AddBuilding(building)
  }
}


