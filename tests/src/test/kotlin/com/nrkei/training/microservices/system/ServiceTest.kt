/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.system

import com.nrkei.training.microservices.rapid.packet.Packet
import com.nrkei.training.microservices.rapid.river.PacketProblems
import com.nrkei.training.microservices.rapid.river.RapidsConnection
import com.nrkei.training.microservices.rapid.river.RapidsPacket
import com.nrkei.training.microservices.rapid.river.River
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class ServiceTest {
    companion object {
        private const val SOLUTION_STRING = "{\"need\":\"car_rental_offer\"," +
                "\"user_id\":456," +
                "\"solutions\":[" +
                "{\"offer\":\"15% discount\"}," +
                "{\"offer\":\"500 extra points\"}," +
                "{\"offer\":\"free upgrade\"}" +
                "]," +
                "\"frequent_renter\":\"\"," +
                "\"system_read_count\":2," +
                "\"contributing_services\":[]}"

        private const val MISSING_COMMA = "{\"frequent_renter\":\"\" \"read_count\":2}"

        private const val NEED_KEY = "need"
        private const val KEY_TO_BE_ADDED = "key_to_be_added"
        private const val EMPTY_ARRAY_KEY = "contributing_services"
        private const val EMPTY_STRING_KEY = "frequent_renter"
        private const val INTERESTING_KEY = "frequent_renter"
        private const val SOLUTIONS_KEY = "solutions"
    }

    private lateinit var connection: TestConnection
    private lateinit var river: River

    @BeforeEach
    fun setup() {
        connection = TestConnection()
        river = River(connection).also { connection.register(it) }
    }

    @Test
    fun `valid JSON extracted`() {
        river.register(object: TestService() {
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                assertFalse(infoWarnings.hasErrors())
            }
        })
        connection.injectMessage(SOLUTION_STRING)
    }

    private class TestConnection : RapidsConnection {
        private val rivers = mutableListOf<RapidsConnection.MessageListener>()

        override fun register(listener: RapidsConnection.MessageListener) {
            rivers.add(listener)
        }

        override fun publish(message: RapidsPacket) {
            throw IllegalStateException("The publish API should not be used")
        }

        internal fun injectMessage(content: String) = rivers.forEach { it.message(this, content) }
    }

    private open class TestService: River.PacketListener {
        override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
            fail("Unexpected success parsing JSON packet. Packet is: \n" +
                packet.toJsonString() +
                "\nWarnings discovered were:\n" +
                infoWarnings.toString()
            )
        }

    }
}
