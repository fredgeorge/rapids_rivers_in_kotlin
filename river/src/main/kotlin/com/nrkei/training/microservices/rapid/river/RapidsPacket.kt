package com.nrkei.training.microservices.rapid.river

// Understands a message targeting an event bus
interface RapidsPacket {
    fun toJsonString(): String
}