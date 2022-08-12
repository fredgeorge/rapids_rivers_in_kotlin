/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.packet

import com.fasterxml.jackson.databind.ObjectMapper
import com.nrkei.training.microservices.packet.Packet.Companion.PACKET_TYPE
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_PACKET_TYPE
import com.nrkei.training.microservices.packet.Packet.Companion.SYSTEM_PURPOSE

// Understands something interesting that occurred
class LogPacket private constructor() : RapidsPacket {
    companion object {
        private const val LOG_PURPOSE = "logging"

        private const val LOG_SEVERITY = "log_severity"
        private const val INFORMATIONAL_SEVERITY = "informational"
        private const val WARNING_SEVERITY = "warning"
        private const val ERROR_SEVERITY = "error"

        private const val LOG_SOURCE = "log_source"

        private const val LOG_CAUSE = "log_cause"
        internal const val SERVICE_NOT_RESPONDING = "service_not_responding"
        internal const val INVALID_JSON = "invalid_json"

        internal const val LOG_MESSAGE = "log_message"

        fun error(cause: String, source: String, vararg keyValues: Pair<String, Any>) =
            logPacket(ERROR_SEVERITY, cause, source, keyValues.toList())

        fun warning(cause: String, source: String, vararg keyValues: Pair<String, Any>) =
            logPacket(WARNING_SEVERITY, cause, source, keyValues.toList())

        fun information(cause: String, source: String, vararg keyValues: Pair<String, Any>) =
            logPacket(INFORMATIONAL_SEVERITY, cause, source, keyValues.toList())

        private fun logPacket(severity: String, cause: String, source: String, keyValues: List<Pair<String, Any>>) =
            LogPacket().apply {
                map[PACKET_TYPE] = SYSTEM_PACKET_TYPE
                map[SYSTEM_PURPOSE] = LOG_PURPOSE
                map[LOG_SEVERITY] = severity
                map[LOG_CAUSE] = cause
                map[LOG_SOURCE] = source
                keyValues.forEach { map[it.first] = it.second }
            }
    }

    private val map = mutableMapOf<String, Any>()

    fun message(description: String) {
        map[LOG_MESSAGE] = description
    }

    override fun toJsonString(): String = ObjectMapper().writeValueAsString(map)

    override fun toString() = toJsonString()
}