package com.centit.support.database.metadata;

import com.alibaba.fastjson.annotation.JSONField;
import com.centit.support.database.utils.DBType;

/**
 * 数据库基本信息
 */
public interface IDatabaseInfo  {

    String getDatabaseName();

    String getOsId();

    String getDatabaseCode();

    String getDatabaseUrl();

    String getUsername();

    String getDatabaseDesc();

    String getPassword();

    @JSONField(serialize = false)
    String getClearPassword();

    default DBType getDBType(){
        return DBType.mapDBType(getDatabaseUrl());
    }
}

