/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.river

// Understands access to an undifferentiated stream of messages
interface RapidsConnection {

    fun register(listener: River.PacketListener) // For services

    fun register(listener: River.SystemListener) // For system services (monitoring, system failures)

    fun publish(packet: RapidsPacket)

    interface MessageListener {
        fun message(sendPort: RapidsConnection, message: String): Any?
    }
}