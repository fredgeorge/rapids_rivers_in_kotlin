/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.util

import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.packet.RapidsPacket
import com.nrkei.training.microservices.river.River

// Simulates an event bus
internal class TestConnection(private val maxReadCount: Int = 9) : RapidsConnection {
    private val rivers = mutableListOf<RapidsConnection.MessageListener>()
    internal val allMessages = mutableListOf<String>()
    internal val allPackets = mutableListOf<RapidsPacket>()
    internal val busMessages = mutableListOf<String>()

    override fun register(listener: River.PacketListener) {
        River(this, listener.rules, maxReadCount).also { river ->
            river.register(listener)
            rivers.add(river)
        }
    }

    override fun register(listener: River.SystemListener) {
        River(this, listener.rules, maxReadCount).also { river ->
            river.register(listener)
            rivers.add(river)
        }
    }

    override fun publish(packet: RapidsPacket) {
        allPackets.add(packet)
        publish(packet.toJsonString())
    }

    internal fun publish(message: String) {
        allMessages.add(message)
        if (busMessages.isNotEmpty()) busMessages.add(message)
        else {
            busMessages.add(message)
            while (busMessages.isNotEmpty()) {
                busMessages.first().also { nextMessage ->
                    rivers.forEach { it.message(this, nextMessage) }
                    busMessages.removeAt(0)
                }
            }
        }
    }

}