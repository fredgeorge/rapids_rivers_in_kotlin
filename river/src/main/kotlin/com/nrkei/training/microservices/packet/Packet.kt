/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.river.Status
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Packet internal constructor(map: Map<String, Any>) : RapidsPacket {
    companion object {
        internal const val COMMUNITY_KEY = "community"
        internal const val SYSTEM_COMMUNITY_VALUE = "system"

        internal const val PACKET_TYPE_KEY = "packet_type"
        internal const val SYSTEM_PACKET_TYPE_VALUE = "system_packet"

        internal const val SYSTEM_PURPOSE = "system_purpose"

        internal const val SYSTEM_READ_COUNT = "system_read_count"

        internal const val SYSTEM_BREADCRUMBS = "system_breadcrumbs"
    }

    constructor(vararg pairs: Pair<String, Any>) : this(pairs.toMap())

    private val map = map.toMutableMap()

    internal fun hasInvalidReadCount(maxReadCount: Int): Boolean =
        map.getOrPut(SYSTEM_READ_COUNT) { 0 }.let {
            map[SYSTEM_READ_COUNT] = (it as Int) + 1
            !(maxReadCount == 0 || map[SYSTEM_READ_COUNT] as Int <= maxReadCount)
        }

    internal fun isHeartBeat() = doesMeetRules(HeartBeat.rules, noProblemTracking)

    internal fun isSystem() = PACKET_TYPE_KEY in map.keys && map[PACKET_TYPE_KEY] == SYSTEM_PACKET_TYPE_VALUE

    internal fun doesMeetRules(rules: List<Validation>, problems: Status) = rules
        .map { it.isValid(this, problems) }
        .all { it }

    operator fun get(key: String) = map[key]

    operator fun set(key: String, value: Any) = map.set(key, value)

    fun isLacking(key: String) = map[key] == null || isEmpty(key)

    private fun isEmpty(key: String): Boolean {
        return map[key]?.let { value ->
            if (value is String && value.isEmpty()) return true
            return value is List<*> && value.size == 0
        } ?: true
    }

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(map)

    override fun toString() = map.toString()

    fun clone() = Packet(map.toMutableMap())

    fun dateTime(key: String) = LocalDateTime.parse(
        this[key] as String,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    )

    fun subPacket(key: String) = this[key] as Map<String, Any>

    fun beError(cause: String, source: String) =
        LogPacket.error(cause, source, *map.map{(key, value) -> key to value}.toTypedArray())

    private val noProblemTracking get() = Status("")
}