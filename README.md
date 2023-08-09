# rapids_rivers_in_kotlin.

Copyright (c) 2022 by Fred George  
@author Fred George  fredgeorge@acm.org  
Licensed under the MIT License; see LICENSE file in root.

A Kotlin implementation of Rapids/Rivers framework using RabbitMQ for the event bus

## Summary

This framework models the Rapids/Rivers/Pond metaphor first proposed by Fred George.
It strives to present a relatively simple choreography scheme to allow tiny MicroServices
to interact with minimal coupling.

Originally formulated for a workshop conference in Bergen, Norway, and it has been re-implemented
several times, including at NAV (Norwegian Welfare Association) in 2019 where it is now being used
extensively in the organization, and at Orn Software in 2022 (in C#).

## Dependencies

This project is built with:

- IntelliJ IDEA 2023.2 (Ultimate Edition)
- Java 17.0.7 Oracle
- Kotlin 1.9.0
- JUnit 5.10.0
- Gradle 8.2.1
- RabbitMQ amqp driver 5.18.0
- Jackson JSON 2.15.2

So open up the project, and let the dependencies resolve themselves. 
You can execute the tests to ensure everything is working correctly
if you are uncertain.

Now you are ready to run the sample MicroServices.

## Basic Concepts

An event bus acts as a __rapids__, an undifferentiated
stream of all messages. Access to the rapids is through an adapter;
for RabbitMQ, the adapter is the RabbitMqRapidsConnection.

Messages on the event bus are captured into __packets__. In Kotlin
and Java, you can inspect the received fields through the APIs
of Packet. You can also create new packets, and add properties to them
that will be rendered into JSON automatically on publishing to the rapids.

Using a set of __rules__, a subset of the rapids is created as a __river__.
Services are attached to a river, and will receive only packets that
conform to the rules.

## Sample Services

Two Kotlin sample services are provided:

- __Need__ _in Kotlin_: service generates a stream of messages on the event bus
    - Examine this code for how to create and send a Packet
- __Monitor__ _in Kotlin_: service logs to the console all messages on the bus
    - Examine this code for how to attach to the __rapids__ via a __river__
    - Also note the comments for how to set up __rules__ for the __packets__ you want to process

Two corresponding Java sample services (JMonitor and JNeed) are 
also provided with the same purposes as the Kotlin versions.

## Running Sample Services

Bring up an instance of RabbitMQ, preferably with the management addition to
allow browser inspection of what is happening.

Run the Monitor service. Be sure to add the startup parameters identifying 
the RabbitMQ IP address (localhost if running locally) and port number 
(default is 5672). You should see a console log announcing its start.

Next, run the Need service. Again, IP and port are required parameters. 
Need should generate a message very five seconds. Monitor should show 
this message.

If this is all working, you are ready to write your own, new services!
