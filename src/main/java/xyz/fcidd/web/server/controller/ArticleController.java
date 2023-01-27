package xyz.fcidd.web.server.controller;

import xyz.fcidd.web.server.annotations.Controller;
import xyz.fcidd.web.server.annotations.RequestMapping;
import xyz.fcidd.web.server.core.ClientHandler;
import xyz.fcidd.web.server.entity.Article;
import xyz.fcidd.web.server.http.HttpServletRequest;
import xyz.fcidd.web.server.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

@Controller
public class ArticleController {
    private static File rootDir;
    private static File staticDir;

    //表示articles目录
    private static File articleDir;

    static {
        try {
            rootDir = new File(
                    ClientHandler.class.getClassLoader()
                            .getResource(".").toURI()
            );
            staticDir = new File(rootDir, "static");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        articleDir = new File("articles");
        if (!articleDir.exists()) {
            articleDir.mkdirs();
        }
    }

    @RequestMapping("/myweb/writeArticle")
    public void writeArticle(HttpServletRequest request, HttpServletResponse response) {
        //1获取表单数据
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");
        if (title == null || author == null || content == null) {
            File file = new File(staticDir, "/myweb/article_info_error.html");
            response.setContentFile(file);
            return;
        }

        //2写入文件
        File articleFile = new File(articleDir, title + ".obj");
        if (articleFile.exists()) {//文件存在,说明重复的标题,不能发表(需求过于严苛,后期数据库可通过ID避免该问题).
            File file = new File(staticDir, "/myweb/have_article.html");
            response.setContentFile(file);
            return;
        }

        Article article = new Article(title, author, content);
        try (
                FileOutputStream fos = new FileOutputStream(articleFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(article);
            File file = new File(staticDir, "/myweb/article_success.html");
            response.setContentFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //3响应页面

    }
}
