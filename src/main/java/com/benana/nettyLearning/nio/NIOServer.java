package com.benana.nettyLearning.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO Server
 *
 * @author Benana
 * @date 2023/3/19 20:25
 */
public class NIOServer {

    public static void main(String[] args) throws Exception {
        //Selector选择器
        //用来管理所有Channel的监听
        Selector selector = Selector.open();

        //ServerSocketChannel
        //打开了一个Channel，并且绑定了本地的一个端口，进行监听
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(123);
        serverSocketChannel.bind(socketAddress);

        //将ServerSocketChannel注册到Selector选择器中
        //SelectionKey.OP_ACCEPT让选择器监听ServerSocketChannel的连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //将Channel设置为非阻塞类型
        //在这种模式下，ServerSocketChannel.accept()方法将立即返回，如果没有连接请求，则返回null。
        //这使得应用程序可以同时处理多个连接请求，因为它不需要等待每个请求的到来。
        //但是由于非阻塞模式的特点，它需要应用程序轮询连接请求的到来，这可能会导致CPU资源的浪费。
        serverSocketChannel.configureBlocking(false);

        while (true) {
            if (selector.select(2000) == 0) {
                System.out.println("正在监听ServerSocketChannel的连接事件，暂无连接......");
                continue;
            }
            //发生事件的所有Channel，当然目前只有一个ServerSocketChannel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            //遍历所有发生事件的Channel，做下一步处理
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //isAcceptable()代表的是SelectionKey.OP_ACCEPT事件发生
                if (selectionKey.isAcceptable()) {
                }

                //在最后需要remove掉处理完的SelectionKey，防止重复处理
                iterator.remove();
            }
        }
    }
}
