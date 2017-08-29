package com.centit.support.database.orm;

import com.centit.support.common.KeyValuePair;
import com.centit.support.database.metadata.SimpleTableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 17-8-29.
 */
public class TableMapInfo extends SimpleTableInfo {

    public List<KeyValuePair<String, ValueGenerator>> getValueGenerators() {
        return valueGenerators;
    }

    private List<KeyValuePair<String,ValueGenerator>> valueGenerators;

    public TableMapInfo addValueGenerator(String fieldName, ValueGenerator generator ){
        if(valueGenerators ==null)
            valueGenerators = new ArrayList<>(5);
        /*boolean add = */
        valueGenerators.add(new KeyValuePair<>(fieldName, generator));
        return this;
    }
}
