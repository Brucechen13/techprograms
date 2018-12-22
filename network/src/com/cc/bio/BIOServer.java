package com.cc.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BIOServer {

    private int PORT = 8888;

    public void startServe(){
        ServerSocket serverSocket = null;
        ThreadPoolExecutor poolExecutor = null;
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("BIO SERVER START");
            /*--------------通过线程池处理缓解高并发给程序带来的压力（伪异步IO编程）----------------*/
            poolExecutor = new ThreadPoolExecutor(10, 100, 1000,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(50));
            while (true) {
                Socket socket = serverSocket.accept();
                poolExecutor.execute(new BIOServerHandle(socket));
            }
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if (null != serverSocket) {
                    serverSocket.close();
                    serverSocket = null;
                    System.out.println("BIO Server 服务器关闭了！！！！");
                }
                poolExecutor.shutdown();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        BIOServer server = new BIOServer();
        server.startServe();
    }
}
