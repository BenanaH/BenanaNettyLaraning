package com.benana.nettyLearning.netty.mqtt.client.sample;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;

import java.nio.charset.StandardCharsets;

/**
 * @author Benana
 * @date 2023/5/11 14:31
 */
public class MyMqttHandler extends SimpleChannelInboundHandler<MqttMessage> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        MqttMessageType mqttMessageType = msg.fixedHeader().messageType();
        System.out.println("MyMqttHandler:channelRead0......");
    }

    /**
     * 当连接激活
     * 我们需要给MQTT服务器发送一些连接消息完成连接
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //1.构建MQTT固定报头，每个MQTT命令消息的消息头都包含一个固定的报头。
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                //7 6 5 4 标志位，MQTT控制报文的类型
                MqttMessageType.CONNECT,
                //3 标志位，DUP标志（如果DUP标志被设置为0，表示这是客户端或服务端第一次请求发送这个PUBLISH报文。如果DUP标志被设置为1，表示这可能是一个早前报文请求的重发。）
                false,
                //2 1 标志位，QoS等级
                MqttQoS.AT_MOST_ONCE,
                //0 标志位，保持
                false,
                //剩余长度
                0);

        //2.构建MQTT连接可变报头
        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(
                //协议名称
                MqttVersion.MQTT_3_1.protocolName(),
                //协议层
                MqttVersion.MQTT_3_1.protocolLevel(),
                //是否需要账号
                true,
                //是否需要密码
                true,
                //Will Retain
                false,
                //Will QOS
                0,
                //Has Will?
                false,
                //Clean Session
                true,
                //心跳超时时间，超过没收发心跳视为超时
                10
        );

        //3.构建连接有效载荷
        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(
                //客户端ID
                "cptest",
                //topic
                null,
                //消息内容
                "".getBytes(StandardCharsets.UTF_8),
                //连接账号
                "admin",
                //连接密码
                "xiaozhihui0123".getBytes(StandardCharsets.UTF_8)
        );

        //4.将构建好的各种连接信息刷给服务端，建立连接
        Channel channel = ctx.channel();
        ctx.channel().writeAndFlush(new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("MyMqttHandler:exceptionCaught......");
        cause.printStackTrace();
    }
}
