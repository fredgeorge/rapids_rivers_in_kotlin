/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.rapid.filter.rules
import com.nrkei.training.microservices.rapid.packet.HeartBeat
import com.nrkei.training.microservices.rapid.packet.Packet
import com.nrkei.training.microservices.rapid.river.PacketProblems
import com.nrkei.training.microservices.rapid.river.RapidsConnection
import com.nrkei.training.microservices.rapid.river.River.PacketListener
import com.nrkei.training.microservices.unit.util.TestConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Ensures system heartbeat behavior works
internal class HeartbeatTest {

    @Test
    fun `positive response`() {
        TestConnection().also { rapids ->
            rapids.register(TestService(isAliveResponse = true))
            rapids.injectMessage(HeartBeat().toJsonString())
            assertEquals(2, rapids.sentMessages.size)
            assertTrue("heart_beat_responder" in rapids.sentMessages[1])
        }
    }

    @Test
    fun `log failure if negative`() {
        TestConnection().also { rapids ->
            rapids.register(TestService(isAliveResponse = false))
            rapids.injectMessage(HeartBeat().toJsonString())
            rapids.sentMessages.also { messages ->
                assertEquals(2, messages.size)
                messages[1].also { message ->
                    assertTrue("log_severity" in message)
                    assertTrue("error" in message)
                    println(message)
                }
            }
        }
    }


    private class TestService(private val isAliveResponse: Boolean) : PacketListener {
        override val rules = rules { }
        override fun isStillAlive(connection: RapidsConnection) = isAliveResponse
        override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
            infoWarnings.severeError("Unexpected invocation of packet API")
        }

        override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
            problems.severeError("Unexpected invocation of rejectedPacket API")
        }
    }
}