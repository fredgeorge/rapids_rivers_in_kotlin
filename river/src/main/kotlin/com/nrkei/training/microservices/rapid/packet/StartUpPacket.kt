/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PURPOSE
import com.nrkei.training.microservices.rapid.river.RapidsPacket
import com.nrkei.training.microservices.rapid.river.River

// Understands a service is ready for traffic
class StartUpPacket internal constructor(service: River.PacketListener) : RapidsPacket {
    companion object {
        private const val START_UP_PURPOSE = "start_up"
        private const val SERVICE_NAME = "service_name"
    }

    private val serviceName = service.name

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(
        mapOf<String, Any>(
            PACKET_TYPE to SYSTEM_PACKET_TYPE,
            SYSTEM_PURPOSE to START_UP_PURPOSE,
            SERVICE_NAME to serviceName
        )
    )
}