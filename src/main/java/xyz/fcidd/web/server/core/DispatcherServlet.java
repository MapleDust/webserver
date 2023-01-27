package xyz.fcidd.web.server.core;

import lombok.extern.log4j.Log4j2;
import xyz.fcidd.web.server.http.HttpServletRequest;
import xyz.fcidd.web.server.http.HttpServletResponse;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

/**
 * 用于处理请求
 */
@Log4j2
public class DispatcherServlet {
    private static File root;
    private static File staticDir;

    static {
        try {
            root = new File(DispatcherServlet.class.getClassLoader().getResource(".").toURI());
            staticDir = new File(root, "static");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();

        log.debug("====================>" + path);
        /*
            path = "/myweb/login"
         */
        //首先判断该请求是否为请求一个业务
        try {
            HandlerMapping.MethodMapping mm = HandlerMapping.getMethod(path);
            if (mm != null) {
                Object controller = mm.getController();
                Method method = mm.getMethod();
                method.invoke(controller, ParameterUtil.getParameterValues(method, request, response));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        File file = new File(staticDir, path);
        log.debug("资源是否存在:" + file.exists());
        if (file.isFile()) {//当file表示的文件真实存在且是一个文件时返回true
            response.setContentFile(file);

        } else {//要么file表示的是一个目录，要么不存在
            response.setStatusCode(404);
            response.setStatusReason("NotFound");
            file = new File(staticDir, "root/404.html");
            response.setContentFile(file);
        }


        //测试添加一个额外的响应头
        response.addHeader("Server", "WebServer");

    }
}






