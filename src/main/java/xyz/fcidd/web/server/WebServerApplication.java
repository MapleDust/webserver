package xyz.fcidd.web.server;

import lombok.extern.log4j.Log4j2;
import xyz.fcidd.web.server.core.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer主类
 * WebServer是一个Web容器，实现了Tomcat的基础功能。
 * 通过本次项目我们的目标是:
 * 1:理解Tomcat底层工作原理
 * 2:理解HTTP协议的规定
 * 3:理解SpringBoot
 */
@Log4j2
public class WebServerApplication {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    /**
     * 初始化WebServer
     */
    public WebServerApplication() {
        try {
            log.info("正在启动服务端...");
            /*
                当端口被其他程序占用时，这里会抛出异常:
                java.net.BindException:address already in use:JVM
                解决:
                1:杀死该java进程(推荐)
                2:重启电脑(不推荐)
                3:更换端口(通常重启后还不行，说明被其他程序占用该端口了)
             */
            serverSocket = new ServerSocket(8080);
            threadPool = Executors.newFixedThreadPool(50);
            log.info("服务端启动完毕!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务端开始工作的方法
     */
    public void start() {
        try {
            while (true) {
                log.info("等待客户端连接...");
                Socket socket = serverSocket.accept();
                log.info("一个客户端连接了!");
                //启动一个线程处理该客户端交互
                ClientHandler clientHandler = new ClientHandler(socket);
                threadPool.execute(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServerApplication webServerApplication = new WebServerApplication();
        webServerApplication.start();
    }
}
