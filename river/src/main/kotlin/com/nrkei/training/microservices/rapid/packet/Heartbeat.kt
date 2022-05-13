/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.rapid.filter.rules
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PURPOSE
import com.nrkei.training.microservices.rapid.river.RapidsPacket

// Understands a request to reaffirm operational status
class HeartBeat : RapidsPacket {
    companion object {
        internal const val HEART_BEAT_PURPOSE = "heart_beat"
        internal const val HEART_BEAT_RESPONDER = "heart_beat_responder"

        internal val rules = rules {
            require key PACKET_TYPE value SYSTEM_PACKET_TYPE
            require key SYSTEM_PURPOSE value HEART_BEAT_PURPOSE
            forbid key HEART_BEAT_RESPONDER
        }
    }

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(
        mapOf(
            PACKET_TYPE to SYSTEM_PACKET_TYPE,
            SYSTEM_PURPOSE to HEART_BEAT_PURPOSE
        )
    )

}