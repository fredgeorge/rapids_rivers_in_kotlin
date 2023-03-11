package com.nrkei.training.microservices.offer

import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.Status

class RuneOffer : River.PacketListener {
    companion object {
        private const val COMMUNITY = "community"
        private const val OFFER_ENGINE_FAMILY = "offer_engine_family"
        private const val NEED = "need"
        private const val CAR_RENTAL_OFFER = "car_rental_offer"

        @JvmStatic
        fun main(args: Array<String>) {
            require(args.size == 2) { "Missing IP and Port arguments! The IP address of the Rapids (as a string), and the Port number of the Rapids (also as a string)." }
            RabbitMqRapids(ipAddress = args[0], port = args[1]).register(RuneOffer())
        }
    }
    override val rules: List<Validation>
        get() = rules {
            require key NEED value CAR_RENTAL_OFFER     // Reject packet unless it has key:value pair a_key:a_value
        }

    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
        connection.publish(
                Packet(COMMUNITY to OFFER_ENGINE_FAMILY,
                        "solution" to "Got a better car offer for you, " + packet["programmer"],
                        "car" to "Tesla Model Y")
        )
        println("Offered car to " + packet["programmer"])
    }
}