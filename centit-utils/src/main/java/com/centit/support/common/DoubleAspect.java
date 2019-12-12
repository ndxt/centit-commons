package com.centit.support.common;

/**
 * Created by codefan on 19-12-12.
 */
public enum DoubleAspect {
    //没有结果的
    NONE(0),
    // 正面的
    POSITIVE(1),
    ON(1),
    OPEN(1),
    YES(1),
    OK(1),
    SUCCESS(1),
    GOOD(1),
    // 负面的
    NEGATIVE(2),
    OFF(2),
    CLOSE(2),
    NO(2),
    ERROR(2),
    FAIL(2),
    BAD(2),
    // 任何情况
    BOTH(3);

    int intState;
    DoubleAspect(int intState){
        this.intState = intState;
    }

    public boolean sameAspect(DoubleAspect other) {
        return this.intState == other.intState;
    }

    public boolean matchAspect(DoubleAspect other){
        return (this.intState & other.intState) != 0;
    }
}
