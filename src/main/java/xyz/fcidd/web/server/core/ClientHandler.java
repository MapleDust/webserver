package xyz.fcidd.web.server.core;

import xyz.fcidd.web.server.http.EmptyRequestException;
import xyz.fcidd.web.server.http.HttpServletRequest;
import xyz.fcidd.web.server.http.HttpServletResponse;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

/**
 * 该线程任务是负责与一个客户端进行一次HTTP交互
 * 浏览器与服务端交互组从一问一答的原则。因此服务端处理一次HTTP交互，步骤如下:
 * 1:解析请求(接受浏览器发送过来的请求内容)
 * 2:处理请求(根据浏览器发送的请求理解其意图并进行对应的处理)
 * 3:发送响应(将处理结果发送给浏览器)
 */
public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            //1解析请求
            HttpServletRequest request = new HttpServletRequest(socket);
            HttpServletResponse response = new HttpServletResponse(socket);

            //2处理请求
            DispatcherServlet servlet = new DispatcherServlet();
            servlet.service(request, response);

            //3发送响应
            response.response();
        } catch (IOException | EmptyRequestException e) {
            e.printStackTrace();
        } finally {
            try {
                //按照HTTP协议要求，一次交互后要断开连接
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
