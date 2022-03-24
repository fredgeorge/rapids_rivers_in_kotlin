/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PACKET_TYPE
import com.nrkei.training.microservices.rapid.packet.Packet.Companion.SYSTEM_PURPOSE
import com.nrkei.training.microservices.rapid.river.RapidsPacket

// Understands something interesting that occurred
class LogPacket private constructor() : RapidsPacket {
    companion object {
        internal const val LOG_PURPOSE = "logging"

        internal const val LOG_SEVERITY = "log_severity"
        internal const val INFORMATIONAL_SEVERITY = "informational"
        internal const val WARNING_SEVERITY = "warning"
        internal const val ERROR_SEVERITY = "error"

        internal const val LOG_SOURCE = "log_source"

        internal const val LOG_CAUSE = "log_cause"
        internal const val SERVICE_NOT_RESPONDING = "service_not_responding"
        const val INVALID_JSON = "invalid_json"

        internal const val LOG_DETAIL = "log_detail"

        fun error(cause: String, source: String) = LogPacket().apply {
            map[PACKET_TYPE] = SYSTEM_PACKET_TYPE
            map[SYSTEM_PURPOSE] = LOG_PURPOSE
            map[LOG_SEVERITY] = ERROR_SEVERITY
            map[LOG_CAUSE] = cause
            map[LOG_SOURCE] = source
        }
    }

    private val map = mutableMapOf<String, Any>()

    fun details(addendum: String) { map[LOG_DETAIL] = addendum }

    override fun toJsonString() = ObjectMapper().writeValueAsString(map)
}