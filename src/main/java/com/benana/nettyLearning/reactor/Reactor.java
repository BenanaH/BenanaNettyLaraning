package com.benana.nettyLearning.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Reactor模式
 * Reactor负责监听IO事件，当有事件发生时，Reactor会将事件分发给对应的Handler进行处理。
 *
 * @author Benana
 * @date 2023/3/28 21:33
 */
public class Reactor implements Runnable {

    private final Selector selector;

    private final ServerSocketChannel serverSocketChannel;

    public Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor());
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey selectionKey = it.next();
                    dispatch(selectionKey);
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }

    /**
     * 当有新的连接请求时，Acceptor会将SocketChannel注册到Selector中，然后创建一个新的Handler来处理该连接的读写操作。
     */
    class Acceptor implements Runnable {
        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    new Handler(selector, socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handler是具体的业务处理逻辑，负责读取和处理数据，然后将处理结果返回给客户端。
     */
    static class Handler implements Runnable {

        private final SocketChannel socketChannel;

        private final SelectionKey selectionKey;

        private final ByteBuffer input = ByteBuffer.allocate(1024);

        private final ByteBuffer output = ByteBuffer.allocate(1024);

        public Handler(Selector selector, SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            selectionKey = socketChannel.register(selector, 0);
            selectionKey.attach(this);
            selectionKey.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        @Override
        public void run() {
            try {
                if (selectionKey.isReadable()) {
                    read();
                } else if (selectionKey.isWritable()) {
                    write();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socketChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void read() throws IOException {
            socketChannel.read(input);
            // 处理读取的数据
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }

        private void write() throws IOException {
            output.clear();
            // 处理写入的数据
            socketChannel.write(output);
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }
}
