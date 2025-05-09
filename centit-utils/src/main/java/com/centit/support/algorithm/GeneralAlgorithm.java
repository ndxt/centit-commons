package com.centit.support.algorithm;

import com.alibaba.fastjson2.util.TypeUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by codefan on 17-9-7.
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class GeneralAlgorithm {

    private GeneralAlgorithm() {
        throw new IllegalAccessError("Utility class");
    }

    public static <T> T nvl(T obj, T obj2) {
        return obj == null ? obj2 : obj;
    }

    public static <T> T nvl2(Object obj, T obj2, T obj3) {
        return obj == null ? obj3 : obj2;
    }


    /**
     * return (a == b) or (a != null and a.equals(b));
     * @see java.util.Objects equals
     * @param operand an object
     * @param operand2 an object to be compared with {@code a} for equality
     * @param allNullAsTrue 两个参数都是null时返回 true
     * @return {@code true} if the arguments are equal to each other
     * and {@code false} otherwise
     */
    public static boolean equals(Object operand, Object operand2, boolean allNullAsTrue) {
        if(allNullAsTrue) {
            if (operand == operand2) {
                return true;
            }
            if (operand == null || operand2 == null) {
                return false;
            }
        } else {
            if (operand == null || operand2 == null) {
                return false;
            }
            if (operand == operand2) {
                return true;
            }
        }
        if(operand.getClass().equals(operand2.getClass())){
            return operand.equals(operand2);
        }

        if(operand instanceof Number && operand2 instanceof Number) {
            return NumberBaseOpt.castObjectToDouble(operand).compareTo(
                NumberBaseOpt.castObjectToDouble(operand2)) == 0;
        }

        return StringBaseOpt.objectToString(operand).compareTo(
            StringBaseOpt.objectToString(operand2)) == 0;
    }

    public static boolean equals(Object operand, Object operand2) {

        return equals(operand, operand2, true);
    }

    public static int compareTwoObject(Object operand, Object operand2, boolean nullAsFirst) {
        if (operand == null && operand2 == null) {
            return 0;
        }

        if (operand == null) {
            return nullAsFirst ? -1 : 1;
        }

        if (operand2 == null) {
            return nullAsFirst ? 1 : -1;
        }
        //operand.getClass().getComponentType().isAssignableFrom(operand.getClass())
        if(operand instanceof Comparable && operand2 instanceof Comparable &&
            operand.getClass().equals(operand2.getClass())){
            if(operand instanceof String) {
                Comparator<Object> compare = Collator.getInstance(java.util.Locale.CHINA);
                return compare.compare(operand, operand2);
            }
            return ObjectUtils.compare((Comparable)operand , (Comparable)operand2);
        }

        if (NumberBaseOpt.isNumber(operand)
            && NumberBaseOpt.isNumber(operand2)) {
            return ObjectUtils.compare(
                NumberBaseOpt.castObjectToDouble(operand),
                NumberBaseOpt.castObjectToDouble(operand2));
        }

        Comparator<Object> compare = Collator.getInstance(java.util.Locale.CHINA);
        return compare.compare(
            StringBaseOpt.objectToString(operand),
            StringBaseOpt.objectToString(operand2));
    }

    public static int compareTwoObject(Object operand, Object operand2) {
        return compareTwoObject(operand, operand2, true);
    }

    public static boolean betweenTwoObject(Object operand, Object obj1, Object obj2) {
        int a = compareTwoObject(operand, obj1);
        int b = compareTwoObject(operand, obj2);
        return a * b <= 0;
    }

    public static int compareTwoObjectByField(Object data1, Object data2, String[] fields, boolean nullAsFirst) {
        if ((data1 == null && data2 == null) || (fields == null)) {
            return 0;
        }

        if (data1 == null) {
            return nullAsFirst?-1:1;
        }

        if (data2 == null) {
            return nullAsFirst?1:-1;
        }

        for (String field : fields) {
            Object obj1;
            if(data1 instanceof Map){
                obj1 = ((Map<?, ?>)data1).get(field);
            }else{
                obj1 = ReflectionOpt.getFieldValue(data1, field);
            }

            Object obj2;
            if(data2 instanceof Map){
                obj2 = ((Map<?, ?>)data2).get(field);
            }else{
                obj2 = ReflectionOpt.getFieldValue(data2, field);
            }

            int cr = GeneralAlgorithm.compareTwoObject(obj1, obj2, nullAsFirst);
            if (cr != 0) {
                return cr;
            }
        }
        return 0;
    }

    /**
     * int compare(final T c1, final T c2)
     * 等价于 ObjectUtils.compare
     *
     * @param <T>         比较对对象类型
     * @param l1          参数1
     * @param l2          参数2
     * @param nullAsFirst true null 在最前面否则在最后面
     * @return 返回比较值 0 相等 1 大于 -1 小于
     * @see org.apache.commons.lang3.ObjectUtils compare
     */
    public static <T extends Comparable<? super T>> int compareTwoComparableObject(T l1, T l2, boolean nullAsFirst) {
        return (l1 == null && l2 == null) ? 0 : (
            l1 == null ? (nullAsFirst ? -1 : 1) : (
                l2 == null ? (nullAsFirst ? 1 : -1) : (
                    l1.compareTo(l2)
                )
            )
        );
    }

    public static <T extends Comparable<? super T>> int compareTwoComparableObject(T l1, T l2) {
        return compareTwoComparableObject(l1, l2, true);
    }

    /**
     * @param keyExtractor 获取主键方法
     * @param nullAsFirst  true null 在最前面否则在最后面
     * @param <T>          比较对象类型
     * @param <U>          比较主键类型
     * @return 比较结果
     */
    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
        Function<? super T, ? extends U> keyExtractor, boolean nullAsFirst) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable)
            (c1, c2) ->
                compareTwoComparableObject(keyExtractor.apply(c1), keyExtractor.apply(c2), nullAsFirst);
    }

    private static int getJavaTypeOrder(Object a) {
        switch (ReflectionOpt.getJavaTypeName(a.getClass())) {
            case "int":
            case "Integer":
                return 1;
            case "long":
            case "Long":
                return 2;
            case "float":
            case "Float":
                return 3;
            case "double":
            case "Double":
                return 4;
            case "BigInteger":
                return 5;
            case "BigDecimal":
                return 6;
            case "String":
                return 10;
            default:
                return 100;
        }
    }

    private static Object checkStringObject(Object obj){
        if(obj instanceof String ) {
            if( StringRegularOpt.isNumber( (String)obj)){
                return NumberBaseOpt.castObjectToNumber(obj);
            }
        }
        return obj;
    }
    /**
     * 将两个对象加+起来，可能是数字相加，也可能是字符串连接
     *
     * @param a object1
     * @param b object2
     * @param nullSensitive  在数字计算是null敏感，就是有一个数为null则结果为null
     * @return 相加结果
     */
    public static Object addTwoObject(Object a, Object b, boolean nullSensitive) {
        if (a == null) {
            if (nullSensitive && (b == null || b instanceof java.lang.Number)) {
                return null;
            }
            return b;
        }
        if (b == null) {
            if (nullSensitive && a instanceof java.lang.Number) {
                return null;
            }
            return a;
        }
        a = checkStringObject(a);
        b = checkStringObject(b);

        if (a instanceof java.lang.Number && b instanceof java.lang.Number) {
            int retType = Math.max(getJavaTypeOrder(a), getJavaTypeOrder(b));
            switch (retType) {
                case 1:
                    return NumberBaseOpt.castObjectToInteger(a) +
                        NumberBaseOpt.castObjectToInteger(b);
                case 2:
                    return NumberBaseOpt.castObjectToLong(a) +
                        NumberBaseOpt.castObjectToLong(b);
                case 5:
                    return NumberBaseOpt.castObjectToBigInteger(a).add(
                        NumberBaseOpt.castObjectToBigInteger(b));
                case 6:
                    return NumberBaseOpt.castObjectToBigDecimal(a).add(
                        NumberBaseOpt.castObjectToBigDecimal(b));
                case 3:
                case 4:
                default:
                    return NumberBaseOpt.castObjectToDouble(a) +
                        NumberBaseOpt.castObjectToDouble(b);
            }
        } else if (a instanceof Date && NumberBaseOpt.isNumber(b)) {
            float num = NumberBaseOpt.castObjectToFloat(b, 0.f);
            return DatetimeOpt.addDays((Date) a, num);
        } else if (a instanceof Collection) {
            List<Object> objs = new ArrayList<>();
            if (b instanceof Collection) {
                objs.addAll((Collection<Object>) a);
                objs.addAll((Collection<Object>) b);
            } else {
                for (Object obj : (Collection<Object>) a) {
                    objs.add(addTwoObject(obj, b, nullSensitive));
                }
            }
            return objs;
        }
        return StringBaseOpt.concat(
            StringBaseOpt.castObjectToString(a),
            StringBaseOpt.castObjectToString(b));
    }

    public static Object addTwoObject(Object a, Object b) {
        return addTwoObject(a, b, true);
    }

    public static Object subtractTwoObject(Object a, Object b) {
        if (a == null) {
            return null;
        }
        if (b == null) {
            if (a instanceof java.lang.Number) {
                return null;
            }
            return a;
        }
        a = checkStringObject(a);
        b = checkStringObject(b);

        if (a instanceof java.lang.Number && b instanceof java.lang.Number) {
            int retType = Math.max(getJavaTypeOrder(a), getJavaTypeOrder(b));
            switch (retType) {
                case 1:
                    return NumberBaseOpt.castObjectToInteger(a) -
                        NumberBaseOpt.castObjectToInteger(b);
                case 2:
                    return NumberBaseOpt.castObjectToLong(a) -
                        NumberBaseOpt.castObjectToLong(b);
                case 5:
                    return NumberBaseOpt.castObjectToBigInteger(a).subtract(
                        NumberBaseOpt.castObjectToBigInteger(b));
                case 6:
                    return NumberBaseOpt.castObjectToBigDecimal(a).subtract(
                        NumberBaseOpt.castObjectToBigDecimal(b));
                case 3:
                case 4:
                default:
                    return NumberBaseOpt.castObjectToDouble(a) -
                        NumberBaseOpt.castObjectToDouble(b);
            }
        } else if (a instanceof Date) {
            if (b instanceof Date) {
                return DatetimeOpt.calcDateSpan((Date) a, (Date) b);
            } else if (NumberBaseOpt.isNumber(b)) {
                float num = NumberBaseOpt.castObjectToFloat(b, 0.f);
                return DatetimeOpt.addDays((Date) a, 0 - num);
            }
        } else if (a instanceof Collection) {
            List<Object> objs = new ArrayList<>();
            if (b instanceof Collection) {
                objs.addAll((Collection<Object>) a);
                objs.removeAll((Collection<Object>) b);
            } else {
                for (Object obj : (Collection<Object>) a) {
                    objs.add(subtractTwoObject(obj, b));
                }
            }
            return objs;
        }
        return a;
    }

    public static Object multiplyTwoObject(Object a, Object b) {
        if (a == null || b == null)
            return null;

        a = checkStringObject(a);
        b = checkStringObject(b);

        if (a instanceof java.lang.Number && b instanceof java.lang.Number) {
            int retType = Math.max(getJavaTypeOrder(a), getJavaTypeOrder(b));
            switch (retType) {
                case 1:
                    return NumberBaseOpt.castObjectToInteger(a) *
                        NumberBaseOpt.castObjectToInteger(b);
                case 2:
                    return NumberBaseOpt.castObjectToLong(a) *
                        NumberBaseOpt.castObjectToLong(b);
                case 5:
                    return NumberBaseOpt.castObjectToBigInteger(a).multiply(
                        NumberBaseOpt.castObjectToBigInteger(b));
                case 6:
                    return NumberBaseOpt.castObjectToBigDecimal(a).multiply(
                        NumberBaseOpt.castObjectToBigDecimal(b));
                case 3:
                case 4:
                default:
                    return NumberBaseOpt.castObjectToDouble(a) *
                        NumberBaseOpt.castObjectToDouble(b);
            }
        } else if (a instanceof Collection) {
            List<Object> objs = new ArrayList<>();
            if (b instanceof Collection) {
                List<Object> aobjs = new ArrayList<>();
                aobjs.addAll((Collection<Object>) a);
                for (Object obj : (Collection<Object>) b) {
                    if (aobjs.contains(obj)) {
                        objs.add(obj);
                    }
                }
            } else {
                for (Object obj : (Collection<Object>) a) {
                    objs.add(multiplyTwoObject(obj, b));
                }
            }
            return objs;
        } else if (b instanceof java.lang.Number) {
            int bi = NumberBaseOpt.castObjectToInteger(b);
            return StringUtils.repeat(StringBaseOpt.castObjectToString(a), bi);
        }
        return StringBaseOpt.concat(
            StringBaseOpt.castObjectToString(a),
            StringBaseOpt.castObjectToString(b));
    }

    public static Object divideTwoObject(Object a, Object b) {
        if (a == null || b == null)
            return null;
        a = checkStringObject(a);
        b = checkStringObject(b);

        if (a instanceof java.lang.Number && b instanceof java.lang.Number) {
            BigDecimal dbop2 = NumberBaseOpt.castObjectToBigDecimal(b);
            if (dbop2 == null || dbop2.compareTo(BigDecimal.ZERO) == 0)
                return null;
            int retType = Math.max(getJavaTypeOrder(a), getJavaTypeOrder(b));
            switch (retType) {
                case 1:
                    return NumberBaseOpt.castObjectToInteger(a) /
                        NumberBaseOpt.castObjectToInteger(b);
                case 2:
                    return NumberBaseOpt.castObjectToLong(a) /
                        NumberBaseOpt.castObjectToLong(b);
                case 5:
                    return NumberBaseOpt.castObjectToBigInteger(a).divide(
                        NumberBaseOpt.castObjectToBigInteger(b));
                case 6:
                    //默认采用8位有效数字
                    return NumberBaseOpt.castObjectToBigDecimal(a).divide(
                        NumberBaseOpt.castObjectToBigDecimal(b), 20, RoundingMode.HALF_EVEN);
                case 3:
                case 4:
                default:
                    return NumberBaseOpt.castObjectToDouble(a) /
                        NumberBaseOpt.castObjectToDouble(b);
            }
        } else if (a instanceof Collection) {
            List<Object> objs = new ArrayList<>();
            for (Object obj : (Collection<Object>) a) {
                objs.add(divideTwoObject(obj, b));
            }
            return objs;
        }
        return a;
    }

    public static Object modTwoObject(Object a, Object b) {
        if (a == null || b == null)
            return null;
        a = checkStringObject(a);
        b = checkStringObject(b);
        if (a instanceof java.lang.Number && b instanceof java.lang.Number) {
            int retType = Math.max(getJavaTypeOrder(a), getJavaTypeOrder(b));
            switch (retType) {
                case 1:
                    return NumberBaseOpt.castObjectToInteger(a) %
                        NumberBaseOpt.castObjectToInteger(b);
                case 2:
                    return NumberBaseOpt.castObjectToLong(a) %
                        NumberBaseOpt.castObjectToLong(b);
                case 3:
                    return NumberBaseOpt.castObjectToFloat(a) %
                        NumberBaseOpt.castObjectToFloat(b);
                case 5:
                    return NumberBaseOpt.castObjectToBigInteger(a).mod(
                        NumberBaseOpt.castObjectToBigInteger(b));
                case 4:
                case 6:
                default:
                    return NumberBaseOpt.castObjectToDouble(a) %
                        NumberBaseOpt.castObjectToDouble(b);
            }
        }
        return a;
    }

    public static Object maxObject(Collection<Object> ar) {
        if (ar == null || ar.isEmpty())
            return null;

        Iterator<Object> ari = ar.iterator();
        Object maxObject = ar.iterator().next();
        if (ar.size() == 1)
            return maxObject;

        int retType = maxObject==null?-1:getJavaTypeOrder(maxObject);;
        while (ari.hasNext()) {
            Object anOther = ari.next();
            if(anOther == null){
                continue;
            }
            if(maxObject==null){
                maxObject = anOther;
                retType = getJavaTypeOrder(maxObject);
            }  else {
                retType = Math.max(retType, getJavaTypeOrder(anOther));
                switch (retType) {
                    case 1:
                        maxObject = Math.max(NumberBaseOpt.castObjectToInteger(maxObject),
                            NumberBaseOpt.castObjectToInteger(anOther));
                        break;
                    case 2:
                        maxObject = Math.max(NumberBaseOpt.castObjectToLong(maxObject),
                            NumberBaseOpt.castObjectToLong(anOther));
                        break;
                    case 3:
                        maxObject = Math.max(NumberBaseOpt.castObjectToFloat(maxObject),
                            NumberBaseOpt.castObjectToFloat(anOther));
                        break;
                    case 5:
                        maxObject = NumberBaseOpt.castObjectToBigInteger(maxObject).max(
                            NumberBaseOpt.castObjectToBigInteger(anOther));
                        break;

                    case 6:
                        maxObject = NumberBaseOpt.castObjectToBigDecimal(maxObject).max(
                            NumberBaseOpt.castObjectToBigDecimal(anOther));
                        break;
                    case 4:
                        maxObject = Math.max(NumberBaseOpt.castObjectToDouble(maxObject),
                            NumberBaseOpt.castObjectToDouble(anOther));
                        break;
                    case 10:
                    default:
                        String str1 = StringBaseOpt.castObjectToString(maxObject);
                        String str2 = StringBaseOpt.castObjectToString(anOther);
                        maxObject = str1.compareTo(str2) > 0 ? str1 : str2;
                        break;
                }
            }
        }
        return maxObject;
    }

    public static Object minObject(Collection<Object> ar) {
        if (ar == null || ar.size() < 1)
            return null;
        Iterator<Object> ari = ar.iterator();
        Object minObject = ar.iterator().next();
        if (ar.size() == 1)
            return minObject;

        int retType = minObject==null?-1:getJavaTypeOrder(minObject);;
        while (ari.hasNext()) {
            Object anOther = ari.next();
            if(anOther == null){
                continue;
            }
            if(minObject==null){
                minObject = anOther;
                retType = getJavaTypeOrder(minObject);
            }  else {
                retType = Math.max(retType, getJavaTypeOrder(anOther));
                switch (retType) {
                    case 1:
                        minObject = Math.min(NumberBaseOpt.castObjectToInteger(minObject),
                            NumberBaseOpt.castObjectToInteger(anOther));
                        break;
                    case 2:
                        minObject = Math.min(NumberBaseOpt.castObjectToLong(minObject),
                            NumberBaseOpt.castObjectToLong(anOther));
                        break;
                    case 3:
                        minObject = Math.min(NumberBaseOpt.castObjectToFloat(minObject),
                            NumberBaseOpt.castObjectToFloat(anOther));
                        break;
                    case 5:
                        minObject = NumberBaseOpt.castObjectToBigInteger(minObject).min(
                            NumberBaseOpt.castObjectToBigInteger(anOther));
                        break;

                    case 6:
                        minObject = NumberBaseOpt.castObjectToBigDecimal(minObject).min(
                            NumberBaseOpt.castObjectToBigDecimal(anOther));
                        break;
                    case 4:
                        minObject = Math.min(NumberBaseOpt.castObjectToDouble(minObject),
                            NumberBaseOpt.castObjectToDouble(anOther));
                        break;
                    case 10:
                    default:
                        String str1 = StringBaseOpt.castObjectToString(minObject);
                        String str2 = StringBaseOpt.castObjectToString(anOther);
                        minObject = str1.compareTo(str2) < 0 ? str1 : str2;
                        break;
                }
            }
        }
        return minObject;
    }


    public static Object sumObjects(Collection<Object> ar) {
        if (ar.size() < 1)
            return null;
        Iterator<Object> ari = ar.iterator();

        Object sumObj = null;
        while(sumObj==null  && ari.hasNext()){
            sumObj = ari.next();
        }
        if (sumObj == null) {
            return null;
        }
        int retType = getJavaTypeOrder(sumObj);

        while (ari.hasNext()) {
            Object anOther = ari.next();
            if(anOther == null){
                continue;
            }
            retType = Math.max(retType, getJavaTypeOrder(anOther));
            switch (retType) {
                case 1:
                    sumObj = NumberBaseOpt.castObjectToInteger(sumObj,0) +
                        NumberBaseOpt.castObjectToInteger(anOther,0);
                    break;
                case 2:
                    sumObj = NumberBaseOpt.castObjectToLong(sumObj,0l) +
                        NumberBaseOpt.castObjectToLong(anOther,0l);
                    break;
                case 3:
                    sumObj = NumberBaseOpt.castObjectToFloat(sumObj,0f) +
                        NumberBaseOpt.castObjectToFloat(anOther,0f);
                    break;
                case 5:
                    sumObj = NumberBaseOpt.castObjectToBigInteger(sumObj, BigInteger.ZERO).add(
                        NumberBaseOpt.castObjectToBigInteger(anOther, BigInteger.ZERO));
                    break;
                case 6:
                    sumObj = NumberBaseOpt.castObjectToBigDecimal(sumObj, BigDecimal.ZERO).add(
                        NumberBaseOpt.castObjectToBigDecimal(anOther, BigDecimal.ZERO));
                    break;
                case 4:
                    sumObj = NumberBaseOpt.castObjectToDouble(sumObj, 0d) +
                        NumberBaseOpt.castObjectToDouble(anOther,0d);
                    break;
                case 10:
                default:
                    sumObj = StringBaseOpt.concat(
                        StringBaseOpt.castObjectToString(sumObj),
                        StringBaseOpt.castObjectToString(anOther));
                    break;
            }
        }
        return sumObj;
    }

    public static Object castObjectToType(Object obj, Class<?> type) {
        return TypeUtils.cast(obj, type);
    }

    /**
     * 功能：判断对象是否为空 类似于 CollectionUtils.isEmpty(Object obj)
     * @param obj 被判断的对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if(obj == null)
            return true;
        if(obj instanceof String)
            return StringUtils.isBlank((String)obj);
        if (obj instanceof Map) {
            Map<?, ?> objMap = (Map<?, ?>) obj;
            return objMap.isEmpty();
        } else  if (obj instanceof Collection) {
            Collection<?> objlist = (Collection<?>) obj;
            if(objlist.isEmpty())
                return true;
            for(Object obj1 : objlist){
                if(!isEmpty(obj1))
                    return false;
            }
            return true;
        } else if (obj instanceof Object[]) {
            Object[] objs = (Object[]) obj;
            for(Object obj1 : objs){
                if(!isEmpty(obj1))
                    return false;
            }
            return true;
        } else if(obj instanceof Supplier){
            Object retObj = ((Supplier<?>)obj).get();
            return isEmpty(retObj);
        } /*else if(obj instanceof ScriptObjectMirror){
           //ScriptObjectMirror implements Map<String, Object>
            ScriptObjectMirror jsObj = (ScriptObjectMirror) obj;
            return jsObj.isEmpty();
        }*/ else{
            return StringUtils.isBlank(StringBaseOpt.castObjectToString(obj));
        }

    }
}
