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
internal class TestConnection : RapidsConnection {
    private val rivers = mutableListOf<RapidsConnection.MessageListener>()
    val sentMessages = mutableListOf<String>()

    override fun register(listener: River.PacketListener) {
        River(this, listener.rules, 0).also { river ->
            rivers.add(river)
            river.register(listener) }
    }

    override fun register(listener: River.SystemListener) {
        River(this, listener.rules, 0).also { river ->
            rivers.add(river)
            river.register(listener) }
    }

    override fun publish(packet: RapidsPacket) {
        sentMessages.add(packet.toJsonString())
    }

    internal fun injectMessage(content: String) = rivers.forEach { it.message(this, content) }
}