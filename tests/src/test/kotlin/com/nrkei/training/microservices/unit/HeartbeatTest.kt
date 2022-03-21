/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.rapid.packet.HeartBeat
import com.nrkei.training.microservices.rapid.river.RapidsConnection
import com.nrkei.training.microservices.rapid.river.RapidsConnection.MessageListener
import com.nrkei.training.microservices.rapid.river.River
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// Ensures system heartbeat behavior works
internal class HeartbeatTest {

    @Test fun `positive default response`() {
        TestConnection().also { rapids ->
            River(rapids).also { river ->
                rapids.injectMessage(HeartBeat().toJsonString())
                assertEquals(1, rapids.sendMessages.size)
            }
        }
    }

    private class TestConnection : RapidsConnection {
        private val rivers = mutableListOf<MessageListener>()
        val sendMessages = mutableListOf<String>()

        override fun register(listener: MessageListener) {
            rivers.add(listener)
        }

        override fun publish(message: String) {
            sendMessages.add(message)
        }

        fun injectMessage(content: String) = rivers.forEach { it.message(this, content) }
    }
}