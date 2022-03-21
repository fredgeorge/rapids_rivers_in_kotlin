package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper

// Understands a request to reaffirm operational status
class HeartBeat : Packet {

    fun toJsonString() = mutableMapOf<String, Any>()
        .also { map ->
            map[Packet.PACKET_TYPE] = Packet.SYSTEM_PACKET_TYPE
            map[Packet.SYSTEM_PURPOSE] = Packet.HEART_BEAT_PURPOSE
        }
        .let { ObjectMapper().writeValueAsString(it) }

}