/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.packet.Packet.Companion.COMMUNITY_KEY
import com.nrkei.training.microservices.packet.Packet.Companion.PACKET_TYPE_KEY
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_COMMUNITY_VALUE
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_PACKET_TYPE_VALUE
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_PURPOSE
import com.nrkei.training.microservices.river.River

// Understands a service is ready for traffic
class StartUpPacket internal constructor(service: River.PacketListener) : RapidsPacket {
    companion object {
        private const val START_UP_PURPOSE = "start_up"
        private const val SERVICE_NAME = "service_name"
    }

    private val serviceName = service.name

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(
        mapOf<String, Any>(
            COMMUNITY_KEY to SYSTEM_COMMUNITY_VALUE,
            PACKET_TYPE_KEY to SYSTEM_PACKET_TYPE_VALUE,
            SYSTEM_PURPOSE to START_UP_PURPOSE,
            SERVICE_NAME to serviceName
        )
    )
}