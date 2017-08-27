package com.centit.support.database.orm;

import com.centit.support.database.metadata.TableInfo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by codefan on 17-8-27.
 */
public class JpaMetadata {
    public static final ConcurrentHashMap<String , TableInfo> jpaMetaData =
            new ConcurrentHashMap<>(100);

}
