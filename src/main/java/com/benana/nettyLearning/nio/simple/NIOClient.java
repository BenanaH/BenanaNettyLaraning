package com.benana.nettyLearning.nio.simple;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * NIO Client
 *
 * @author Benana
 * @date 2023/3/19 20:58
 */
public class NIOClient {

    public static void main(String[] args) throws Exception {
        //这里直接打开一个SocketChannel，去连接ServerSocketChannel
        SocketChannel socketChannel = SocketChannel.open();
        InetSocketAddress socketAddress = new InetSocketAddress(123);

        //开始连接......
        socketChannel.connect(socketAddress);
        while (!socketChannel.finishConnect()) {
            System.out.println("SocketChannel正在连接中......");
        }

        //连接上了，开始写入数据到我们的SocketChannel，远端服务端从SocketChannel读取数据
        //ServerSocketChannel的读取行为与我们的预期不同，它会一直读取到"Hello!"而不是只读取到一个"Hello!"
        //由于"Hello!"字符串的UTF-8编码中包含多个字节，因此ServerSocketChannel会将其视为一个字节流，并将所有的字节读取出来
        //socketChannel.write(ByteBuffer.wrap("Hello!".getBytes()));
        byte[] bytes = "Hello!".getBytes(StandardCharsets.UTF_8);
        socketChannel.write(ByteBuffer.wrap(bytes));
        System.in.read();
    }
}
