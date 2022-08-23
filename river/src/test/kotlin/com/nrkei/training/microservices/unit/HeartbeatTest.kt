/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.HeartBeat
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.river.Status
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.river.River.PacketListener
import com.nrkei.training.microservices.util.TestConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// Ensures system heartbeat behavior works
internal class HeartbeatTest {

    @Test
    fun `positive response`() {
        TestConnection().also { rapids ->
            TestService(isAliveResponse = true).also { service ->
                rapids.register(service)
                rapids.publish(HeartBeat().toJsonString())
                assertEquals(2, rapids.allMessages.size)
                assertEquals(1, service.packetCount) // Heartbeat does get through
                assertTrue("heart_beat_responder" in rapids.allMessages[1])
            }
        }
    }

    @Test
    fun `log failure if negative`() {
        TestConnection().also { rapids ->
            TestService(isAliveResponse = false).also { service ->
                rapids.register(service)
                rapids.publish(HeartBeat().toJsonString())
                rapids.allMessages.also { messages ->
                    assertEquals(2, messages.size)
                    assertEquals(1, service.packetCount)
                    messages[1].also { message ->
                        assertTrue("log_severity" in message)
                        assertTrue("error" in message)
                        println(message)
                    }
                }
            }
        }
    }


    private class TestService(private val isAliveResponse: Boolean) : PacketListener {
        var packetCount = 0
        override val rules = rules { }
        override fun isStillAlive(connection: RapidsConnection) = isAliveResponse
        override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
            packetCount += 1
        }

        override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: Status) {
            problems.severeError("Unexpected invocation of rejectedPacket API")
        }
    }
}