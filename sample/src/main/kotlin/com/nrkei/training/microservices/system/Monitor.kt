/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.system

import com.nrkei.training.microservices.filter.rules
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids
import com.nrkei.training.microservices.river.PacketProblems
import com.nrkei.training.microservices.river.River.SystemListener

// Understands the messages on an event bus
class Monitor : SystemListener {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RabbitMqRapids(ipAddress = args[0], port = args[1]).register(Monitor())
        }
    }

    // No rules for a Monitor. Options for filtering shown for documentation purpose
    override val rules = rules {
//        require key "a_key"                     // Reject packet unless it has key of a_key (not null, not empty String, not empty Array)
//        require key "a_key" value "a_value"     // Reject packet unless it has key:value pair a_key:a_value
//        forbid key "b_key"                      // Reject packet if it has key of b_key (unless b-key is null, empty String, or empty Array)
    }

    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: PacketProblems) {
        println(String.format(" [*] %s", infoWarnings))
    }

    override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
        println(String.format(" [x] %s", problems))
    }

    override fun invalidFormat(connection: RapidsConnection, invalidString: String, problems: PacketProblems) {
        println(String.format(" [x] %s", problems))
    }

    override fun loopDetected(connection: RapidsConnection, packet: Packet, problems: PacketProblems) {
        println(String.format(" [x] %s", problems))
    }
}