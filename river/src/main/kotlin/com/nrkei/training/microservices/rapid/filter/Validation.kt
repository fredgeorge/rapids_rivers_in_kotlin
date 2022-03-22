package com.nrkei.training.microservices.rapid.filter

import com.nrkei.training.microservices.rapid.packet.Packet

interface Validation {
    fun isValid(packet: Packet): Boolean
}

internal class KeyValueValidation(private val key: String, private val value: Any): Validation {
    override fun isValid(packet: Packet) = packet[key] == value
}

internal class KeyExistanceValidation(private val key: String): Validation {
    override fun isValid(packet: Packet) = packet[key] != null
}

internal class KeyAbsenseValidation(private val key: String): Validation {
    override fun isValid(packet: Packet) = packet[key] == null
}