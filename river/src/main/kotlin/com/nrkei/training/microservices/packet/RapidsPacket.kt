package com.nrkei.training.microservices.packet

// Understands a message targeting an event bus
interface RapidsPacket {
    fun toJsonString(): String
}