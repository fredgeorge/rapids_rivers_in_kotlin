/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.rapid.filter.Validation

class Packet internal constructor(map: Map<String, Any>) {
    companion object {
        internal const val PACKET_TYPE = "packet_type"
        internal const val SYSTEM_PACKET_TYPE = "system_packet"
        internal const val SYSTEM_PURPOSE = "system_purpose"
        internal const val READ_COUNT = "system_read_count"
    }

    private val map = map.toMutableMap()

    internal fun hasInvalidReadCount(maxReadCount: Int): Boolean =
        map.getOrPut(READ_COUNT, { 0 }).let {
            map[READ_COUNT] = (it as Int) + 1
            !(maxReadCount == 0 || map[READ_COUNT] as Int <= maxReadCount)
        }

    internal fun isHeartBeat() = doesMeetRules(HeartBeat.validations)

    internal fun doesMeetRules(rules: List<Validation>) = rules
        .map { it.isValid(this) }
        .all { it }

    operator fun get(key: String) = map[key]

    operator fun set(key: String, value: Any) = map.set(key, value)

    internal fun toJsonString() = ObjectMapper().writeValueAsString(map)

    override fun toString() = map.toString()
}