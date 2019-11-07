package com.centit.support.database.orm;

import com.centit.support.common.LeftRightPair;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableInfo;
import com.centit.support.database.metadata.TableField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 17-8-29.
 */
public class TableMapInfo extends SimpleTableInfo {


    public List<LeftRightPair<String, ValueGenerator>> getValueGenerators() {
        return valueGenerators;
    }

    private List<LeftRightPair<String,ValueGenerator>> valueGenerators;

    public TableMapInfo addValueGenerator(String fieldName, ValueGenerator generator ){
        if(valueGenerators ==null)
            valueGenerators = new ArrayList<>(5);
        /*boolean add = */
        valueGenerators.add(new LeftRightPair<>(fieldName, generator));
        return this;
    }

    public void appendOrderBy(SimpleTableField column, String orderBy) {
        String orderBySql ;
        if( StringUtils.isBlank(orderBy) || "ASC".equalsIgnoreCase(orderBy) ){
            orderBySql = column.getColumnName();
        }else if("DESC".equalsIgnoreCase(orderBy)){
            // StringUtils.equalsAnyIgnoreCase(orderByTrim, "DESC", "ASC" )){
            orderBySql = column.getColumnName() + " DESC";
        }else{
            orderBySql = orderBy;
        }

        if( StringUtils.isBlank(this.getOrderBy()) ){
            super.setOrderBy( orderBySql);
        }else{
            super.setOrderBy(super.getOrderBy() +", " + orderBySql);
        }
    }
}
