/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.rental;

import com.nrkei.training.microservices.packet.Packet;
import com.nrkei.training.microservices.rapid.RapidsConnection;
import com.nrkei.training.microservices.rapid.rabbitmq.RabbitMqRapids;

import java.util.HashMap;

class JNeed {
    public static void main(String[] args) {
        String host = args[0];
        String port = args[1];
        publish(new RabbitMqRapids(host, port));
    }

    private static void publish(RapidsConnection connection) {
        try {
            HashMap<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("community", "offer_engine_family");
            jsonMap.put("need", "car_rental_offer");
            var packet = new Packet(jsonMap);
            while (true) {
                System.out.printf(String.format(" [<] %s\n", packet));
                connection.publish(packet);
                Thread.sleep(5000);
            }
        } catch (
                Exception e) {
            throw new RuntimeException("Could not publish message:", e);
        }
    }
}

