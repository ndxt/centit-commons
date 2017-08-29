package com.centit.support.database.orm;

import com.centit.support.common.KeyValuePair;
import com.centit.support.database.metadata.SimpleTableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 17-8-29.
 */
public class TableMapInfo extends SimpleTableInfo {

    private List<KeyValuePair<String,ValueGenerator>> defaultValueGenerators;

    public TableMapInfo addValueGenerator(String fieldName, ValueGenerator generator ){
        if(defaultValueGenerators==null)
            defaultValueGenerators = new ArrayList<>(5);
        /*boolean add = */
        defaultValueGenerators.add(new KeyValuePair<>(fieldName, generator));
        return this;
    }
}
