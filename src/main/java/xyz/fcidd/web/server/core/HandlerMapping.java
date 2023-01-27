package xyz.fcidd.web.server.core;

import lombok.extern.log4j.Log4j2;
import xyz.fcidd.web.server.annotations.Controller;
import xyz.fcidd.web.server.annotations.RequestMapping;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用当前类维护请求路径与对应的Controller中处理该请求的方法
 * 使得DispatcherServlet在处理请求时判断是否为业务可以使用当前类完成。
 */

@Log4j2
public class HandlerMapping {
    public static Map<String, MethodMapping> mapping = new HashMap();

    static {
        initMapping();
    }

    private static void initMapping() {
        try {
            File rootDir = new File(HandlerMapping.class.getClassLoader().
                    getResource("./").toURI());
            File dir = new File(rootDir, "/xyz/fcidd/web/server/controller");
            File[] files = dir.listFiles(f -> f.getName().endsWith(".class"));
            for (File file : files) {
                String fileName = file.getName();
                String className = fileName.substring(0, fileName.indexOf("."));
                log.debug(fileName);
                Class cls = Class.forName("xyz.fcidd.controller." + className);
                //判断当前类是否为Controller(是否被@Controller标注)
                if (cls.isAnnotationPresent(Controller.class)) {
                    //将该Controller实例化
                    Object controller = cls.newInstance();
                    //获取该类中所有方法
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                        //判断当前方法是否为处理业务的方法(是否被@RequestMapping标注)
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            //获取注解@RequestMapping
                            RequestMapping rm = method.getAnnotation(RequestMapping.class);
                            /*
                                比如:
                                @Controller
                                public class UserController{
                                    @RequestMapping("/myweb/reg")
                                    public void reg(HttpServletRequest request,HttpServletResponse response){...}
                                    ...
                                }

                                Object controller : UserController对象
                                Method method : reg()方法
                                String value : "/myweb/reg"
                             */
                            String value = rm.value();//该方法处理的请求路径
                            MethodMapping mm = new MethodMapping(method, controller);
                            mapping.put(value, mm);
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据请求路径获取处理该请求的Controller以及对应的方法
     *
     * @param uri
     * @return
     */
    public static MethodMapping getMethod(String uri) {
        return mapping.get(uri);
    }

    public static void main(String[] args) {
        mapping.forEach(
                (k, v) -> {
                    log.debug("请求路径" + k);
                    log.debug("对应的处理方法:" + v.getMethod().getName());
                }
        );
    }


    public static class MethodMapping {
        //method.invoke(controller,...);
        private Method method;      //方法对象
        private Object controller;  //方法所属的Controller对象

        public MethodMapping(Method method, Object controller) {
            this.method = method;
            this.controller = controller;
        }

        public Method getMethod() {
            return method;
        }

        public Object getController() {
            return controller;
        }
    }
}







