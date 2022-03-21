/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.river

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nrkei.training.microservices.rapid.river.RapidsConnection.MessageListener

// Understands a themed flow of messages
class River(connection: RapidsConnection): MessageListener {
    init {
        connection.register(this)
    }

    override fun message(sendPort: RapidsConnection, message: String) {
        try {

        println(message)
        println(ObjectMapper().readValue<Map<String, Any>>(message))
        } catch (e: JsonParseException) {
            println(e.message)
        }
    }
}