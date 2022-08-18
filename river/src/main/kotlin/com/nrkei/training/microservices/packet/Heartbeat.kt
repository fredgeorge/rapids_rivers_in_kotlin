/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet.Companion.COMMUNITY_KEY
import com.nrkei.training.microservices.packet.Packet.Companion.PACKET_TYPE_KEY
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_COMMUNITY_VALUE
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_PACKET_TYPE_VALUE
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_PURPOSE
import kotlin.math.absoluteValue
import kotlin.random.Random

// Understands a request to reaffirm operational status
class HeartBeat : RapidsPacket {
    companion object {
        internal const val HEART_BEAT_PURPOSE = "heart_beat"
        internal const val HEART_BEAT_GENERATOR = "heart_beat_generator"
        internal const val HEART_BEAT_RESPONDER = "heart_beat_responder"
        internal const val HEART_BEAT_INDEX = "heart_beat_index"

        internal val rules = rules {
            require key COMMUNITY_KEY value SYSTEM_COMMUNITY_VALUE
            require key PACKET_TYPE_KEY value SYSTEM_PACKET_TYPE_VALUE
            require key SYSTEM_PURPOSE value HEART_BEAT_PURPOSE
            require key HEART_BEAT_INDEX
            forbid key HEART_BEAT_RESPONDER
        }
    }

    private var index = 0
    private val generatorId = Random.nextInt().absoluteValue
    private val fields = mapOf(
        COMMUNITY_KEY to SYSTEM_COMMUNITY_VALUE,
        PACKET_TYPE_KEY to SYSTEM_PACKET_TYPE_VALUE,
        SYSTEM_PURPOSE to HEART_BEAT_PURPOSE,
        HEART_BEAT_GENERATOR to generatorId
    )

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(
        fields.toMutableMap().also { it[HEART_BEAT_INDEX] = ++index }
    )

    override fun toString(): String = ObjectMapper().writeValueAsString(
        fields.toMutableMap().also { it[HEART_BEAT_INDEX] = index + 1 }.toString()
    )
}
