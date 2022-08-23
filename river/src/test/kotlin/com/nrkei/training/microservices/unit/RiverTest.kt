/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * MIT License
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.util.TestConnection
import com.nrkei.training.microservices.util.TestService
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

    private val connection = TestConnection(2)

    @Test
    fun `unfiltered service`() {
        TestService(emptyList()).also { service ->
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

    private fun assertSize(expectedSize: Int, results: List<*>) {
        assertEquals(expectedSize, results.size)
    }

}