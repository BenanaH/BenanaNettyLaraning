package com.benana.nettyLearning.nio.simple;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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

        //将Channel设置为非阻塞类型
        //在这种模式下，ServerSocketChannel.accept()方法将立即返回，如果没有连接请求，则返回null。
        //这使得应用程序可以同时处理多个连接请求，因为它不需要等待每个请求的到来。
        //但是由于非阻塞模式的特点，它需要应用程序轮询连接请求的到来，这可能会导致CPU资源的浪费。
        serverSocketChannel.configureBlocking(false);

        //将ServerSocketChannel注册到Selector选择器中
        //SelectionKey.OP_ACCEPT让选择器监听ServerSocketChannel的连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

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
                //当OP_ACCEPT事件正在发生的时候，调用ServerSocketChannel的accept就必不可能获取到null
                //这里代表的是有一个客户端和服务端建立连接了，所以要注册到选择器里面
                if (selectionKey.isAcceptable()) {
                    //这个是连接过来的那个客户端生成的SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    //如果ServerSocketChannel是非阻塞的，那么客户端也需要非阻塞
                    socketChannel.configureBlocking(false);

                    //也注册到Selector中，监听读事件。也就是客户端往Channel写了东西。
                    //这里为什么要先放一个Buffer？如果SocketChannel在发生SelectionKey.OP_READ就创建一个新的Buffer，会非常消耗内存资源
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                //这里代表的是选择器中有监听的管道需要读取数据了，一般是客户端发送了数据到管道中
                if (selectionKey.isReadable()) {
                    //向下转型，不会出现异常
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                    //这里是获取到Channel.register()第三个参数放的东西，我们在上面放了一个ByteBuffer
                    ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                    int readBytes = socketChannel.read(byteBuffer);

                    //当客户端关闭连接时，服务器端的socketChannel也会收到一个EOF（End of File）信号。
                    //这个信号实际上就是一个读事件，告诉服务器可以读取这个channel了，只不过读取的长度是0。
                    //此时，read()方法会返回-1，告诉服务器端已经读到了流的末尾。
                    //因此，此时isReadable()方法会返回true，通知服务器可以读取客户端关闭连接的消息，然后服务器端会相应地处理这个事件。
                    if (readBytes > 0) {
                        byteBuffer.flip();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes);
                        String message = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println("收到客户端（" + socketChannel.hashCode() + "）发送的信息：" + message);
                        byteBuffer.clear();
                    }
                    //小于0则代表读完了，通道关闭了！
                    else if (readBytes < 0) {
                        selectionKey.cancel();
                        socketChannel.close();
                    }
                }
                //在最后需要remove掉处理完的SelectionKey，防止重复处理
                iterator.remove();
            }
        }
    }
}
