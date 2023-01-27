package xyz.fcidd.web.server.controller;

import lombok.extern.log4j.Log4j2;
import qrcode.QRCodeUtil;
import xyz.fcidd.web.server.annotations.Controller;
import xyz.fcidd.web.server.annotations.RequestMapping;
import xyz.fcidd.web.server.http.HttpServletRequest;
import xyz.fcidd.web.server.http.HttpServletResponse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

@Log4j2
@Controller
public class ToolsController {
    @RequestMapping("/myweb/createQR")
    public void createQR(HttpServletRequest request, HttpServletResponse response) {
        String line = request.getParameter("content");
        try {
            response.setContentType("image/jpeg");
            QRCodeUtil.encode(
                    line,   //二维码上显示的文字
                    "./logo.jpg",   //二维码中间的logo图
                    response.getOutputStream(),//生成的二维码数据会通过该流写出
                    true);//是否压缩logo图片的尺寸

            log.debug("二维码已生成");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @RequestMapping("/myweb/random.jpg")
    public void createRandomImage(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("image/jpeg");

        //1创建一张空白图片，同时指定宽高   理解为:搞一张白纸，准备画画
        BufferedImage image = new BufferedImage(
                70, 30, BufferedImage.TYPE_INT_RGB
        );

        //2通过图片获取绘制该图片的画笔，通过这个画笔就可以往该图片上绘制了
        Graphics g = image.getGraphics();

        Random random = new Random();
        //3随机生成个颜色涂满整张画布
        Color color = new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256));
        g.setColor(color);//设置画笔颜色
        g.fillRect(0, 0, 70, 30);

        //4向画布上绘制文字
        String line = "abcdefghjiklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        g.setFont(new Font(null, Font.BOLD, 20));
        for (int i = 0; i < 4; i++) {
            color = new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            g.setColor(color);//设置画笔颜色
            String str = line.charAt(random.nextInt(line.length())) + "";
            g.drawString(str, i * 15 + 5, 18 + random.nextInt(11) - 5);
        }

        //5随机生成5条干扰线
        for (int i = 0; i < 5; i++) {
            color = new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            g.setColor(color);//设置画笔颜色
            g.drawLine(random.nextInt(71), random.nextInt(31),
                    random.nextInt(71), random.nextInt(31));
        }


        try {
            ImageIO.write(image, "jpg",
                    response.getOutputStream());
            log.debug("图片已生成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        //1创建一张空白图片，同时指定宽高   理解为:搞一张白纸，准备画画
        BufferedImage image = new BufferedImage(
                70, 30, BufferedImage.TYPE_INT_RGB
        );

        //2通过图片获取绘制该图片的画笔，通过这个画笔就可以往该图片上绘制了
        Graphics g = image.getGraphics();

        Random random = new Random();
        //3随机生成个颜色涂满整张画布
        Color color = new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256));
        g.setColor(color);//设置画笔颜色
        g.fillRect(0, 0, 70, 30);

        //4向画布上绘制文字
        String line = "abcdefghjiklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        g.setFont(new Font(null, Font.BOLD, 20));
        for (int i = 0; i < 4; i++) {
            color = new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            g.setColor(color);//设置画笔颜色
            String str = line.charAt(random.nextInt(line.length())) + "";
            g.drawString(str, i * 15 + 5, 18 + random.nextInt(11) - 5);
        }

        //5随机生成5条干扰线
        for (int i = 0; i < 5; i++) {
            color = new Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            g.setColor(color);//设置画笔颜色
            g.drawLine(random.nextInt(71), random.nextInt(31),
                    random.nextInt(71), random.nextInt(31));
        }


        try {
            ImageIO.write(image, "jpg",
                    new FileOutputStream("./random.jpg"));
            log.debug("图片已生成");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public static void main(String[] args) {
//        try {
//            String line = "扫你妹!";
//            //参数1:二维码上显示的文字，参数2:二维码图片生成的位置
////            QRCodeUtil.encode(line,"./qr.jpg");
//
//            //参数1:二维码上显示的文字，参数2:二维码中间的logo图，
//            //参数3:二维码生成的位置，参数4:是否压缩logo图片的尺寸
////            QRCodeUtil.encode(line,"./logo.jpg",
////                      "./qr.jpg",true);
//
//            QRCodeUtil.encode(
//                    line,   //二维码上显示的文字
//                    "./logo.jpg",   //二维码中间的logo图
//                    new FileOutputStream("./qr.jpg"),//生成的二维码数据会通过该流写出
//                    true);//是否压缩logo图片的尺寸
//
//            System.out.println("二维码已生成");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
