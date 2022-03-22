/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.rapid.packet.LogPacket
import com.nrkei.training.microservices.rapid.packet.LogPacket.Companion.INVALID_JSON
import com.nrkei.training.microservices.rapid.packet.Packet
import com.nrkei.training.microservices.rapid.river.RapidsConnection
import com.nrkei.training.microservices.rapid.river.RapidsConnection.MessageListener
import com.nrkei.training.microservices.rapid.river.River
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Ensures system errors trigger reaction
internal class UnexpectedPacketTest {

    @Test
    fun `invalid JSON`() {
        TestConnection().also { rapids ->
            River(rapids).also { river ->
                river.register(TestSystemService(rapids))
                rapids.injectMessage("qwerty")
                rapids.sentMessages.also { messages ->
                    assertEquals(1, messages.size)
                    messages.first().also { message ->
                        assertTrue("log_severity" in message)
                        assertTrue("error" in message)
                        assertTrue("log_detail" in message)
                        println(message)
                    }
                }
            }
        }
    }

    private class TestConnection : RapidsConnection {
        private val rivers = mutableListOf<MessageListener>()
        val sentMessages = mutableListOf<String>()

        override fun register(listener: MessageListener) {
            rivers.add(listener)
        }

        override fun publish(message: String) {
            sentMessages.add(message)
        }

        fun injectMessage(content: String) = rivers.forEach { it.message(this, content) }
    }

    private class TestSystemService(private val rapids: RapidsConnection) : River.SystemListener {
        override fun isStillAlive() = true

        override fun invalidFormat(invalidString: String) {
            LogPacket.error(INVALID_JSON, name).apply {
                details(invalidString)
                rapids.publish(this.toJsonString())
            }
        }

        override fun loopDetected(packet: Packet) {
            TODO("Not yet implemented")
        }
    }
}