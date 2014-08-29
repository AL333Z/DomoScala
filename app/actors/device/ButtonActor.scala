package actors.device

import actors.DeviceActor._
import actors.MeshnetBase.{FromDeviceMessage, SubscribeToMessagesFromDevice}
import akka.actor.{ActorRef, Props, actorRef2Scala}
import actors.DeviceActor
import akka.event.LoggingReceive
import play.libs.Akka

object ButtonActor {
  def props(name: String, meshnetActor: ActorRef, deviceId: Int): Props = Props(classOf[ButtonActor], name, meshnetActor, deviceId)
}

class ButtonActor(name: String, meshnetActor: ActorRef, deviceId: Int) extends DeviceActor(name, meshnetActor, deviceId) {

  override def preStart(){
    meshnetActor ! SubscribeToMessagesFromDevice(deviceId)
  }

  def receive = LoggingReceive {
    case msg: FromDeviceMessage => parseFromDeviceMessage(msg)
    case _ => sender ! UnsupportedAction
  }

  def parseFromDeviceMessage(msg: FromDeviceMessage){
    if(msg.command == 5){
      // TODO what Akka message should I send to the event stream when the button is pressed?
      //Akka.system.eventStream.publish()
      println("button pressed!")
    }
  }
}