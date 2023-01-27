package xyz.fcidd.web.server.core;

import lombok.extern.log4j.Log4j2;
import xyz.fcidd.web.server.http.HttpServletRequest;
import xyz.fcidd.web.server.http.HttpServletResponse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Log4j2
public class ParameterUtil {

    static Object[] getParameterValues(Method method, HttpServletRequest request, HttpServletResponse response) throws InstantiationException, IllegalAccessException {
        Parameter[] paras = method.getParameters();//获取该方法的所有参数
        Object[] paraValues = new Object[paras.length];//根据参数的个数创建一个Object数组，用于存放所有需要传给该方法的实参
        for (int i = 0; i < paras.length; i++) {
            Parameter para = paras[i];//获取每一个参数
            Class paraType = para.getType();//获取参数类型
            String paraName = para.getName();//参数名
            String paraValue = request.getParameter(paraName);//根据参数名去request提取对应的参数值
            if (paraType.isPrimitive() || paraType == String.class) {//是否为基本类型或String类型
                paraValues[i] = transferBasicType(paraType, paraValue);
            } else if (paraType == HttpServletRequest.class) {//如果该参数是请求对象
                paraValues[i] = request;
            } else if (paraType == HttpServletResponse.class) {//如果该参数为响应对象
                paraValues[i] = response;
            } else {//一个java bean作为参数
                Object obj;
                try {
                    obj = paraType.newInstance();
                } catch (InstantiationException e) {
                    log.debug(paraType.getName() + ":缺少无参构造器");
                    throw e;
                } catch (IllegalAccessException e) {
                    log.debug(paraType.getName() + ":缺少public的无参构造器");
                    throw e;
                }
                request.getParameters().forEach((name, value) -> {
                    try {
                        log.debug("属性名:" + name);
                        Field field = paraType.getDeclaredField(name);
                        String fieldName = field.getName();//获取属性名
                        StringBuilder builder = new StringBuilder(fieldName);
                        builder.replace(0, 1, builder.substring(0, 1).toUpperCase());
                        String setMethodName = "set" + builder.toString();//根据属性名拼接出对应的set方法名
                        log.debug(setMethodName);
                        try {
                            Method setMethod = paraType.getMethod(setMethodName, field.getType());//获取该属性的set方法
                            log.debug("调用方法:" + setMethod);
                            setMethod.invoke(obj, transferBasicType(field.getType(), value));//将参数值通过set方法设置到该属性上
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } catch (NoSuchFieldException e) {
                        //没有该属性则跳过
                        log.debug("没有该属性");
                    }
                });
                paraValues[i] = obj;
            }
        }
        return paraValues;
    }

    /**
     * 将value转换为cls表示的类型
     *
     * @param cls
     * @param value
     * @return
     */
    private static Object transferBasicType(Class cls, String value) {
        if (value == null || cls == null) {
            return null;
        } else if (cls == int.class || cls == Integer.class) {
            return Integer.parseInt(value);
        } else if (cls == String.class) {
            return value;
        } else if (cls == byte.class || cls == Byte.class) {
            return Byte.parseByte(value);
        } else if (cls == short.class || cls == Short.class) {
            return Short.parseShort(value);
        } else if (cls == long.class || cls == Long.class) {
            return Long.parseLong(value);
        } else if (cls == float.class || cls == Float.class) {
            return Integer.parseInt(value);
        } else if (cls == double.class || cls == Double.class) {
            return Double.parseDouble(value);
        } else if (cls == char.class || cls == Character.class) {
            return value.charAt(0);
        } else if (cls == boolean.class || cls == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }
}
