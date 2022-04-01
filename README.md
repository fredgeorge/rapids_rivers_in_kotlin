# kotlin_rapids_rivers
Kotlin implementation of Rapids/Rivers framework using RabbitMQ for the event bus.

There are a myriad of services:

- *Monitor*: A service that displays everything
- *Need*: A service that expresses a Need for a Rental Car Offer with a possible User identification
- *Brand Solution*: A service that makes an Offer for the overall rental car brand
- *Location Solution*: A service that makes an Offer specific for a rental location
- *Solution Selection*: A service that chooses the best Offer for a given Need
- *Membership Level*: A service that adds Membership status to a Need if User is a member
- *Membership Solution*: A service that suggests joining the Membership if User is not a member
- *Brand Membership Solution*: A service that makes an Offer for only members
- *Location Membership Solution*: A service that makes an Offer for only members

These services monitor the overall system:

- *Log All*: A service that collects all the Log messages
- *Log Error*: A service that collects only error Logs
- *Invalid Packet Detection*: A system service that detects invalid JSON and creates Log message
- *Loop Detection*: A system service that detects looping situations on the event bus, and creates Log message

These service deliberately generate error situations to ensure system detects and handles them

- *Invalid Packet Bug*: A test service that deliberately injects non-JSON on the event bus
- *Loop Bug*: A test service that creates an infinite message loop

