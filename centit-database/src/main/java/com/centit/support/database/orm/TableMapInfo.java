package com.centit.support.database.orm;

import com.centit.support.common.KeyValuePair;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableInfo;
import com.centit.support.database.metadata.TableField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
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

    /**
     * 懒加载的字段，不能包括主键
     */
    private List<SimpleTableField> lazyColumns=null;


    public List<SimpleTableField> getLazyColumns() {
        if(lazyColumns==null)
            lazyColumns = new ArrayList<SimpleTableField>(20);
        return lazyColumns;
    }

    public void addLazyColumn(SimpleTableField column) {
        getLazyColumns().add(column);
    }

    /**
     * 根据属性名查找 字段信息
     * @param name 字段属性名
     * @return 字段信息
     */
    @Override
    public SimpleTableField findFieldByName(String name){
        for(Iterator<SimpleTableField> it = getColumns().iterator(); it.hasNext();){
            SimpleTableField col = it.next();
            if(col.getPropertyName().equals(name))
                return col;
        }
        if(lazyColumns!=null) {
            for (Iterator<SimpleTableField> it = lazyColumns.iterator(); it.hasNext(); ) {
                SimpleTableField col = it.next();
                if (col.getPropertyName().equals(name))
                    return col;
            }
        }

        for(Iterator<SimpleTableField> it = getColumns().iterator();it.hasNext();){
            SimpleTableField col = it.next();
            if(col.getColumnName().equals(name))
                return col;
        }
        if(lazyColumns!=null) {
            for(Iterator<SimpleTableField> it = lazyColumns.iterator();it.hasNext();){
                SimpleTableField col = it.next();
                if(col.getColumnName().equals(name))
                    return col;
            }
        }

        return null;
    }

    /**
     * 根据属性名查找 字段信息
     * @param name 属性名
     * @return 字段信息
     */
    @Override
    public SimpleTableField findFieldByColumn(String name){
        for(Iterator<SimpleTableField> it = getColumns().iterator();it.hasNext();){
            SimpleTableField col = it.next();
            if(col.getColumnName().equals(name))
                return col;
        }
        if(lazyColumns!=null) {
            for(Iterator<SimpleTableField> it = lazyColumns.iterator();it.hasNext();){
                SimpleTableField col = it.next();
                if(col.getColumnName().equals(name))
                    return col;
            }
        }

        for(Iterator<SimpleTableField> it = getColumns().iterator();it.hasNext();){
            SimpleTableField col = it.next();
            if(col.getPropertyName().equals(name))
                return col;
        }

        if(lazyColumns!=null) {
            for (Iterator<SimpleTableField> it = lazyColumns.iterator(); it.hasNext(); ) {
                SimpleTableField col = it.next();
                if (col.getPropertyName().equals(name))
                    return col;
            }
        }

        return null;
    }

    /**
     * 返回 sql 语句 和 属性名数组
     * @param alias String
     * @return Pair String String []
     */
    public String buildFieldIncludeLazySql(String alias){
        StringBuilder sBuilder= new StringBuilder();

        boolean addAlias = StringUtils.isNotBlank(alias);
        int i=0;
        for(TableField col : getColumns()){
            if(i>0)
                sBuilder.append(", ");
            else
                sBuilder.append(" ");
            if(addAlias)
                sBuilder.append(alias).append('.');
            sBuilder.append(col.getColumnName());

            i++;
        }
        if(lazyColumns!=null){
            for(TableField col : lazyColumns){
                sBuilder.append(", ");
                if(addAlias)
                    sBuilder.append(alias).append('.');
                sBuilder.append(col.getColumnName());
            }
        }
        return sBuilder.toString();
    }

}
