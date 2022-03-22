package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.rapid.filter.rules
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PURPOSE

// Understands a request to reaffirm operational status
class HeartBeat {
    companion object {
        internal const val HEART_BEAT_PURPOSE = "heart_beat"
        internal const val HEART_BEAT_RESPONDER = "heart_beat_responder"

        internal val validations = rules {
            require key PACKET_TYPE value SYSTEM_PACKET_TYPE
            require key SYSTEM_PURPOSE value HEART_BEAT_PURPOSE
            forbid key HEART_BEAT_RESPONDER
        }
    }

    fun toJsonString() = mutableMapOf<String, Any>()
        .also { map ->
            map[PACKET_TYPE] = SYSTEM_PACKET_TYPE
            map[SYSTEM_PURPOSE] = HEART_BEAT_PURPOSE
        }
        .let { ObjectMapper().writeValueAsString(it) }

}