/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * MIT License
 */

package com.nrkei.training.microservices.unit

import com.nrkei.training.microservices.packet.Packet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PacketTest {
    companion object {
        private val packet = Packet(
            "string_key" to "rental_offer_engine",
            "integer_key" to 7,
            "double_key" to 7.5,
//            "null_key" to null,
            "empty_string" to "",
            "boolean_key" to true,
            "boolean_string_key" to "false",
            "date_time_key" to "2022-03-03T00:00:00Z",
            "string_list_key" to listOf("foo", "bar"),
            "integer_list_key" to listOf(2, 4),
            "empty_list_key" to emptyList<String>(),
            "detail_key" to mapOf(
                "detail_string_key" to "upgrade",
                "detail_double_key" to 10.75
            )
        )
    }

    @Test
    fun `fetch nuggets`() {
        assertEquals("rental_offer_engine", packet["string_key"])
        assertEquals(7, packet["integer_key"])
        assertEquals(7.5, packet["double_key"])
        assertEquals(true, packet["boolean_key"])
        assertEquals("false", packet["boolean_string_key"])
        assertEquals(LocalDateTime.of(2022, 3, 3, 0, 0), packet.dateTime("date_time_key"))
        assertEquals(listOf("foo", "bar"), packet["string_list_key"])
        assertEquals(listOf(2, 4), packet["integer_list_key"])
    }

    @Test
    fun `is missing`() {
        assertTrue(packet.isLacking("foo"))
        assertTrue(packet.isLacking("empty_string"))
        assertTrue(packet.isLacking("empty_list_key"))
    }

    @Test
    fun `detail extraction`() {
        assertEquals("upgrade", packet.subPacket("detail_key")["detail_string_key"])
        assertEquals("upgrade", (packet["detail_key"] as Map<String, Any>)["detail_string_key"])
        assertEquals(10.75, packet.subPacket("detail_key")["detail_double_key"])
    }
}