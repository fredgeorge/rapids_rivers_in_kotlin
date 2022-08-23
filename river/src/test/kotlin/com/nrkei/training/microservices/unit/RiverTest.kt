/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * MIT License
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.HeartBeat
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.util.DeadService
import com.nrkei.training.microservices.util.TestConnection
import com.nrkei.training.microservices.util.TestService
import com.nrkei.training.microservices.util.TestSystemService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class RiverTest {
    companion object {
        private val packet = Packet(
            "string_key" to "rental_offer_engine",
            "integer_key" to 7,
            "double_key" to 7.5,
            "boolean_key" to true,
            "date_time_key" to "2022-03-03T00:00:00Z",
            "string_list_key" to listOf("foo", "bar"),
            "integer_list_key" to listOf(2, 4),
            "detail_key" to mapOf(
                "detail_string_key" to "upgrade",
                "detail_double_key" to 10.75
            )
        )
    }

    private val connection = TestConnection(20)

    @Test
    fun `unfiltered service`() {
        TestService().also { service ->
            connection.register(service)
            connection.publish(packet)
            assertSize(1, service.acceptedPackets)
            assertSize(0, service.rejectedPackets)
            assertSize(1, service.informationStatuses)
            assertSize(0, service.problemStatuses)
        }
    }

    @Test
    fun `filtered services`() {
        val acceptedService = TestService(rules { require key "integer_key" })
        val rejectedService = TestService(rules { forbid key "integer_key" })
        connection.register(acceptedService)
        connection.register(rejectedService)
        connection.publish(packet)
        assertSize(1, acceptedService.acceptedPackets)
        assertFalse(acceptedService.informationStatuses.first().hasErrors())
        assertSize(1, rejectedService.rejectedPackets)
        assertTrue(rejectedService.problemStatuses.first().hasErrors())
    }

    @Test
    fun `invalid JSON`() {
        val normalService = TestService()
        val systemService = TestSystemService()
        connection.register(normalService)
        connection.register(systemService)
        connection.publish("{")
        assertSize(0, normalService.informationStatuses)
        assertSize(0, normalService.problemStatuses)  // Not processed
        assertSize(0, systemService.informationStatuses)
        assertSize(0, systemService.problemStatuses)  // Not treated as packet problem; rather different API
        assertSize(1, systemService.formatProblems)  // Special handling here
    }

    @Test
    fun `start up`() {
        connection.register(TestService())
        assertSize(1, connection.allPackets)
    }

    @Test
    fun `heart beats`() {
        val normalService = TestService()
        val deadService = DeadService()
        val systemService = TestSystemService()
        connection.register(normalService)
        connection.register(deadService)
        connection.register(systemService)
        val heartbeat = HeartBeat()
        connection.publish(heartbeat)
        assertSize(1 + 2, systemService.acceptedPackets)  // one heart beat and 2 responses
        connection.publish(heartbeat)
        connection.publish(heartbeat)
        assertSize(3 + 6, systemService.acceptedPackets)  // one heart beat and 2 responses

    }

    @Test
    fun `loop detection`() {
        TestConnection(2).also { connection -> // a very strict connection
            TestSystemService().also { service ->
                connection.register(service)
                Packet().also { packet ->
                    connection.publish(packet)
                    assertSize(1, service.acceptedPackets)
                    assertSize(0, service.loopPackets)

                    packet.set("system_read_count", 1)
                    connection.publish(packet)
                    assertSize(2, service.acceptedPackets)
                    assertSize(0, service.loopPackets)

                    packet.set("system_read_count", 2)  // threshold is 2; count is incremented, then checked
                    connection.publish(packet)
                    assertSize(2, service.acceptedPackets) // packet never makes it to the service
                    assertSize(1, service.loopPackets)
                }
            }
        }
    }

    private fun assertSize(expectedSize: Int, results: List<*>) {
        assertEquals(expectedSize, results.size)
    }
}
