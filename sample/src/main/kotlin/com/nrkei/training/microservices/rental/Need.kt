/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rental

import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids

// Understands the requirement for advertising on a site
class Need {

    companion object {
        private const val COMMUNITY = "community"
        private const val OFFER_ENGINE_FAMILY = "offer_engine_family"
        private const val NEED = "need"
        private const val CAR_RENTAL_OFFER = "car_rental_offer"

        @JvmStatic
        fun main(args: Array<String>) {
            RabbitMqRapids(ipAddress = args[0], port = args[1]).also { rapids ->
                publish(rapids)
            }
        }

        private fun publish(rapidsConnection: RapidsConnection) {
            try {
                Packet(
                    COMMUNITY to OFFER_ENGINE_FAMILY,
                    NEED to CAR_RENTAL_OFFER
                ).also { needPacket ->
                    while (true) {
                        println(String.format(" [<] %s", needPacket))
                        rapidsConnection.publish(needPacket)
                        Thread.sleep(5000)
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Could not publish message:", e)
            }
        }
    }
}
