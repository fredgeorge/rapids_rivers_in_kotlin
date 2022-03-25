/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.rapid.packet.LogPacket
import com.nrkei.training.microservices.rapid.packet.LogPacket.Companion.INVALID_JSON
import com.nrkei.training.microservices.rapid.packet.Packet
import com.nrkei.training.microservices.rapid.river.PacketProblems
import com.nrkei.training.microservices.rapid.river.RapidsConnection
import com.nrkei.training.microservices.rapid.river.River
import com.nrkei.training.microservices.unit.util.TestConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Ensures system errors trigger reaction
internal class InvalidPacketTest {

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

    private class TestSystemService(private val rapids: RapidsConnection) : River.SystemListener {
        override fun isStillAlive() = true

        override fun invalidFormat(invalidString: String, problems: PacketProblems) {
            LogPacket.error(INVALID_JSON, name).apply {
                details(invalidString)
                rapids.publish(this)
            }
        }

        override fun loopDetected(packet: Packet, problems: PacketProblems) {
            problems.severeError("Unexpected invocation of loopDetected API")
        }

        override fun packet(packet: Packet, infoWarnings: PacketProblems) {
            infoWarnings.severeError("Unexpected invocation of packet API")
        }

        override fun rejectedPacket(packet: Packet, problems: PacketProblems) {
            problems.severeError("Unexpected invocation of rejectedPacket API")
        }
    }
}