package com.centit.support.algorithm;

import com.centit.support.common.ObjectException;
import org.apache.commons.lang3.StringUtils;

public abstract class EnumBaseOpt {

    public static <T> T ordinalToEnum(Class<T> enumType,
                                      int ordinal) {
        if (!enumType.isEnum()) {
            throw new ObjectException(enumType,
                enumType.getName() + " is not an Enum type.");
        }
        T[] values = enumType.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new ObjectException(enumType,
                "IndexOutOfBoundsException: Enum " +
                    enumType.getName() + "invalid ordinal");
        }
        return values[ordinal];
    }

    public static <T> T stringToEnum(Class<T> enumType,
                                                String name, boolean ignoreCase) {
        if (!enumType.isEnum()) {
            throw new ObjectException(enumType,
                enumType.getName() + " is not an Enum type.");
        }
        T[] values = enumType.getEnumConstants();
        for(T value : values){
            if(StringUtils.equals(((Enum<?>)value).name(), name)
             || (ignoreCase && StringUtils.equalsIgnoreCase(((Enum<?>)value).name(), name))){
                return value;
            }
        }
        return null;
    }

    public static <T> T stringToEnum(Class<T> enumType,
                                     String name){
        return stringToEnum(enumType, name, false);
    }

    public static int enumToOrdinal(Object enumObj) {
        if (!enumObj.getClass().isEnum()) {
            throw new ObjectException(enumObj,
                enumObj.getClass().getName() + " is not an object of Enum.");
        }
        return ((Enum<?>) enumObj).ordinal();
    }

    public static String enumToString(Object enumObj) {
        if (!enumObj.getClass().isEnum()) {
            throw new ObjectException(enumObj,
                enumObj.getClass().getName() + " is not an object of Enum.");
        }
        return ((Enum<?>) enumObj).name();
    }
}
