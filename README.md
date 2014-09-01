DomoScala
=========

Home automation with Scala and Arduino.

**Currently, there's nothing exciting to try out.**

[![Build Status](https://travis-ci.org/AL333Z/DomoScala.svg?branch=master)](https://travis-ci.org/AL333Z/DomoScala)

Installation and first run
==========================

- install [RXTX native driver for your OS](http://jlog.org/rxtx-mac.html)
- download the [Typesafe Activator](https://typesafe.com/activator)
- `cd <yourLocalPath>/DomoScala`
- `play run` or `play test` or whatever you want.
- go to [http://localhost:9000/](http://localhost:9000/)

Motivations
===========

This project aims to implement a low-cost home automation system using modern technologies such as [Scala lang](http://www.scala-lang.org), [Play framework](http://www.playframework.com), [Akka](http://akka.io/), [Arduino](http://arduino.cc/), and other exciting stuff. 

*At the time of writing, we only implemented the core of the system, as a proof of concept.*

System structure
================
The system itself is a Play framework app. Since Play is built on top of Akka, most of the system components are modeled as akka [actors](http://en.wikipedia.org/wiki/Actor_model).

The app [will] allow the user to configure the structure of its buildings. Each **building** is represented as a set of **rooms**, and each room is modeled as a set of **devices**. All of this components are abstracted by **actors**, interacting via **message-passing**.

**NB**: *At the time of writing, the system configuration is hard-coded inside the app.*

The system consists of:
* a **server** (even a [Raspberry Pi](http://www.raspberrypi.org/) should work fine)
* one or more **Arduino** (or other kind of circuits), that interact with the server
* one or more **clients** app (frontend app, android app, ...)

Arduino
----------
Arduino is used to simulate an home automation system, with its **sensor** and **actuator** devices.
**[Meshnet lib](https://github.com/mattibal/meshnet)** is used to build a [mesh net](http://en.wikipedia.org/wiki/Mesh_networking) of devices. This approach extends the total range of the network and allows devices to comunicate with each other without the need of a Wifi access point near each device.

API
-----
The app provide some **REST APIs** to interact with the system. For example, a client can retrieve the list of buildings in a system with a simple API call.

```
GET   /buildings    
```

The app also provide APIs to get notified when something happens inside the system (aka **pubblish/subscribe**). This feature allows other developers to develop client app that receive updates in *real-time*.
To achieve this goal, we use **[Web Sockets](http://en.wikipedia.org/wiki/WebSocket)** combined with **actors**.
For example, a client can be notified of all system event (coming from all buildings in the system).

```
GET   /push    
```

The APIs support both **fine grained and coarse grained granularity** when querying system status (from all buildings to single sensor/actuator).
For more details on APIs, check `conf/routes` file.

Frontend
--------
The frontend use **HTML5**, **[Bootstrap](getbootstrap.com)** and **[jQuery](jquery.com)** to present a minimal web interface to the user. The interface is updated in real-time thanks to the usage of the Web Socket APIs depicted above.
This approach avoids the need to continuously reload the page to get the latest informations about the system.

Android
----------
The android app [will] be nothing special. Just an app that use REST APIs and Web Sockets to update some list views.
It's pretty the same as the html+js fronted, but developed with **Android SDK**.
