/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
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
            require(args.size == 2) { "Missing IP and Port arguments! The IP address of the Rapids (as a string), and the Port number of the Rapids (also as a string)." }
            RabbitMqRapids(ipAddress = args[0], port = args[1]).also { rapids ->
                publish(rapids)
            }
        }

        private fun publish(rapidsConnection: RapidsConnection) {
            try {
                Packet(
                    Messages.Key.COMMUNITY to Messages.Value.OFFER_ENGINE_FAMILY,
                    Messages.Key.NEED to Messages.Value.CAR_RENTAL_OFFER,
                    Messages.Key.USER to "kjetil"
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
