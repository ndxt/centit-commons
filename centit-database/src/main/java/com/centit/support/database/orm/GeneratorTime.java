package com.centit.support.database.orm;

/**
 * Created by codefan on 17-8-29.
 */
public enum GeneratorTime {
    // 在新建的时候生成
    NEW(1),
    //在修改式生成
    UPDATE(2),
    //在查询时生成
    READ(4),
    // 在new 和 update 的时候都保存
    NEW_UPDATE(3),
    //任何时候都生成 READ_NEW_UPDATE
    ALWAYS(7);

    int generatorTime;

    GeneratorTime(int generatorTime) {
        this.generatorTime = generatorTime;
    }

    public boolean matchTime(GeneratorTime other) {
        return (this.generatorTime & other.generatorTime) != 0;
    }
}
