/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.LogPacket
import com.nrkei.training.microservices.packet.LogPacket.Companion.INVALID_JSON
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.river.PacketProblems
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.river.River.SystemListener
import com.nrkei.training.microservices.util.TestConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Ensures system errors trigger reaction
internal class InvalidPacketTest {

    @Test
    fun `invalid JSON`() {
        TestConnection().also { rapids ->
            rapids.register(TestSystemService(rapids))
            rapids.injectMessage("qwerty")
            rapids.sentMessages.also { messages ->
                assertEquals(2, messages.size)
                messages[1].also { message ->
                    assertTrue("log_severity" in message)
                    assertTrue("error" in message)
                    assertTrue("log_message" in message)
                    println(message)
                }
            }
        }
    }

    private class TestSystemService(private val rapids: RapidsConnection) : SystemListener {
        override val rules = rules { }

        override fun isStillAlive(connection: RapidsConnection) = true

        override fun invalidFormat(connection: RapidsConnection, invalidString: String, problems: PacketProblems) {
            LogPacket.error(INVALID_JSON, name).apply {
                message(invalidString)
                rapids.publish(this)
            }
        }

        override fun loopDetected(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
            problems.severeError("Unexpected invocation of loopDetected API")
        }

        override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
            infoWarnings.severeError("Unexpected invocation of packet API")
        }

        override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
            problems.severeError("Unexpected invocation of rejectedPacket API")
        }
    }
}