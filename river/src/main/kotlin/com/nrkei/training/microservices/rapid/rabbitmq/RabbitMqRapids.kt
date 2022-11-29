/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.rapid.rabbitmq

import com.nrkei.training.microservices.rapid.RapidsConnection
import com.nrkei.training.microservices.packet.RapidsPacket
import com.nrkei.training.microservices.river.River
import com.nrkei.training.microservices.river.River.PacketListener
import com.rabbitmq.client.*
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class RabbitMqRapids(ipAddress: String, port: String) : RapidsConnection, AutoCloseable {
    companion object {
        // See RabbitMQ pub/sub documentation: https://www.rabbitmq.com/tutorials/tutorial-three-python.html
        private const val RABBIT_MQ_PUB_SUB = "fanout"
        private const val EXCHANGE_NAME = "rapids"

        private const val DEFAULT_MAXIMUM_READ_COUNT = 9

        private fun PacketListener.toQueueName() = this.name
    }

    private val factory = ConnectionFactory().apply {
        setHost(ipAddress)
        setPort(port.toInt())
    }
    private lateinit var channel: Channel
    private lateinit var connection: Connection

    override fun register(listener: PacketListener) {
        river(listener) register listener
        Thread.sleep(100)
    }

    override fun register(listener: River.SystemListener) {
        river(listener) register listener
        Thread.sleep(100)
    }

    override fun publish(packet: RapidsPacket) {
        connectIfNecessary()
        try {
            channel.basicPublish(EXCHANGE_NAME, "", null, packet.toJsonString().toByteArray(charset("UTF-8")))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            throw RuntimeException("UnsupportedEncodingException on message extraction", e)
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException when sending a message", e)
        }
    }

    private fun river(listener: PacketListener) =
        River(this, listener.rules, DEFAULT_MAXIMUM_READ_COUNT).also { river ->
            listener.toQueueName().also { queueName ->
                configureQueue(queueName)
                println(" [*] Waiting for messages for ${listener.name}. To exit press CTRL+C")
                consumeMessages(consumer(channel, river), queueName)
            }
        }

    private fun consumeMessages(consumer: Consumer, queueName: String): String? {
        return try {
            channel.basicConsume(queueName, true, consumer)
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException while consuming messages", e)
        }
    }

    private fun consumer(channel: Channel, river: River): DefaultConsumer {
        val sendPort: RapidsConnection = this
        return object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String, envelope: Envelope,
                properties: AMQP.BasicProperties, body: ByteArray
            ) {
                String(body, Charset.forName("UTF-8")).also { message ->
//                    System.out.println(" [>] Received '" + message + "'")
                    river.message(sendPort, message)
                }
            }
        }
    }

    private fun configureQueue(queueName: String) {
        connectIfNecessary()
        declareQueue(queueName)
        bindQueueToExchange(queueName)
    }

    private fun declareQueue(queueName: String): AMQP.Queue.DeclareOk? {
        return try {
            // Configured for non-durable, auto-delete, and exclusive
            channel.queueDeclare(queueName, false, true, true, HashMap())
        } catch (e: IOException) {
            println(" [X] Error creating queue $queueName")
            e.printStackTrace()
            throw RuntimeException("IOException declaring Queue", e)
        }
    }

    private fun bindQueueToExchange(queueName: String) {
        try {
            channel.queueBind(queueName, EXCHANGE_NAME, "")
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException binding Queue to Exchange", e)
        }
    }

    private fun connectIfNecessary() {
        if (this::channel.isInitialized) return
        connection = newConnection()
        channel = newChannel()
        declareExchange()
    }

    private fun newConnection(): Connection {
        try {
            return factory.newConnection()
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException on creating Connection", e)
        }
    }

    private fun newChannel(): Channel {
        try {
            return connection.createChannel()
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException on creating Channel", e)
        }
    }

    private fun declareExchange() {
        try {
            // Configure for durable, auto-delete
            channel.exchangeDeclare(EXCHANGE_NAME, RABBIT_MQ_PUB_SUB, true, true, HashMap())
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException declaring Exchange", e)
        }
    }

    override fun close() {
        try {
            if (this::channel.isInitialized) channel.close()
            if (this::connection.isInitialized) connection.close()
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("IOException on close", e)
        }
    }
}