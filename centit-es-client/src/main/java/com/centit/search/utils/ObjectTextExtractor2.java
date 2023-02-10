package com.centit.search.utils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 将业务对象解析成文本
 * <p>
 * 不支持对象循环引用，请手工将循环引用对象放入excludes属性中，否则将引虚拟机栈溢出异常
 * </p>
 */
@Deprecated
public abstract class ObjectTextExtractor2 {

    private static Logger logger = LogManager.getLogger(ObjectTextExtractor2.class);

    /**
     * 返回对象解析结果，includes和excludes参数为空则返回对象的所有非空属性的文本值
     *
     * @param object   待解析对象
     * @param includes 只返回指定类中的指定属性
     * @param excludes 排除指定类中的指定属性\n
     *                 优先级高于 includes
     * @return 对象文本
     */
    public static String extractText(Object object, Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes) {

        StringBuilder text = new StringBuilder();
        try {
            invoke(object, null, includes, excludes, text);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        return text.toString();
    }


    /**
     * 获取对象的属性值
     * 此方法可直接调用，
     *
     * @param object   任意对象
     * @param property 对象属性，需要解析的属性，如果属性为对象且需要解析，可以使用导向符号  . 例： \n
     *                 includes = new String[]{"id.userCode", "userInfo.loginName", "password"}; \n
     *                 如对象中不包含该属性，则忽略 \n
     * @param includes 只返回指定类中的指定属性
     * @param excludes 排除指定类中的指定属性\n
     *                 优先级高于 includes
     * @param value    StringBuilder
     * @throws NoSuchMethodException 异常
     * @throws IllegalAccessException 异常
     * @throws InvocationTargetException 异常
     */
    private static void invoke(Object object, String property,
                              Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes,
                              StringBuilder value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (StringUtils.isNotBlank(property) && property.contains(".")) {
            //判断需要解析的对象类型

            //将包含导向符号的属性解析
            String[] properties = property.split("\\.");
            if (isCollectionType(object)) {
                decideObjectClassType(object, property, includes, excludes, value);
            } else {
                Object o = MethodUtils.invokeMethod(object, analyzePropertyToMethodName(properties[0]));

                if (2 == properties.length) {
                    invoke(o, properties[1], includes, excludes, value);
                } else {
                    List<String> ps = new ArrayList<>();
                    for (int i = 1; i < properties.length; i++) {
                        ps.add(properties[i]);
                    }
                    //去除前一位导向符号
                    invoke(o, StringUtils.join(ps, "."), includes, excludes, value);
                }
            }

        } else {
            //执行get方法获取返回值
            if (isCollectionType(object)) {
                decideObjectClassType(object, property, includes, excludes, value);
            } else {
                //未明确要求返回哪些属性，就遍历所有属性
                if (StringUtils.isBlank(property)) {
                    for (String s : listProperties(object, includes, excludes)) {
                        invoke(object, s, includes, excludes, value);
                    }
                } else {
                    //执行对象的get方法
                    Object o = MethodUtils.invokeMethod(object, analyzePropertyToMethodName(property));
                    if (null == o) {
                        return;
                    }

                    if (o.getClass().isArray()) {
                        //判断返回结果是否是数组
                        for (int i = 0; i < Array.getLength(o); i++) {
                            Object next = Array.get(o, i);

                            invoke(next, includes, excludes, value);
                        }
                    } else if (ClassUtils.isAssignable(o.getClass(), Collection.class)) {
                        //执行结果是集合
                        Iterator<?> iter = ((Collection<?>) o).iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();

                            invoke(next, includes, excludes, value);
                        }
                    } else if (ClassUtils.isAssignable(o.getClass(), Map.class)) {
                        //执行结果为Map，Map只解析Value值
                        Iterator<?> iter = ((Map<?, ?>) o).values().iterator();
                        while (iter.hasNext()) {
                            Object next = iter.next();

                            invoke(next, includes, excludes, value);
                        }
                    } else {
                        //执行结果为基本类型
                        invoke(o, includes, excludes, value);
                    }
                }
            }
        }
    }

    /**
     * 获取类中可执行的属性
     * @param object   Object
     * @param includes 只返回指定类中的指定属性
     * @param excludes 排除指定类中的指定属性\n
     *                 优先级高于 includes
     * @return List
     */
    private static List<String> listProperties(Object object, Map<Class<?>, String[]> includes, Map<Class<?>, String[]>
            excludes) {
        List<String> properties = new ArrayList<>();

        //遍历该对象中所有get且无参的方法
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && ArrayUtils.isEmpty(method.getParameterTypes())) {
                String methodName = method.getName();

                methodName = methodName.substring(3);
                methodName = StringUtils.uncapitalize(methodName);
                properties.add(methodName);
            }
        }
        //过滤排除属性
        if (MapUtils.isNotEmpty(excludes)) {
            for (Map.Entry<Class<?>, String[]> clz : excludes.entrySet()) {
                if (ClassUtils.isAssignable(object.getClass(), clz.getKey())) {
                    Iterator<String> iter = properties.iterator();
                    while (iter.hasNext()) {
                        String next = iter.next();
                        if (ArrayUtils.contains(clz.getValue(), next)) {
                            iter.remove();
                        }
                    }

                    break;
                }
            }
        }
        //过滤包含属性
        if (MapUtils.isNotEmpty(includes)) {
            for (Map.Entry<Class<?>, String[]> clz : excludes.entrySet()) {
                if (ClassUtils.isAssignable(object.getClass(), clz.getKey())) {
                    Iterator<String> iter = properties.iterator();
                    while (iter.hasNext()) {
                        String next = iter.next();
                        if (!ArrayUtils.contains(clz.getValue(), next)) {
                            iter.remove();
                        }
                    }

                    break;
                }
            }
        }
        return properties;
    }

    /**
     * 判断是否是集合对象
     * @param object 任意对象
     * @return 是：True 否：False
     */
    private static boolean isCollectionType(Object object) {
        return ClassUtils.isAssignable(object.getClass(), Collection.class) || ClassUtils.isAssignable(object
                .getClass(), Map.class) || object.getClass().isArray();
    }


    /**
     * 对集合对象进行解析
     * @param object   任意对象
     * @param property 对象属性
     * @param includes 只返回指定类中的指定属性
     * @param excludes 排除指定类中的指定属性\n
     *                 优先级高于 includes
     * @param value    StringBuilder
     * @throws NoSuchMethodException 异常
     * @throws IllegalAccessException 异常
     * @throws InvocationTargetException 异常
     */
    private static void decideObjectClassType(Object object, String property,
                                              Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes,
                                              StringBuilder value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (ClassUtils.isAssignable(object.getClass(), Collection.class)) {
            //解析集合类型
            invoke((Collection<?>) object, property, includes, excludes, value);
        } else if (ClassUtils.isAssignable(object.getClass(), Map.class)) {
            // 解析Map类型
            invoke((Map<?, ?>) object, property, includes, excludes, value);
        } else if (object.getClass().isArray()) {
            //解析数组类型
            for (int i = 0; i < Array.getLength(object); i++) {
                Object next = Array.get(object, i);

                invoke(next, includes, excludes, value);
            }
        }
    }

    /**
     * 获取属性值
     *
     * @param object   任意对象
     * @param includes 只返回指定类中的指定属性
     * @param excludes 排除指定类中的指定属性\n
     *                 优先级高于 includes
     * @param value    StringBuilder
     * @throws NoSuchMethodException 异常
     * @throws IllegalAccessException 异常
     * @throws InvocationTargetException 异常
     */
    private static void invoke(Object object, Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes, StringBuilder value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (isPrimitiveType(object)) {
            value.append(object).append("\r\n");
        } else {
            List<String> properties = listProperties(object, includes, excludes);
            for (String s : properties) {
                invoke(object, s, includes, excludes, value);
            }
        }
    }

    /**
     * 集合对象处理
     * @param listObjects Collection
     * @param includes    只返回指定类中的指定属性
     * @param excludes    排除指定类中的指定属性\n
     *                    优先级高于 includes
     * @param property    集合中对象的属性
     * @param value       StringBuilder
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static void invoke(Collection<?> listObjects, String property,
                               Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes,
                               StringBuilder value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Object object : listObjects) {
            invoke(object, property, includes, excludes, value);
        }
    }

    /**
     * Map对象处理
     * @param listObjects Collection
     * @param includes    只返回指定类中的指定属性
     * @param excludes    排除指定类中的指定属性\n
     *                    优先级高于 includes
     * @param property    集合中对象的属性
     * @param value       StringBuilder
     * @throws NoSuchMethodException 异常
     * @throws IllegalAccessException 异常
     * @throws InvocationTargetException 异常
     */
    private static void invoke(Map<?, ?> listObjects, String property, Map<Class<?>, String[]> includes, Map<Class<?>, String[]> excludes, StringBuilder value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        invoke(listObjects.values(), property, includes, excludes, value);
    }


    /**
     * 对象是否为基本类型
     *
     * @param object 任意对象
     * @return 是：True 否：False
     */
    private static boolean isPrimitiveType(Object object) {
        Class[] cls = new Class[]{
                Byte.class, Integer.class, Short.class,
                Long.class, Boolean.class, Character.class,
                Float.class, Double.class, String.class};

        for (Class cl : cls) {
            if (ClassUtils.isAssignable(object.getClass(), cl, true)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将属性转换为get方法名
     * @param property 属性名称
     * @return 方法名称
     */
    private static String analyzePropertyToMethodName(String property) {
        if (property.contains(".")) {
            return ("get" + StringUtils.capitalize(property.split("\\.")[0]));
        } else {
            return ("get" + StringUtils.capitalize(property));
        }
    }

}
