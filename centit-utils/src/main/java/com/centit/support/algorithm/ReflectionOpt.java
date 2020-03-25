package com.centit.support.algorithm;

import com.centit.support.common.LeftRightPair;
import com.centit.support.common.ParamName;
import com.centit.support.file.FileType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 提供一些反射方面缺失功能的封装.
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class ReflectionOpt {

    protected static final Logger logger = LoggerFactory.getLogger(ReflectionOpt.class);

    private ReflectionOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /*
     * 循环向上转型,获取对象的DeclaredField.
     *
     * @throws NoSuchFieldException 如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Object object, String propertyName) throws NoSuchFieldException {
        assert (object != null);
        assert (propertyName != null && !propertyName.isEmpty());

        return getDeclaredField(object.getClass(), propertyName);
    }

    /*
     * 循环向上转型,获取对象的DeclaredField.
     *
     * @throws NoSuchFieldException 如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Class<?> clazz, String propertyName) throws NoSuchFieldException {
        assert (clazz != null);
        assert (propertyName != null && !propertyName.isEmpty());
        //Assert.notNull(object);
        //Assert.hasText(propertyName);
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field f = superClass.getDeclaredField(propertyName);
                if (f != null)
                    return f;
            } catch (NoSuchFieldException e) {
                logger.debug(e.getMessage());
                // Field不在当前类定义,继续向上转型
            }
        }
        throw new NoSuchFieldException("No such field: " + clazz.getName() + '.' + propertyName);
    }

    /*
     * 获得对象的属性值
     * @param object
     * @param field
     * @return
     * @throws NoSuchFieldException
     */
    public static Object forceGetFieldValue(Object object, Field field) {
        assert (object != null);

        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            logger.info("error wont' happen." + e.getMessage());
        }
        field.setAccessible(accessible);
        return result;
    }


    /*
     * 获得get field value by getter
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        Method md = null;
        try {
            md = obj.getClass().getMethod("get" + StringUtils.capitalize(fieldName));
        } catch (NoSuchMethodException noGet) {
            try {
                md = obj.getClass().getMethod("is" + StringUtils.capitalize(fieldName));
            } catch (Exception e) {
                logger.error(noGet.getMessage() + e.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (md == null) {
            try {
                return forceGetProperty(obj, fieldName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            try {
                return md.invoke(obj);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return null;
    }

    public static boolean setFieldValue(Object object, String fieldName, Object newValue, Class<?> paramType) {
        Class<?> relParamType = (paramType != null) ? paramType : (newValue != null ? newValue.getClass() : null);
        boolean hasSetValue = false;
        if (relParamType != null) {
            try {
                Method md = object.getClass().getMethod("set" + StringUtils.capitalize(fieldName), relParamType);
                md.invoke(object, newValue);
                hasSetValue = true;
            } catch (NoSuchMethodException noSet) {
                logger.error(noSet.getMessage(), noSet);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        if (!hasSetValue) {
            try {
                forceSetProperty(object, fieldName, newValue);
                hasSetValue = true;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return hasSetValue;
    }

    public static boolean setFieldValue(Object object, String fieldName, Object newValue) {
        return setFieldValue(object, fieldName, newValue, null);
    }

    /*
     * 获得get field value by getter
     */
    public static Object getFieldValue(Object obj, Field field) {
        try {
            return field.get(obj);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    /*
     * 获取对象变量值,忽略private,protected修饰符的限制.
     *
     * @throws NoSuchFieldException 如果没有该Field时抛出.
     */
    public static Object forceGetProperty(Object object, String propertyName) throws NoSuchFieldException {
        assert (object != null);
        assert (propertyName != null && !propertyName.isEmpty());

        Field field = getDeclaredField(object, propertyName);
/*        if(field==null){
            log.debug("property not found. (没有找到对应的属性) 对象：" + object.toString() +" 属性 ："+ propertyName);
            return null;
        }*/
        return forceGetFieldValue(object, field);
    }

    /*
     * 设置对象变量值,忽略private,protected修饰符的限制.
     *
     * @throws NoSuchFieldException 如果没有该Field时抛出.
     */
    public static void forceSetProperty(Object object, String propertyName, Object newValue)
        throws NoSuchFieldException {
        assert (object != null);
        assert (propertyName != null && !propertyName.isEmpty());

        Field field = getDeclaredField(object, propertyName);
/*        if(field==null){
            log.debug("property not found. (没有找到对应的属性) 对象：" + object.toString() +" 属性 ："+ propertyName);
            return;
        }*/
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException e) {
            logger.error("Error won't happen." + e.getMessage(), e);
        }
        if (!accessible) {
            field.setAccessible(accessible);
        }
    }

    /*
     * 调用对象函数,忽略private,protected修饰符的限制.
     *
     * @throws NoSuchMethodException 如果没有该Method时抛出.
     */
    public static Object invokePrivateMethod(Object object, String methodName, Object... params)
        throws NoSuchMethodException {
        assert (object != null);
        assert (methodName != null && !methodName.isEmpty());

        Class<?>[] types = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            types[i] = params[i].getClass();
        }

        Class<?> clazz = object.getClass();
        Method method = null;
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                method = superClass.getDeclaredMethod(methodName, types);
                break;
            } catch (NoSuchMethodException e) {
                logger.debug("方法不在当前类定义,继续向上转型");
                // 方法不在当前类定义,继续向上转型
            }
        }

        if (method == null)
            throw new NoSuchMethodException("No Such Method:" + clazz.getSimpleName() + methodName);

        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Object result = null;
        try {
            result = method.invoke(object, params);
        } catch (Exception e) {
            //ReflectionUtils.handleReflectionException(e);
            logger.error(e.getMessage());
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        method.setAccessible(accessible);
        return result;
    }

    /*
     * 取得Field列表.
     */
    public static Field[] getFields(Object object) {
        return object.getClass().getDeclaredFields();
    }

    /*
     * 按Filed的类型取得Field列表.
     */
    public static List<Field> getFieldsByType(Object object, Class<?> type) {
        List<Field> list = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (type.isAssignableFrom(field.getType())) {
                list.add(field);
            }
        }
        return list;
    }

    /*
     * 按FiledName获得Field的类型.
     */
    public static Class<?> getPropertyType(Class<?> type, String name) throws NoSuchFieldException {
        return getDeclaredField(type, name).getType();
    }


    /*
     * 获得field的getter函数名称.
     */
    public static String methodNameToField(String methodName) {
        if (methodName == null)
            return null;
        int sl = methodName.length();

        if (sl > 3 && (methodName.startsWith("get") || methodName.startsWith("set")))
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);

        if (sl > 2 && methodName.startsWith("is"))
            return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);

        return methodName;
    }

    /*
     * 获得field的getter函数,如果找不到该方法,返回null.
     */
    public static Method getGetterMethod(Class<?> classType, Class<?> propertyType, String fieldName) {
        try {
            String getFuncName = boolean.class.equals(propertyType) ?/*|| Boolean.class.isAssignableFrom(propertyType)*/
                "is" + StringUtils.capitalize(fieldName) : "get" + StringUtils.capitalize(fieldName);
            Method md = classType.getMethod(getFuncName);
            if (propertyType.isAssignableFrom(md.getReturnType()))
                return md;
            else
                return null;
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /*
     * 获得field的getter函数,如果找不到该方法,返回null.
     */
    public static Method getGetterMethod(Class<?> classType, String fieldName) {
        try {
            Method md = classType.getMethod("get" + StringUtils.capitalize(fieldName));
            if (void.class.equals(md.getReturnType()))
                return null;
            else
                return md;
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /*
     * 获得field的getter函数,如果找不到该方法,返回null.
     */
    public static Method getSetterMethod(Class<?> classType, Class<?> propertyType, String fieldName) {
        try {
            return classType.getMethod("set" + StringUtils.capitalize(fieldName), propertyType);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static LeftRightPair<Method, Object[]> getMatchBestMethod(Class<?> classType, String methodName, Map<String, Object> params) {
        Method[] mths = classType.getMethods();
        List<Method> getMths = new ArrayList<>();
        int matchParam = -1;
        LeftRightPair<Method, Object[]> mp = new LeftRightPair<>();
        for (Method mth : mths) {
            if (mth.getName().equals(methodName)) {
                Parameter[] parameters = mth.getParameters();
                int nps = parameters.length;
                boolean bmatch = true;
                Object[] prams = null;
                if (nps > 0) {
                    prams = new Object[nps];
                    for (int i = 0; i < nps; i++) {
                        String paramName = parameters[i].getName();
                        if (parameters[i].isAnnotationPresent(ParamName.class)) {
                            ParamName param = parameters[i].getAnnotation(ParamName.class);
                            paramName = param.value();
                        }
                        prams[i] = params.get(paramName);
                        if (prams[i] == null) {
                            bmatch = false;
                        } else {
                            // 类型转换
                            prams[i] = GeneralAlgorithm.castObjectToType(prams[i], parameters[i].getType());
                            if (prams[i] == null) {
                                bmatch = false;
                            }
                        }
                    }
                }
                if (bmatch && nps > matchParam) {
                    matchParam = nps;
                    mp.setLeft(mth);
                    mp.setRight(prams);
                }
            }
        }
        return mp;
    }

    private static void innerAddListItem(List<Object> objList, Object obj) {
        if (obj instanceof Collection) {
            Collection<?> templist = (Collection<?>) obj;
            objList.addAll(templist);
        } else if (obj instanceof Object[]) {
            Object[] objs = (Object[]) obj;
            for (Object tobj : objs) {
                objList.add(tobj);
            }
        } else {
            objList.add(obj);
        }
    }
    /**
     * 获得 对象的 属性; 目前只能支持一维数组的获取，多维数据暂时不支持，目前看也没有这个需要
     *
     * @param sourceObj  可以是 任意对象
     * @param expression 表达式 a.b[1].c 也可以 a.b[1].[2].c 间接实现二维数组
     * @return 返回结果
     */
    public static Object attainExpressionValue(Object sourceObj, String expression) {
        if (sourceObj == null || StringUtils.isBlank(expression))
            return null;
        if (".".equals(expression)) {
            return sourceObj;
        }
        int nPos = expression.indexOf('.');
        String fieldValue;
        String restExpression = ".";
        if (nPos > 0) {
            fieldValue = expression.substring(0, nPos).trim();
            if (expression.length() > nPos + 1) {
                restExpression = expression.substring(nPos + 1);
            }
        } else if (nPos == 0) {
            fieldValue = "";
            restExpression = expression.substring(1);
        } else {
            fieldValue = expression.trim();
        }

        int nAarrayInd = -1;
        nPos = fieldValue.indexOf('[');
        if (nPos >= 0) {
            String sArrayInd = fieldValue.substring(nPos + 1, fieldValue.length() - 1);
            if (StringRegularOpt.isNumber(sArrayInd)) {
                nAarrayInd = NumberBaseOpt.castObjectToInteger(sArrayInd, 0);
            }
            fieldValue = fieldValue.substring(0, nPos);
        }

        Object retObj;
        if (StringUtils.isBlank(fieldValue)) {
            retObj = sourceObj;
        } else if (sourceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> objMap = (Map<String, Object>) sourceObj;
            retObj = objMap.get(fieldValue);
        } else {
            //如果是一个标量则不应该再有属性，所以统一返回null
            if (ReflectionOpt.isScalarType(sourceObj.getClass())) {
                return null;
            } else if(sourceObj instanceof Collection){
                Collection<?> objlist = (Collection<?>) sourceObj;
                int objSize = objlist.size();
                List<Object> retList = new ArrayList<>(objSize+1);
                for (Object obj : objlist) {
                    Object tempObj = attainExpressionValue(obj, fieldValue);
                    innerAddListItem(retList, tempObj);
                }
                retObj = retList;
            } else if(sourceObj instanceof Object[]){
                Object[] objs = (Object[]) sourceObj;
                List<Object> retList = new ArrayList<>(objs.length+1);
                for (Object obj : objs) {
                    Object tempObj = attainExpressionValue(obj, fieldValue);
                    innerAddListItem(retList, tempObj);
                }
                retObj = retList;
            } else {
                retObj = ReflectionOpt.getFieldValue(sourceObj, fieldValue);
            }
        }
        if (retObj == null)
            return null;

        if (nAarrayInd >= 0 && retObj instanceof Collection) {
            Collection<?> objlist = (Collection<?>) retObj;
            int objSize = objlist.size();
            if (objSize < 1 || nAarrayInd >= objSize) {
                return null;
            }
            int i = 0;
            for (Object obj : objlist) {
                if (nAarrayInd == i) {
                    retObj = obj;
                }
                i++;
            }
        } else if (nAarrayInd >= 0 && retObj instanceof Object[]) {
            Object[] objs = (Object[]) retObj;
            int objSize = objs.length;
            if (objSize < 1 || nAarrayInd >= objSize) {
                return null;
            }
            retObj = objs[nAarrayInd];
        }
        return attainExpressionValue(retObj, restExpression);
    }

    /*
     * 获得get boolean field value by getter
     */
    public static Boolean getBooleanFieldValue(Object obj, String fieldName) {
        try {
            Method md = obj.getClass().getMethod("is" + StringUtils.capitalize(fieldName));
            if (md == null)
                return null;
            Object objValue = md.invoke(obj);
            if (objValue == null)
                return null;
            if (objValue instanceof Boolean)
                return (Boolean) objValue;
            return Boolean.valueOf(objValue.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /*
     * 获得boolean 型 field的getter函数,如果找不到该方法,返回null.
     */
    public static Method getBooleanGetterMethod(Class<?> classType, String fieldName) {
        try {
            return classType.getMethod("is" + StringUtils.capitalize(fieldName));
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将getter方法映射为对应属性
     *
     * @param method Getter方法
     * @return 对应属性名
     */
    public static String mapGetter2Field(Method method) {
        String methodName = method.getName();
        return StringUtils.uncapitalize(
            methodName.substring(methodName.startsWith("is") ? 2 : 3));
    }

    /*
     * 获取所有getMethod方法
     */
    public static List<Method> getAllGetterMethod(Class<?> type) {
        try {
            Method[] mths = type.getMethods();
            List<Method> getMths = new ArrayList<>();
            for (Method mth : mths)
                if ((mth.getName().startsWith("get") || mth.getName().startsWith("is"))
                    && !mth.getName().equals("getClass")
                    && !void.class.equals(mth.getReturnType())
                    //&& ! mth.getReturnType().getName().equals("void")
                    && mth.getGenericParameterTypes().length < 1)
                    getMths.add(mth);
            return getMths;

        } catch (SecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /*
     * 获取所有setMethod方法
     */
    public static List<Method> getAllSetterMethod(Class<?> type) {
        try {
            Method[] mths = type.getMethods();
            List<Method> setMths = new ArrayList<Method>();
            for (Method mth : mths) {
                String methodName = mth.getName();
                if (methodName.startsWith("set")
                    && methodName.length() >= 4
                    //&& mth.getReturnType().getName().equals("void")
                    && mth.getGenericParameterTypes().length == 1) {
                    setMths.add(mth);
                }
            }
            return setMths;

        } catch (SecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /*
     * 调用对象的无参数函数
     */
    public static void invokeNoParamFunc(Object demander, String smethod) {
        try {
            //"copyNotNullProperty"
            Method setV = demander.getClass().getMethod(smethod);
            //Class rt = d.getReturnType();
/*            if(setV == null)
                return;    */
            setV.invoke(demander);
        } catch (NoSuchMethodException | SecurityException
            | IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * 调用相同类型的类之间的二元操作
     */
    public static <T extends Object> void invokeBinaryOpt(T demander, String smethod, T param) {
        try {
            //"copyNotNullProperty"
            Method setV = demander.getClass().getMethod(smethod, demander.getClass());
            //Class rt = d.getReturnType();
/*            if(setV == null)
                return;    */
            setV.invoke(demander, param);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException
            | IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param clazz The class to introspect
     * @return the first generic declaration, or <code>Object.class</code> if cannot be determined
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic declaration,start from 0.
     * @return the index generic declaration, or <code>Object.class</code> if
     * cannot be determined
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }
        return (Class<?>) params[index];
    }


    /**
     * isPrimitiveType
     * 判断一个类型是否是基础类型 int boolean void float double char
     * 或者单值类型 String Date Integer Float Double，  scalar
     *
     * @param tp isPrimitiveType
     * @return isPrimitiveType
     */
    public static boolean isPrimitiveType(Class<?> tp) {
        return tp.isPrimitive() || tp.getName().startsWith("java.lang.");
    }

    /**
     * isScalarType
     * 判断一个类型是否是基础类型 int boolean void float double char
     * 或者单值类型 String Date Integer Float Double，  scalar
     *
     * @param tp isScalarType
     * @return isScalarType
     */
    public static boolean isScalarType(Class<?> tp) {
        if (tp.isPrimitive()) {
            return true;
        }

        if (tp.isArray()) {
            return false;
        }
        //tp.getPackage()
        String tpName = tp.getPackage().getName();
        if (tpName.equals("java.lang") || tpName.equals("java.sql"))
            return true;
        if (java.util.Date.class.isAssignableFrom(tp))// "java.util.Date".equals(tp.getName()))
            return true;
        if (java.util.UUID.class.isAssignableFrom(tp))// "java.util.UUID".equals(tp.getName()))
            return true;

        return false;
    }

    public static boolean isNumberType(Class<?> tp) {
        return java.lang.Number.class.isAssignableFrom(tp);
        //tp.getSuperclass().equals(Number.class) || tp.equals(Number.class);
    }

    /**
     * 判断一个对象是否是 数组[]、Collection(List)
     *
     * @param obj 对象
     * @return 否是 数组[]
     */
    public static boolean isArray(Object obj) {
        Class<?> tp = obj.getClass();
        if (tp.isArray())
            return true;
        //if(obj instanceof Object[])
        //    return true;
        if (obj instanceof Collection<?>)
            return true;
        return false;
    }

    /**
     * 判断一个类型是否是 数组[]、Collection(List)
     *
     * @param tp 类型
     * @return 否是 数组[]
     */
    public static boolean isArrayType(Class<?> tp) {
        if (tp.isArray())
            return true;
        if (Collection.class.isAssignableFrom(tp))
            return true;
        return false;
    }

    /*
     *  得到当前方法的名字
     */
    public static String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    public static String getJavaTypeName(Class<?> type) {

        String typeName = type.getTypeName();
        if (typeName.indexOf('.') < 1) {
            return typeName;
        } else if (typeName.startsWith("java.lang.")
            || "java.util.Date".equals(typeName)
            || "java.sql.Clob".equals(typeName)
            || "java.sql.Blob".equals(typeName)
            || "java.util.UUID".equals(typeName)
            || "java.math.BigDecimal".equals(typeName)
            || "java.math.BigInteger".equals(typeName)) {
            return FileType.getFileExtName(typeName);
        } else if (typeName.startsWith("java.sql.")) {
            return "sql" + FileType.getFileExtName(typeName);
        } else {
            return typeName;
        }
    }

    /*
     * 类 clazz 必需有一个无参数的默认构造函数
     * 这个Map需要和属性严格匹配，如果需要更灵活的匹配可以 使用OBNL
     *  或者转换为JSONString在用JSON转化为对象
     * @param clazz
     * @param properties
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public <T> T createObjectFromMap(Class<T> clazz, Map<String, Object> properties)
        throws InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException {
        T obj = (T) clazz.newInstance();
        try {
            Method[] mths = clazz.getMethods();
            for (Method mth : mths) {
                Type[] paramTypes = mth.getGenericParameterTypes();
                String methodName = mth.getName();
                if (methodName.startsWith("set")
                    && methodName.length() >= 4
                    && paramTypes.length == 1) {
                    Object value = properties.get(methodName.substring(3, 4).toLowerCase() + methodName.substring(4));
                    if (value != null) {
                        mth.invoke(obj, value);
                    }
                }
            }
        } catch (SecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return obj;
    }
}

