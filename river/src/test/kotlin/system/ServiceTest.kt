/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package system

import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.river.PacketProblems
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.packet.RapidsPacket
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.River.PacketListener
import com.nrkei.training.microservices.river.River.SystemListener
import org.junit.jupiter.api.Assertions.*
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
    }

    private lateinit var connection: TestConnection

    @BeforeEach
    fun setup() {
        connection = TestConnection()
    }

    @Test fun `valid JSON extracted`() {
        connection.register(object: PacketListener {
            override val rules = rules {  }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                println(infoWarnings)
                assertFalse(infoWarnings.hasErrors())
            }
        })
        connection inject SOLUTION_STRING
    }

    @Test fun `invalid JSON format`() {
        var invocationCount = 0
        connection.register(object: TestSystemService() {
            override fun invalidFormat(connection: RapidsConnection, invalidString: String, problems: PacketProblems) {
                assertTrue(problems.hasErrors())
                invocationCount += 1
            }
        })
        connection inject MISSING_COMMA
        assertEquals(1, invocationCount)
    }

    @Test fun `required key exists`() {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules { require key NEED_KEY }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                assertFalse(infoWarnings.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
    }

    @Test fun `required key with required value exists`() {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules { require key NEED_KEY value "car_rental_offer" }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                assertFalse(infoWarnings.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
    }

    @Test fun `haw required key, but wrong value`() {
        assertRejectedPacket(rules { require key NEED_KEY value "hotel_offer" })
    }

    @Test fun `forbidden key missing`() {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules { forbid key "no such key" }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                assertFalse(infoWarnings.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
    }

    @Test fun `forbidden key exists`() {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules { forbid key NEED_KEY }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                fail("Unexpected invocation of packer API\n")
            }
            override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
                assertTrue(problems.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
    }

    @Test fun `required key can have value set or changed`() {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules { require key NEED_KEY value "car_rental_offer" }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                packet[NEED_KEY] = "hotel_offer"
                connection.publish(packet)
                assertFalse(infoWarnings.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
        assertTrue("hotel_offer" in connection.sentMessages[1])
    }

    @Test fun `forbidden key can have value set`() {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules { forbid key KEY_TO_BE_ADDED }
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                packet[KEY_TO_BE_ADDED] = "hotel_offer"
                connection.publish(packet)
                assertFalse(infoWarnings.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
        assertTrue("hotel_offer" in connection.sentMessages[1])
    }

    @Test fun `missing, empty string, or empty array all are considered missing`() {
        assertValidPacket(rules { forbid key KEY_TO_BE_ADDED })
        assertValidPacket(rules { forbid key EMPTY_ARRAY_KEY })
        assertValidPacket(rules { forbid key EMPTY_STRING_KEY })
    }

    @Test fun `missing, empty string, or empty array all fail required validation`() {
        assertRejectedPacket(rules { require key "missing key" })
        assertRejectedPacket(rules { require key EMPTY_ARRAY_KEY })
        assertRejectedPacket(rules { require key EMPTY_STRING_KEY })
    }

    private fun assertValidPacket(rules: List<Validation>) {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                assertFalse(infoWarnings.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
    }

    private fun assertRejectedPacket(rules: List<Validation>) {
        var invocationCount = 0
        connection.register(object: PacketListener {
            override val rules = rules
            override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
                fail("Unexpected invocation of packer API\n")
            }
            override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
                assertTrue(problems.hasErrors())
                invocationCount += 1
            }
        })
        connection inject SOLUTION_STRING
        assertEquals(1, invocationCount)
    }

    private class TestConnection : RapidsConnection {
        private val rivers = mutableListOf<RapidsConnection.MessageListener>()
        val sentMessages = mutableListOf<String>()

        override fun register(listener: PacketListener) {
            River(this, listener.rules, 0).also { river ->
                rivers.add(river)
                river.register(listener) }
        }

        override fun register(listener: SystemListener) {
            River(this, listener.rules, 0).also { river ->
                rivers.add(river)
                river.register(listener) }
        }

        override fun publish(packet: RapidsPacket) {
            sentMessages.add(packet.toJsonString())
        }

        infix fun inject(content: String) = rivers.forEach { it.message(this, content) }
    }

    private open class TestSystemService: SystemListener {
        override val rules = rules {  }

        override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
            fail("Unexpected invocation of packer API. Packet is: \n" +
                packet.toJsonString() +
                "\nWarnings discovered were:\n" +
                infoWarnings.toString()
            )
        }

        override fun invalidFormat(connection: RapidsConnection, invalidString: String, problems: PacketProblems) {
            fail("Unexpected invocation of invalidFormat API. Message is: \n" +
                    invalidString +
                    "\nProblems discovered were:\n" +
                    problems.toString()
            )
        }

        override fun loopDetected(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
            fail("Unexpected invocation of loopDetected API. Packet is: \n" +
                    packet.toJsonString() +
                    "\nProblems discovered were:\n" +
                    problems.toString()
            )
        }


    }
}
