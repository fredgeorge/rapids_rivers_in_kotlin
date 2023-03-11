package com.nrkei.training.microservices.rental

import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.Status

class KjetilSolution {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            require(args.size == 2) { "Missing IP and Port arguments! The IP address of the Rapids (as a string), and the Port number of the Rapids (also as a string)." }
            RabbitMqRapids(ipAddress = args[0], port = args[1]).also { rapids ->
                listen(rapids)
            }
        }

        private fun listen(rapidsConnection: RapidsConnection) {
            try {
                rapidsConnection.register(object : River.PacketListener {
                    override val rules = rules {
                        require key Messages.Key.COMMUNITY value Messages.Value.OFFER_ENGINE_FAMILY
                        require key Messages.Key.NEED value Messages.Value.CAR_RENTAL_OFFER
                    }

                    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
                        Packet(
                            Messages.Key.COMMUNITY to Messages.Value.OFFER_ENGINE_FAMILY,
                            Messages.Key.CAR_RENTAL_OFFER_MESSAGE to "Got a car for you " + packet["programmer"],
                            Messages.Key.CAR_RENTAL_OFFER to "SUX 5000",
                            Messages.Key.DAILY_RATE to "1000",
                            Messages.Key.LOCATION to "Arbins"
                        )
                        rapidsConnection.publish(
                            Packet(
                                Messages.Key.COMMUNITY to Messages.Value.OFFER_ENGINE_FAMILY,
                                Messages.Key.CAR_RENTAL_OFFER_MESSAGE to "Got a car for you " + packet["programmer"],
                                Messages.Key.CAR_RENTAL_OFFER to "SUX 5000",
                                Messages.Key.DAILY_RATE to "1000",
                                Messages.Key.LOCATION to "Arbins"
                            )
                        )
                        println("Offered a car to ${packet["programmer"]}: $packet")
                    }
                })
            } catch (e: Exception) {
                throw RuntimeException("Could not publish message:", e)
            }
        }
    }
}
