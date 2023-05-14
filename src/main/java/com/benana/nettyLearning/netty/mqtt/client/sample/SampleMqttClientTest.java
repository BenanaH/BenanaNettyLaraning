package com.benana.nettyLearning.netty.mqtt.client.sample;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.*;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Scanner;

/**
 * @author Benana
 * @date 2023/5/11 14:27
 */
public class SampleMqttClientTest {

    public static void main(String[] args) {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new MyMqttInitializer());
            bootstrap.remoteAddress("127.0.0.1", 1883);
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("连接成功!");
                        return;
                    }
                    System.out.println("连接失败.....");
                }
            });
            Channel channel = channelFuture.channel();

            //订阅START
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttTopicSubscription subscription = new MqttTopicSubscription("test", MqttQoS.AT_MOST_ONCE);
            MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(1);
            MqttSubscribePayload payload = new MqttSubscribePayload(Collections.singletonList(subscription));
            MqttSubscribeMessage message = new MqttSubscribeMessage(fixedHeader, variableHeader, payload);
            channel.writeAndFlush(message);
            //订阅END

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String next = scanner.next();
                MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_MOST_ONCE, false, 0);
                MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader("test", 1);
                MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, Unpooled.copiedBuffer(next, StandardCharsets.UTF_8));
                channel.writeAndFlush(mqttPublishMessage);
            }
            ChannelFuture closeFuture = channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

}
