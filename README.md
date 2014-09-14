DomoScala
=========

Home automation with Scala and Arduino.

**Currently, there's nothing exciting to try out.**

[![Build Status](https://travis-ci.org/AL333Z/DomoScala.svg?branch=master)](https://travis-ci.org/AL333Z/DomoScala)

Installation and first run
==========================

- install [RXTX native driver for your OS](http://jlog.org/rxtx-mac.html). This is only needed if you want to control real hardware. If you just want to try a sample with simulated sensors/actuators, skip this step.
- `cd <yourLocalPath>/DomoScala`
- launch Typesafe Activator script with `./activator`. This will download all that is need to launch the application.
- `run` or `test` or whatever you want.
- go to [http://localhost:9000/](http://localhost:9000/). This will compile and launch the application itself.

Motivations
===========

This project aims to implement a low-cost home automation system using modern technologies such as [Scala lang](http://www.scala-lang.org), [Play framework](http://www.playframework.com), [Akka](http://akka.io/), [Arduino](http://arduino.cc/), low power wireless mesh networks, and other exciting stuff. 

*At the time of writing, we only implemented the core of the system, as a proof of concept.*

System architecure
==================
The core of the system is a [Play framework](https://www.playframework.com) app. Since Play is built on top of [Akka](http://akka.io), most of the system components are modeled as Akka [actors](http://en.wikipedia.org/wiki/Actor_model).

The app [will] allow the user to configure the structure of its buildings. Each **building** is represented as a set of **rooms**, and each room is modeled as a set of **devices**. All of this components are abstracted by **actors**, interacting via **message-passing**.

**NB**: *At the time of writing, the system configuration is hard-coded inside the app.*

The system consists of:
* a **server** running the Play application (it should work in any device capable of running Java, even a [Raspberry Pi](http://www.raspberrypi.org/))
* one or more **Arduino** or other custom boards with sensors (temperature, buttons, ...) and actuators (lamps, locks, ...)
* one or more **clients** that let the users interact with the system (Web application, Android app, ...)

Hardware devices
----------
The home automation system is able to control real electrical sensors and actuators through the use of microcontrollers, small computers that are able to directly control many electrical components. The microcontroller platform that we choose is the very popular [Arduino](http://arduino.cc). It's possible to use the official Arduino Uno board, or also make a completely custom board based on the AVR Atmega328, the same chip of the official one.

A good home automation system is composed by a lot of small devices, that are spread in the various parts of the house. So it's very important for the devices to have a low cost and to have a wireless connection with an high range (greater than Wifi) and low power consumption, in order to be potentially battery powered.
For achieve this goal we developed **[Meshnet](https://github.com/mattibal/meshnet)**, a Java and Arduino library that build a wireless-wired (mixed) [mesh network](http://en.wikipedia.org/wiki/Mesh_networking) of many low cost (5€) devices. In this kind of network, every device is also a router that can relay packets for other devices, extending the wireless range of the whole network. At the physical level, Meshnet uses [nRF24l1](http://arduino-info.wikispaces.com/Nrf24L01-2.4GHz-HowTo) proprietary low-power 2,4 GHz wireless modules, that can be bought at 1€ each, and various kind of serial communications where you need wired connections.

With these technologies we can achieve a better wireless coverage than Wifi, a greater flexibility since the devices could be battery operated, and a much lower cost than using many Raspberry Pi with Wifi or Ethernet connections in many places of the house.

API
-----
The app provide some **REST APIs** to interact with the system. For example, a client can retrieve the list of buildings in a system with a simple API call.

```
GET   /buildings    
```

The app also provide APIs to get notified when something happens inside the system (aka **publish/subscribe**). This feature allows other developers to develop client app that receive updates in *real-time*.
To achieve this goal, we use **[Web Sockets](http://en.wikipedia.org/wiki/WebSocket)** combined with **actors**.
For example, a client can be notified of all system event (coming from all buildings in the system).

```
GET   /push    
```

The APIs support both **fine grained and coarse grained granularity** when querying system status. Thus, a client can request/subscribe to information for all buildings and/or for a single device.
For more details on APIs, check `conf/routes` file.

A sample response of a `GET   /buildings` request may have the following format:

```json
{
   "status":"OK",
   "buildings":[
      {
         "id":"Building0",
         "rooms":[
            {
               "id":"Room0",
               "devices":[
                  {
                     "id":"Bulb0",
                     "devType":"bulb"
                  },
                  {
                     "id":"Button0",
                     "devType":"button"
                  }
               ]
            },
            {
               "id":"Room1",
               "devices":[
                  {
                     "id":"Bulb1",
                     "devType":"bulb"
                  },
                  {
                     "id":"Thermometer0",
                     "devType":"temp"
                  },
                  {
                     "id":"LightSensor0",
                     "devType":"light"
                  }
               ]
            }
         ]
      }
   ]
}
```

A new value published by a device, for example by a sound sensor with id `SoundSensor0` installed in a room with id `Room0` in a building with id `Building0`, may have the following format:

```json
{  
   "buildingId":"Building0",
   "roomId":"Room0",
   "deviceId":"SoundSensor0",
   "um":"decibels",
   "status":{  
      "value":0.6545045971870422
   }
}
```

Frontend
--------
The frontend use **HTML5**, **[Bootstrap](getbootstrap.com)** and **[jQuery](jquery.com)** to present a minimal web interface to the user. The interface is updated in real-time thanks to the usage of the Web Socket APIs depicted above.
This approach avoids the need to continuously reload the page to get the latest informations about the system.

Android
----------
The android app [will] be nothing special. Just an app that use REST APIs and Web Sockets to update some list views.
It's pretty the same as the html+js fronted, but developed with **Android SDK**.

Implementation
==============

Arduino and Meshnet
-------------------
//TODO 

Actors
------

The main actor of the system is *DomoscalaActor* (`actors.DomoscalaActor.scala`). It wraps all the system configurations and structure and all other actor (and APIs) has to interact to that.

*MeshnetBase* (`actors.Meshnet.scala`) wraps the interaction with meshnet library. It's a facade that allows the interaction with meshnet (and so, with physical devices) via message-passing.

*DeviceActor* (`actors.DeviceActor.scala`) represents an abstract device actor. In its companion object are defined all the messages that all concrete device actor instances can accept/exchange. All concrete implementation of this actor are in `actors.device` package.

The interaction between all system components is defined in `test` folder, containing all tests specs.

API
---

Every APIs is **asynchronous from the bottom up**. The system handles every request in an [asynchronous, non-blocking way](https://www.playframework.com/documentation/2.3.x/ScalaAsync). Thus, if the client request a heavy operation to the server, the client will be blocked while waiting for the response, but nothing will be blocked on the server, and server resources can be used to serve other clients.

[Web Sockets](https://www.playframework.com/documentation/2.3.x/ScalaWebSockets) allows the implementation of Publish/Subscribe feature. Thus, there's a **two way full duplex communication channel** between clients and the server that enable the flow of new contents from the server to the client once the contents are produced, timely.
