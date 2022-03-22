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
import com.nrkei.training.microservices.rapid.packet.LogPacket
import com.nrkei.training.microservices.rapid.packet.LogPacket.Companion.SERVICE_NOT_RESPONDING
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
    private val systemListeners = mutableListOf<SystemListener>()

    infix fun register(listener: PacketListener) = listeners.add(listener)

    infix fun register(listener: SystemListener) = systemListeners.add(listener).also {
        listeners.add(listener)
    }

    override fun message(sendPort: RapidsConnection, message: String) {
        try {
            println(message)
            Packet(ObjectMapper().readValue<Map<String, Any>>(message)).apply {
                println(this)
                when {
                    hasInvalidReadCount(maxReadCount) -> this@River.triggerLoopDetection(this)
                    isHeartBeat() -> this@River.triggerHeartBeat(this)
                    doesMeetRules(rules) -> this@River.triggerPacket(this)
                    else -> this@River.triggerFailingPacket(this)
                }
                println(this)
            }
        } catch (e: JsonParseException) {
            triggerInvalidPacket(message)
        }
    }

    private fun triggerInvalidPacket(message: String) {
        systemListeners.forEach { service -> service.invalidFormat(message) }
    }

    private fun triggerLoopDetection(packet: Packet) {
        systemListeners.forEach { service -> service.loopDetected(packet) }
    }

    private fun triggerHeartBeat(packet: Packet) {
        listeners.forEach { service ->
            if(service.isStillAlive()) {
                packet[HeartBeat.HEART_BEAT_RESPONDER] = service.name
                connection.publish(packet.toJsonString())
            }
            else {
                connection.publish(LogPacket.error(SERVICE_NOT_RESPONDING, service.name).toJsonString())
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

    interface SystemListener : PacketListener {
        fun invalidFormat(invalidString: String)
        fun loopDetected(packet: Packet)
    }
}