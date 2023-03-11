package com.nrkei.training.microservices.rental

import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.Status

class GodSolutionPicker : River.PacketListener {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            require(args.size == 2) { "Missing IP and Port arguments! The IP address of the Rapids (as a string), and the Port number of the Rapids (also as a string)." }
            RabbitMqRapids(ipAddress = args[0], port = args[1]).register(GodSolutionPicker())
        }
    }

    override val rules: List<Validation>
        get() = rules {
            require key Messages.Key.PROBABILITY
            require key Messages.Key.DAILY_RATE
            require key Messages.Key.USER
            require key Messages.Key.CAR
            forbid key Messages.Key.BEST_OFFER
        }

    val bestOffersPerUser = mutableMapOf<String, Packet>()

    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
        val user = "${packet[Messages.Key.USER]}"
        val car  = packet[Messages.Key.CAR]
        val probability  = packet[Messages.Key.PROBABILITY].toString().toInt()
        val rate  = packet[Messages.Key.DAILY_RATE].toString().toInt()

        val previous = bestOffersPerUser.get(user)
        var best = previous ?: packet
        if ( previous != null ) {
            if ( calc(packet[Messages.Key.PROBABILITY].toString().toInt(), packet[Messages.Key.DAILY_RATE].toString().toInt()) < calc( probability, rate)) {
                best = packet
            }
        }
        bestOffersPerUser.put(user, best)

        println("=== Best solution => "+best)
        best[Messages.Key.BEST_OFFER] = "true"
        connection.publish( best )
    }

    private fun calc(probability : Int, rate : Int ): Int {
        return probability * rate
    }
}