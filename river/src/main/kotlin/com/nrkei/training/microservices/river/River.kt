/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.river

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.packet.HeartBeat
import com.nrkei.training.microservices.packet.LogPacket
import com.nrkei.training.microservices.packet.LogPacket.Companion.SERVICE_NOT_RESPONDING
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_BREADCRUMBS
import com.nrkei.training.microservices.packet.StartUpPacket
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.RapidsConnection.MessageListener

// Understands a themed flow of messages
class River(
    private val connection: RapidsConnection,
    private val rules: List<Validation> = emptyList(),
    private val maxReadCount: Int
) : MessageListener {

    private val listeners = mutableListOf<PacketListener>()
    private val systemListeners = mutableListOf<SystemListener>()

    infix fun register(listener: PacketListener) = listeners.add(listener).also {
        connection.publish(StartUpPacket(listener))
    }

    infix fun register(listener: SystemListener) = systemListeners.add(listener).also {
        register(listener as PacketListener)
    }

    override fun message(sendPort: RapidsConnection, message: String) {
        PacketProblems(message).also { problems ->
            try {
                Packet(ObjectMapper().readValue<Map<String, Any>>(message)).apply {
                    if (hasInvalidReadCount(maxReadCount)) {
                        this@River.triggerLoopDetection(this, problems)
                        return
                    }
                    if (isHeartBeat()) this@River.triggerHeartBeat(this.clone())
                    if (doesMeetRules(rules, problems)) this@River.triggerPacket(this, problems)
                    else this@River.triggerRejectedPacket(this, problems)
                }
            } catch (e: JsonParseException) {
                problems.error("Invalid JSON format detected")
                triggerInvalidPacket(message, problems)
            }
        }
    }

    private fun triggerInvalidPacket(message: String, problems: PacketProblems) {
        systemListeners.forEach { service -> service.invalidFormat(connection, message, problems) }
    }

    private fun triggerLoopDetection(packet: Packet, problems: PacketProblems) {
        systemListeners.forEach { service -> service.loopDetected(connection, packet, problems) }
    }

    private fun triggerHeartBeat(packet: Packet) {
        listeners.forEach { service ->
            if(service.isStillAlive(connection)) {
                packet[HeartBeat.HEART_BEAT_RESPONDER] = service.name
                connection.publish(packet)
            }
            else {
                connection.publish(LogPacket.error(SERVICE_NOT_RESPONDING, service.name))
            }
        }
    }

    private fun triggerPacket(packet: Packet, infoWarnings: PacketProblems) {
        @Suppress("UNCHECKED_CAST") val breadcrumbs = (packet[SYSTEM_BREADCRUMBS] as List<String>?) ?: emptyList()
        listeners.forEach { service ->
            packet[SYSTEM_BREADCRUMBS] = breadcrumbs + service.name
            service.packet(connection, packet, infoWarnings)
        }
    }

    private fun triggerRejectedPacket(packet: Packet, problems: PacketProblems) {
        listeners.forEach { service -> service.rejectedPacket(connection, packet, problems) }
    }

    interface PacketListener {
        val name: String get() = "${this.javaClass.simpleName} [${this.hashCode()}]"
        val rules: List<Validation>
        fun isStillAlive(connection: RapidsConnection): Boolean = true // Default if there is nothing specific to verify
        fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems)
        fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {} // Optional; for debugging
    }

    interface SystemListener : PacketListener {
        fun invalidFormat(connection: RapidsConnection, invalidString: String, problems: PacketProblems)
        fun loopDetected(connection: RapidsConnection, packet: Packet, problems: PacketProblems)
    }
}