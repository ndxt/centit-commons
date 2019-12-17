package com.centit.support.algorithm;

import com.centit.support.common.ObjectException;

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

    public static int enumToOrdinal(Object enumObj) {
        if (!enumObj.getClass().isEnum()) {
            throw new ObjectException(enumObj,
                enumObj.getClass().getName() + " is not an object of Enum.");
        }
        return ((Enum) enumObj).ordinal();
    }
}
