package xyz.fcidd.web.server.http;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 响应对象
 * 该类的每一个实例用于表示发送给客户端(浏览器)的一个HTTP响应内容
 * 每个响应由三部分构成:
 * 状态行，响应头，响应正文
 */
public class HttpServletResponse {
    private Socket socket;

    //状态行相关信息
    private int statusCode = 200;//状态代码，默认为200
    private String statusReason = "OK";//状态描述，默认为OK

    //响应头相关信息
    //key:响应头名字   value:响应头的值
    private Map<String, String> headers = new HashMap<>();


    //响应正文相关信息
    private File contentFile;//响应正文对应的实体文件
    private byte[] contentData;//以一组字节作为正文内容(通常动态数据使用)

    private ByteArrayOutputStream baos;


    public HttpServletResponse(Socket socket) {
        this.socket = socket;
    }

    /**
     * 将当前响应对象内容按照标准的HTTP响应格式发送给浏览器
     */
    public void response() throws IOException {
        //发送前的准备工作
        sendBefore();
        //3.1发送状态行
        sendStatusLine();
        //3.2发送响应头
        sendHeaders();
        //3.3将文件的所有字节作为正文内容发送给浏览器
        sendContent();
    }

    //响应发送前的准备工作
    private void sendBefore() {
        if (baos != null) {//如果baos不为null，则说明用过这个流写出过动态数据
            contentData = baos.toByteArray();//将内部的字节数组获取(动态数据)
            addHeader("Content-Length", contentData.length + "");
        }
    }


    //发送状态行
    private void sendStatusLine() throws IOException {
        println("HTTP/1.1" + " " + statusCode + " " + statusReason);
    }

    //发送响应头
    private void sendHeaders() throws IOException {
        /*
            headers
            KEY                 VALUE
            Content-Type        text/html
            Content-Length      2323
            ...                  ...
         */
        //遍历headers将所有的响应头发送给浏览器
        Set<Entry<String, String>> entrySet = headers.entrySet();
        for (Entry<String, String> e : entrySet) {
            String key = e.getKey();
            String value = e.getValue();
            //println("Content-Type: text/html");
            println(key + ": " + value);
        }

        //单独发送回车+换行表示响应头部分发送完毕了
        println("");
    }

    //发送响应正文
    private void sendContent() throws IOException {
        if (contentData != null) {//是否有动态数据作为正文
            OutputStream out = socket.getOutputStream();
            out.write(contentData);

        } else if (contentFile != null) {
            //try()意味着编译后有finally用于遍历流。所以这不违背try不能单独定义的原则
            try (
                    FileInputStream fis = new FileInputStream(contentFile);
            ) {
                OutputStream out = socket.getOutputStream();
                byte[] buf = new byte[1024 * 10];//10kb
                int len;//记录每次实际读取到的字节数
                while ((len = fis.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            }
        }
    }


    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] data = line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);//发送状态行内容
        out.write(13);//发送回车符
        out.write(10);//发送换行符
    }

    /**
     * 重定向到uri指定的路径
     *
     * @param uri
     */
    public void sendRedirect(String uri) {
        //1设置状态代码位302
        statusCode = 302;
        statusReason = "Moved Temporarily";

        //2添加必要的响应头Location指定浏览器再次请求的位置
        addHeader("Location", uri);
    }


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
         /*
                根据正文文件类型来设置Content-Type用于告知浏览器该正文类型以便其理解.
                但是在HTTP协议中规定:如果发送响应时,不包含Content-Type响应头时,则是
                让浏览器自行理解正文类型.
             */
        try {
            //该方法会自动分析文件对应的Content-Type值,若无法识别会返回null
            //http://localhost:8088/TeduStore/index.html
                /*
                    比如:file表示的是 index.html 文件
                    该方法返回值为:"text/html"

                        file表示的是 jquery.js 文件
                    该方法那会的值为:"application/javascript"

                 */
            String type = Files.probeContentType(contentFile.toPath());
            if (type != null) {
                addHeader("Content-Type", type);
            }
        } catch (IOException e) {
        }
        addHeader("Content-Length", contentFile.length() + "");


    }

    /**
     * 添加一个响应头
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public OutputStream getOutputStream() {
        if (baos == null) {
            baos = new ByteArrayOutputStream();
        }
        return baos;
    }

    public PrintWriter getWriter() {
        /*
            进行流连接，创建一个PrintWrtier并最终连接到属性baos上
            将该PrintWriter返回给外界使用。
         */
        OutputStream out = getOutputStream();//baos
        OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        BufferedWriter bw = new BufferedWriter(osw);
        return new PrintWriter(bw, true);

//        return new PrintWriter(
//                new BufferedWriter(
//                        new OutputStreamWriter(
//                                getOutputStream(),StandardCharsets.UTF_8
//                        )
//                ),true
//        );
    }

    /**
     * 添加响应头Content-Type以及对应的值
     *
     * @param mime
     */
    public void setContentType(String mime) {
        addHeader("Content-Type", mime);
    }

}
