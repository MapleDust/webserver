package xyz.fcidd.web.server.controller;

import lombok.extern.log4j.Log4j2;
import xyz.fcidd.web.server.annotations.Controller;
import xyz.fcidd.web.server.annotations.RequestMapping;
import xyz.fcidd.web.server.entity.User;
import xyz.fcidd.web.server.http.HttpServletRequest;
import xyz.fcidd.web.server.http.HttpServletResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理与用户相关的业务操作
 */
@Log4j2
@Controller
public class UserController {
    //表示users目录
    private static File userDir;

    static {
        userDir = new File("users");
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
    }

    /**
     * 生成展示所有用户信息的动态页面
     *
     * @param request
     * @param response
     */
    @RequestMapping("/myweb/showAllUser")
    public void showAllUser(HttpServletRequest request, HttpServletResponse response) {
        /*
            1:通过读取users目录下所有的obj文件，进行反序列化
              得到所有的注册用户信息(若干的User对象)将他们存入一个List集合备用
            2:拼接一个HTML页面，并将所有用户信息拼接到table表格中用于展示给用户
            3:将拼接好的HTML代码作为正文给浏览器响应回去
         */
        //1
        List<User> userList = new ArrayList<>();
        /*
            1.1:通过File的listFiles方法，从userDir目录下获取所有的obj文件
                要使用文件过滤器
            1.2:遍历每一个obj文件，并利用对象流与文件流的连接，将obj文件中的
                User对象反序列化出来
            1.3:将反序列化的User对象存入userList集合中
         */
        //1.1
        File[] subs = userDir.listFiles(f -> f.getName().endsWith(".obj"));
        //1.2
        for (File userFile : subs) {
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                User user = (User) ois.readObject();
                //1.3
                userList.add(user);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        log.debug(userList);

        //2将数据拼接到html生成页面

        response.setContentType("text/html");

        PrintWriter pw = response.getWriter();
        pw.println("<!DOCTYPE html>");
        pw.println("<html lang=\"en\">");
        pw.println("<head>");
        pw.println("<meta charset=\"UTF-8\">");
        pw.println("<title>用户列表</title>");
        pw.println("</head>");
        pw.println("<body>");
        pw.println("<center>");
        pw.println("<h1>用户列表</h1>");
        pw.println("<table border=\"1\">");
        pw.println("<tr>");
        pw.println("<td>用户名</td>");
        pw.println("<td>密码</td>");
        pw.println("<td>昵称</td>");
        pw.println("<td>年龄</td>");
        pw.println("</tr>");
        for (User user : userList) {
            pw.println("<tr>");
            pw.println("<td>" + user.getUsername() + "</td>");
            pw.println("<td>" + user.getPassword() + "</td>");
            pw.println("<td>" + user.getNickname() + "</td>");
            pw.println("<td>" + user.getAge() + "</td>");
            pw.println("</tr>");
        }
        pw.println("</table>");
        pw.println("</center>");
        pw.println("</body>");
        pw.println("</html>");
        log.debug("页面已生成");
    }

    @RequestMapping("/myweb/login")
    public void login(HttpServletRequest request, HttpServletResponse response) {
        //1获取用户输入的登录信息
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || password == null) {
            response.sendRedirect("/myweb/login_info_error.html");
            return;
        }

        //2
        File userFile = new File(userDir, username + ".obj");
        if (userFile.exists()) {//用户名输入正确
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                //读取该注册用户信息
                User user = (User) ois.readObject();
                if (user.getPassword().equals(password)) {//密码正确
                    //登录成功
                    response.sendRedirect("/myweb/login_success.html");
                    return;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //如果程序走到这里,情况1:用户名没有输入正确，文件不存在
        //              情况2:用户名对了，但是密码不对
        response.sendRedirect("/myweb/login_fail.html");


    }

    @RequestMapping("/myweb/reg")
    public void reg(HttpServletRequest request, HttpServletResponse response) {
        log.info("开始处理注册");
        /*
            1:获取用户在注册页面上输入的注册信息
            2:将注册信息保存起来
            3:给用户回馈一个注册结果页面
         */
        //1
        //调用getParameter时传入的参数应当与页面上表单中输入框的名字一致(输入框name属性的值)
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String ageStr = request.getParameter("age");
        log.info(username + "," + password + "," + nickname + "," + ageStr);
        /*
            添加一个判断，要求:如果上述四个信息有null值或者年龄不是数字
            立刻给用户响应一个错误提示页面:reg_info_error.html
            该页面居中显示一行字:注册信息输入有误，请重新注册。

            正则表达式:[0-9]+
         */
        if (username == null || password == null || nickname == null || ageStr == null ||
                !ageStr.matches("[0-9]+")) {
            /*
               http://localhost:8088/myweb/reg?username=fancq&password=123456&nickname=fcq&age=22

               给浏览器发送的响应内容:
               HTTP/1.1 302 Moved Temporarily(CRLF)
               Location: /myweb/reg_info_error.html(CRLF)(CRLF)

               浏览器接收到该响应后，根据状态代码302得知服务器希望他自动再发起一次请求
               来请求指定的位置
               指定的位置是哪里?浏览器根据响应头Location得知位置
               此时得到的Location位置为:/myweb/reg_info_error.html

               由于浏览器之前的请求(发起注册):
               http://localhost:8088/myweb/reg?username=fancq&password=123456&nickname=fcq&age=22
               因此浏览器理解本次重新请求的路径就是:
               http://localhost:8088/myweb/reg_info_error.html
             */
            response.sendRedirect("/myweb/reg_info_error.html");
            return;
        }

        int age = Integer.parseInt(ageStr);
        log.debug(username + "," + password + "," + nickname + "," + age);

        //2
        User user = new User(username, password, nickname, age);
        File userFile = new File(userDir, username + ".obj");
        /*
            判断重名，如果是已注册用户，则响应页面:have_user.html告知
            该页面居中显示一行字:该用户已存在，请重新注册。
         */
        if (userFile.exists()) {//该文件存在则说明是重复用户
            response.sendRedirect("/myweb/have_user.html");
            return;
        }


        try (
                FileOutputStream fos = new FileOutputStream(userFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            //这里序列化的是注册用户信息，因此是user对象！！！！！！！！！！！！
            oos.writeObject(user);

            //3
            response.sendRedirect("/myweb/reg_success.html");
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }


    }

}
