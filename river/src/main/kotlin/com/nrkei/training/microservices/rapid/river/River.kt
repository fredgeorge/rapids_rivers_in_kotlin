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
        PacketProblems(message).also { problems ->
            try {
                println(message)
                Packet(ObjectMapper().readValue<Map<String, Any>>(message)).apply {
                    println(this)
                    when {
                        hasInvalidReadCount(maxReadCount) -> this@River.triggerLoopDetection(this, problems)
                        isHeartBeat() -> this@River.triggerHeartBeat(this)
                        doesMeetRules(rules) -> this@River.triggerPacket(this, problems)
                        else -> this@River.triggerRejectedPacket(this, problems)
                    }
                    println(this)
                }
            } catch (e: JsonParseException) {
                problems.error("Invalid JSON format detected")
                triggerInvalidPacket(message, problems)
            }
        }
    }

    private fun triggerInvalidPacket(message: String, problems: PacketProblems) {
        systemListeners.forEach { service -> service.invalidFormat(message, problems) }
    }

    private fun triggerLoopDetection(packet: Packet, problems: PacketProblems) {
        systemListeners.forEach { service -> service.loopDetected(packet, problems) }
    }

    private fun triggerHeartBeat(packet: Packet) {
        listeners.forEach { service ->
            if(service.isStillAlive()) {
                packet[HeartBeat.HEART_BEAT_RESPONDER] = service.name
                connection.publish(packet)
            }
            else {
                connection.publish(LogPacket.error(SERVICE_NOT_RESPONDING, service.name))
            }
        }
    }

    private fun triggerPacket(packet: Packet, infoWarnings: PacketProblems) {
        listeners.forEach { service -> service.packet(packet, infoWarnings) }
    }

    private fun triggerRejectedPacket(packet: Packet, problems: PacketProblems) {
        listeners.forEach { service -> service.rejectedPacket(packet, problems) }
    }

    interface PacketListener {
        val name: String get() = "${this.javaClass.simpleName} [${this.hashCode()}]"
        fun isStillAlive(): Boolean = true
        fun packet(packet: Packet, infoWarnings: PacketProblems)
        fun rejectedPacket(packet: Packet, problems: PacketProblems)
    }

    interface SystemListener : PacketListener {
        fun invalidFormat(invalidString: String, problems: PacketProblems)
        fun loopDetected(packet: Packet, problems: PacketProblems)
    }
}