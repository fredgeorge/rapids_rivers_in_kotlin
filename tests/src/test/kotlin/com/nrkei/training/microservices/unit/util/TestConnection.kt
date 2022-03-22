/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.unit.util

import com.nrkei.training.microservices.rapid.river.RapidsConnection

// Simulates an event bus
internal class TestConnection : RapidsConnection {
    private val rivers = mutableListOf<RapidsConnection.MessageListener>()
    val sentMessages = mutableListOf<String>()

    override fun register(listener: RapidsConnection.MessageListener) {
        rivers.add(listener)
    }

    override fun publish(message: String) {
        sentMessages.add(message)
    }

    fun injectMessage(content: String) = rivers.forEach { it.message(this, content) }
}