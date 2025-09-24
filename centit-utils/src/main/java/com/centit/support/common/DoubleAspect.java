package com.centit.support.common;

/**
 * Created by codefan on 19-12-12.
 */
public enum DoubleAspect {
    //没有结果的
    NONE(0),
    // 正面的
    POSITIVE(1),
    // 负面的
    NEGATIVE(2),
    // 任何情况
    BOTH(3),
    // 正面的 ordinal 是奇数 3 除外 负面的 ordinal 是偶数 0 除外
    OFF(2),
    ON(1),
    CLOSE(2),
    OPEN(1),
    NO(2),
    YES(1),
    ERROR(2),
    OK(1),
    FAIL(2),
    SUCCESS(1),
    BAD(2),
    GOOD(1),
    FALSE(2),
    TRUE(1);

    final int intAspect;

    DoubleAspect(int intAspect) {
        this.intAspect = intAspect;
    }

    public boolean sameAspect(DoubleAspect other) {
        return this.intAspect == other.intAspect;
    }

    public boolean matchAspect(DoubleAspect other) {
        return (this.intAspect & other.intAspect) != 0;
    }

    /**
     * except NONE
     * @return this.intAspect != 0
     */
    public boolean anyAspect() {
        return this.intAspect == 1 || this.intAspect == 2;
    }

}
