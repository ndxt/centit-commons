package com.centit.support.database.metadata;

import com.alibaba.fastjson.annotation.JSONField;
import com.centit.support.database.utils.DBType;

import java.util.Map;

/**
 * 数据库基本信息
 */
public interface IDatabaseInfo {

    String getDatabaseCode();

    String getDatabaseName();

    String getOsId();

    String getDatabaseUrl();

    String getUsername();

    String getPassword();

    String getDatabaseDesc();

    @JSONField(serialize = false)
    String getClearPassword();

    Map<String, Object> getExtProps();

    default DBType getDBType() {
        return DBType.mapDBType(getDatabaseUrl());
    }

    default Object getExtProp(String key) {
        Map<String, Object> extProps = getExtProps();
        if(extProps==null){
            return null;
        }
        return extProps.get(key);
    }
}

