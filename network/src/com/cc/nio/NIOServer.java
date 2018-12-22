package com.cc.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer  implements Runnable{

    private final int BUFFER_SIZE = 1024; // 缓冲区大小
    private final int PORT = 8888;        // 监听的端口
    private Selector selector;            // 多路复用器，NIO编程的基础，负责管理通道Channel
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);  // 缓冲区Buffer

    private void startServer(){
        try {
            // 1.开启多路复用器
            selector = Selector.open();
            // 2.打开服务器通道(网络读写通道)
            ServerSocketChannel channel = ServerSocketChannel.open();
            // 3.设置服务器通道为非阻塞模式，true为阻塞，false为非阻塞
            channel.configureBlocking(false);
            // 4.绑定端口
            channel.socket().bind(new InetSocketAddress(PORT));
            // 5.把通道注册到多路复用器上，并监听阻塞事件
            /**
             * SelectionKey.OP_READ   : 表示关注读数据就绪事件
             * SelectionKey.OP_WRITE  : 表示关注写数据就绪事件
             * SelectionKey.OP_CONNECT: 表示关注socket channel的连接完成事件
             * SelectionKey.OP_ACCEPT : 表示关注server-socket channel的accept事件
             */
            channel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server start >>>>>>>>> port :" + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1.多路复用器监听阻塞
                selector.select();
                // 2.多路复用器已经选择的结果集
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                // 3.不停的轮询
                while (selectionKeys.hasNext()) {
                    // 4.获取一个选中的key
                    SelectionKey key = selectionKeys.next();
                    // 5.获取后便将其从容器中移除
                    selectionKeys.remove();
                    // 6.只获取有效的key
                    if (!key.isValid()){
                        continue;
                    }
                    // 阻塞状态处理
                    if (key.isAcceptable()){
                        accept(key);
                    }
                    // 可读状态处理
                    if (key.isReadable()){
                        read(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // 设置阻塞，等待Client请求。在传统IO编程中，用的是ServerSocket和Socket。在NIO中采用的ServerSocketChannel和SocketChannel
    private void accept(SelectionKey selectionKey) {
        try {
            // 1.获取通道服务
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            // 2.执行阻塞方法
            SocketChannel socketChannel = serverSocketChannel.accept();
            // 3.设置服务器通道为非阻塞模式，true为阻塞，false为非阻塞
            socketChannel.configureBlocking(false);
            // 4.把通道注册到多路复用器上，并设置读取标识
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void read(SelectionKey selectionKey) {
        try {
            // 1.清空缓冲区数据
            readBuffer.clear();
            // 2.获取在多路复用器上注册的通道
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            // 3.读取数据，返回
            int count = socketChannel.read(readBuffer);
            // 4.返回内容为-1 表示没有数据
            if (-1 == count) {
                selectionKey.channel().close();
                selectionKey.cancel();
                return ;
            }
            // 5.有数据则在读取数据前进行复位操作
            readBuffer.flip();
            // 6.根据缓冲区大小创建一个相应大小的bytes数组，用来获取值
            byte[] bytes = new byte[readBuffer.remaining()];
            // 7.接收缓冲区数据
            readBuffer.get(bytes);
            // 8.打印获取到的数据
            System.out.println("NIO Server : " + new String(bytes)); // 不能用bytes.toString()
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        NIOServer server = new NIOServer();
        server.startServer();
        new Thread(server).start();
    }
}
