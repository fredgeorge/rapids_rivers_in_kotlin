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
            require key PACKET_TYPE value SYSTEM_PACKET_TYPE
            require key SYSTEM_PURPOSE value HEART_BEAT_PURPOSE
            require key HEART_BEAT_INDEX
            forbid key HEART_BEAT_RESPONDER
        }
    }

    private var index = 0
    private val generatorId = Random.nextInt().absoluteValue
    private val fields = mapOf(
        PACKET_TYPE to SYSTEM_PACKET_TYPE,
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
