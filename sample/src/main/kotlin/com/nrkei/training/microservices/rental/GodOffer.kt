package com.nrkei.training.microservices.rental

import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.Status
import kotlin.random.Random

class GodOffer  : River.PacketListener {
    companion object {
            @JvmStatic
        fun main(args: Array<String>) {
            require(args.size == 2) { "Missing IP and Port arguments! The IP address of the Rapids (as a string), and the Port number of the Rapids (also as a string)." }
            RabbitMqRapids(ipAddress = args[0], port = args[1]).register(GodOffer())
        }
    }

    override val rules: List<Validation>
        get() = rules {
            require key Messages.Key.NEED value Messages.Value.CAR_RENTAL_OFFER
            forbid key Messages.Key.PROBABILITY
            forbid key Messages.Key.BEST_OFFER
        }

    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
        val answer = packet.clone()
        answer[Messages.Key.MESSAGE ] = "Got a car for you, " + packet["user"]
        answer[Messages.Key.CAR] = "Delorean"
        answer[Messages.Key.DAILY_RATE] = Random.nextInt(100,2000)
        answer[Messages.Key.PROBABILITY]= Random.nextInt(0,100)

        connection.publish(answer)
        println("Offered car to " + packet["user"])

    }

}