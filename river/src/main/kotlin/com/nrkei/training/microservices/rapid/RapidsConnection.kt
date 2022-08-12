/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.rapid

import com.nrkei.training.microservices.packet.RapidsPacket
import com.nrkei.training.microservices.river.River

// Understands access to an undifferentiated stream of messages
interface RapidsConnection {

    infix fun register(listener: River.PacketListener) // For services

    infix fun register(listener: River.SystemListener) // For system services (monitoring, system failures)

    infix fun publish(packet: RapidsPacket)

    interface MessageListener {
        fun message(sendPort: RapidsConnection, message: String): Any?
    }
}