package com.nrkei.training.microservices.rapid.filter

import com.nrkei.training.microservices.rapid.packet.Packet

interface Validation {
    fun isValid(packet: Packet, )
}