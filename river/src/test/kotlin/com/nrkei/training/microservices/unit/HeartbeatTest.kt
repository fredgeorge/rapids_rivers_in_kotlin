/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.rapid.packet.HeartBeat
import com.nrkei.training.microservices.rapid.packet.Packet
import com.nrkei.training.microservices.rapid.river.PacketProblems
import com.nrkei.training.microservices.rapid.river.River
import com.nrkei.training.microservices.unit.util.TestConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Ensures system heartbeat behavior works
internal class HeartbeatTest {

    @Test
    fun `positive response`() {
        TestConnection().also { rapids ->
            River(rapids).also { river ->
                river.register(TestService(isAliveResponse = true))
                rapids.injectMessage(HeartBeat().toJsonString())
                assertEquals(1, rapids.sentMessages.size)
                assertTrue("heart_beat_responder" in rapids.sentMessages.first())
            }
        }
    }

    @Test
    fun `log failure if negative`() {
        TestConnection().also { rapids ->
            River(rapids).also { river ->
                river.register(TestService(isAliveResponse = false))
                rapids.injectMessage(HeartBeat().toJsonString())
                rapids.sentMessages.also { messages ->
                    assertEquals(1, messages.size)
                    messages.first().also { message ->
                        assertTrue("log_severity" in message)
                        assertTrue("error" in message)
                        println(message)
                    }
                }
            }
        }
    }


    private class TestService(private val isAliveResponse: Boolean) : River.PacketListener {
        override fun isStillAlive() = isAliveResponse
        override fun packet(packet: Packet, infoWarnings: PacketProblems) {
            infoWarnings.severeError("Unexpected invocation of packet API")
        }
        override fun rejectedPacket(packet: Packet, problems: PacketProblems) {
            problems.severeError("Unexpected invocation of rejectedPacket API")
        }
    }
}