/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.river

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nrkei.training.microservices.rapid.filter.Validation
import com.nrkei.training.microservices.rapid.packet.HeartBeat
import com.nrkei.training.microservices.rapid.packet.Packet
import com.nrkei.training.microservices.rapid.river.RapidsConnection.MessageListener

// Understands a themed flow of messages
class River(
    private val connection: RapidsConnection,
    private val rules: List<Validation> = emptyList(),
    private val maxReadCount: Int = 0
) : MessageListener {

    init {
        connection.register(this)
    }

    private val listeners = mutableListOf<PacketListener>()

    infix fun register(listener: PacketListener) = listeners.add(listener)

    override fun message(sendPort: RapidsConnection, message: String) {
        try {
            println(message)
            Packet(ObjectMapper().readValue<Map<String, Any>>(message)).apply {
                println(this)
                when {
                    hasInvalidReadCount(maxReadCount) -> this@River.triggerInvalidPacket(this)
                    isHeartBeat() -> this@River.triggerHeartBeat(this)
                    doesMeetRules(rules) -> this@River.triggerPacket(this)
                    else -> this@River.triggerFailingPacket(this)
                }
                println(this)
            }
        } catch (e: JsonParseException) {
            println(e.message)
        }
    }

    private fun triggerInvalidPacket(packet: Packet) {

    }

    private fun triggerHeartBeat(packet: Packet) {
        listeners.forEach { service ->
            if(service.isStillAlive()) {
                packet[HeartBeat.HEART_BEAT_RESPONDER] = service.name
                connection.publish(packet.toJsonString())
            }
        }
    }

    private fun triggerFailingPacket(packet: Packet) {

    }

    private fun triggerPacket(packet: Packet) {

    }

    interface PacketListener {
        val name: String get() = "${this.javaClass.simpleName} [${this.hashCode()}]"
        fun isStillAlive(): Boolean = true
    }
}