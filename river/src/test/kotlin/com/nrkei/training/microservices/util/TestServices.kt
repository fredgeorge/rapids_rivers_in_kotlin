/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * MIT License
 */

package com.nrkei.training.microservices.util

import com.nrkei.training.microservices.filter.KeyAbsenceValidation
import com.nrkei.training.microservices.filter.KeyExistenceValidation
import com.nrkei.training.microservices.filter.Validation
import com.nrkei.training.microservices.packet.Packet
import com.nrkei.training.microservices.packet.RapidsPacket
import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.Status

internal open class TestService(override val rules: List<Validation> = emptyList()) : River.PacketListener {
    internal val acceptedPackets = mutableListOf<Packet>()
    internal val rejectedPackets = mutableListOf<Packet>()
    internal val informationStatuses = mutableListOf<Status>()
    internal val problemStatuses = mutableListOf<Status>()

    override val name = "TestService [${this.hashCode()}]"

    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
        acceptedPackets.add(packet)
        informationStatuses.add(infoWarnings)
    }

    override fun rejectedPacket(connection: RapidsConnection, packet: Packet, problems: Status) {
        rejectedPackets.add(packet)
        problemStatuses.add(problems)
    }
}

internal class TestSystemService(rules: List<Validation> = emptyList()) : TestService(rules), River.SystemListener {
    internal val formatProblems = mutableListOf<Status>()
    internal val loopPackets = mutableListOf<RapidsPacket>()

    override val name = "TestSystemService [${this.hashCode()}]"

    override fun invalidFormat(connection: RapidsConnection, invalidString: String, problems: Status) {
        formatProblems.add(problems)
    }

    override fun loopDetected(connection: RapidsConnection, packet: Packet, problems: Status) {
        loopPackets.add(packet)
    }
}

internal class DeadService() : TestService() {
    override fun isStillAlive(connection: RapidsConnection) = false
}

internal class LinkedService(requiredKeys: List<String>, private val forbiddenKeys: List<String>) :
    TestService(requiredKeys.map { KeyExistenceValidation(it) } + forbiddenKeys.map { KeyAbsenceValidation(it) }) {

    override fun packet(connection: RapidsConnection, packet: Packet, infoWarnings: Status) {
        if (forbiddenKeys.isNotEmpty()) {
            packet.set(forbiddenKeys.first(), true)
            connection. publish(packet)
        }
        super.packet(connection, packet, infoWarnings)
    }
}
