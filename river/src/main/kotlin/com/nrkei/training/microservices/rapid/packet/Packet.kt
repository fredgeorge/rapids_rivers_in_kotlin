/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.rapid.filter.Validation
import com.nrkei.training.microservices.rapid.river.PacketProblems
import com.nrkei.training.microservices.rapid.river.RapidsPacket

class Packet internal constructor(map: Map<String, Any>) : RapidsPacket {
    companion object {
        internal const val PACKET_TYPE = "packet_type"
        internal const val SYSTEM_PACKET_TYPE = "system_packet"

        internal const val SYSTEM_PURPOSE = "system_purpose"

        internal const val SYSTEM_READ_COUNT = "system_read_count"

        internal const val SYSTEM_BREADCRUMBS = "system_breadcrumbs"
    }

    constructor(vararg pairs: Pair<String, Any>): this(pairs.toMap())

    private val map = map.toMutableMap()

    internal fun hasInvalidReadCount(maxReadCount: Int): Boolean =
        map.getOrPut(SYSTEM_READ_COUNT) { 0 }.let {
            map[SYSTEM_READ_COUNT] = (it as Int) + 1
            !(maxReadCount == 0 || map[SYSTEM_READ_COUNT] as Int <= maxReadCount)
        }

    internal fun isHeartBeat() = doesMeetRules(HeartBeat.validations, noProblemTracking)

    internal fun doesMeetRules(rules: List<Validation>, problems: PacketProblems) = rules
        .map { it.isValid(this, problems) }
        .all { it }

    operator fun get(key: String) = map[key]

    operator fun set(key: String, value: Any) = map.set(key, value)

    fun isMissing(key: String) = map[key] == null || isEmpty(key)

    private fun isEmpty(key: String): Boolean {
        return map[key]?.let { value ->
            if (value is String && value.isEmpty()) return true
            return value is List<*> && value.size == 0
        } ?: true
    }

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(map)

    override fun toString() = map.toString()

    private val noProblemTracking get() = PacketProblems("")
}