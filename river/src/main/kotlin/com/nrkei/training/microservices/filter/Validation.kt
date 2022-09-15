/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.filter

import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.river.Status

interface Validation {
    fun isValid(packet: Packet, problems: Status): Boolean
}

class KeyValueValidation(private val key: String, private val value: Any): Validation {
    override fun isValid(packet: Packet, problems: Status) = (packet[key] == value).also { isValid ->
        if (isValid) return@also
        problems.error(
            if(packet.isLacking(key)) "Key '$key' is missing"
            else "Key '$key' has value '${packet[key]} rather than expected '$value'"
        )
    }
}

class KeyExistenceValidation(private val key: String): Validation {
    override fun isValid(packet: Packet, problems: Status) = (!packet.isLacking(key)).also { isValid ->
        if (!isValid) problems.error("Key '$key' is missing")
    }
}

class KeyAbsenceValidation(private val key: String): Validation {
    override fun isValid(packet: Packet, problems: Status) = packet.isLacking(key).also { isValid ->
        if (!isValid) problems.error("Key '$key' exists (unexpectedly?)")
    }
}