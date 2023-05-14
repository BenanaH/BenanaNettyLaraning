package com.benana.nettyLearning.netty.mqtt.client.sample;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author Benana
 * @date 2023/5/11 14:29
 */
public class MyMqttInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("mqttDecoder", new MqttDecoder());
        pipeline.addLast("mqttEncoder", MqttEncoder.INSTANCE);
        pipeline.addLast("idleStateHandler", new IdleStateHandler(10, 10, 10));
        pipeline.addLast("mqttPingHandler", new MqttPingHandler(60));
        pipeline.addLast("myMqttHandler", new MyMqttHandler());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("MyMqttInitializer:exceptionCaught......");
        cause.printStackTrace();
    }

}
