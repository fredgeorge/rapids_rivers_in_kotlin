/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.packet

interface Packet {
    companion object {
        internal const val PACKET_TYPE = "packet_type"
        internal const val SYSTEM_PACKET_TYPE = "system_packet"
        internal const val SYSTEM_PURPOSE = "system_purpose"
        internal const val HEART_BEAT_PURPOSE = "heart_beat"
        internal const val HEART_BEAT_RESPONDER = "heart_beat_responder"
        internal const val READ_COUNT = "system_read_count"
    }
}