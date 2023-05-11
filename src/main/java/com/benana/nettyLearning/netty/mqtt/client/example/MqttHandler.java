package com.benana.nettyLearning.netty.mqtt.client.example;

import io.netty.buffer.ByteBuf;

public interface MqttHandler {

    void onMessage(String topic, ByteBuf payload);
}
