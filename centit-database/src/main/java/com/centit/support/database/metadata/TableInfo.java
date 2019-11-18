package com.centit.support.database.metadata;

import com.alibaba.fastjson.annotation.JSONField;
import com.centit.support.algorithm.StringBaseOpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TableInfo {
    /**
     * @return 数据库表名，对应pdm中的code，对应元数据中的 tabcode
     */
    String getTableName();

    /**
     * @return 数据库表中文名，对应pdm中的name,对应元数据中的 tabename
     */
    String getTableLabelName();

    /**
     * @return 数据库表备注信息，对应pdm中的Comment,对应元数据中的 tabdesc
     */
    String getTableComment();

    /**
     * @return 主键名称
     */
    String getPkName();

    /**
     * @return 表的模式名称
     */
    String getSchema();

    /**
     * @return 默认排序语句
     */
    String getOrderBy();
    /**
     * @return 根据属性名查找 字段信息
     * @param name 属性名
     */
    TableField findFieldByName(String name);
    /**
     *
     * @param name 属性名
     * @return 根据属性名查找 字段信息
     */
    TableField findFieldByColumn(String name);

    /**
     * 获取所有的列名
     * @return 所有的列名
     */
    List<? extends TableField> getColumns();

    /**
     * @return 获取引用信息（外键）但是数据库中不一定有对应的外键
     */
    List<? extends TableReference> getReferences();

    /**
     * 判断表是否定义单一主键
     * @return 否是定义单一主键
     */
    default int countPkColumn(){
        int pkColumn = 0;
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                if (field.isPrimaryKey()) {
                    pkColumn ++;
                }
            }
        }
        return pkColumn;
    }
    /**
     * 判断表是否定义主键
     * @return 否是定义主键
     */
    default boolean hasParmaryKey(){
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                if (field.isPrimaryKey()) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 判断一个字段是否是主键
     * @param colname 字段
     * @return 否是主键
     */
    default boolean isParmaryKey(String colname){
        TableField field = findFieldByColumn(colname);
        return field != null && field.isPrimaryKey();
    }

    @JSONField(serialize = false)
    default List<String> getAllFieldsName(){
        List<String> pkCols = new ArrayList<>(4);
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                pkCols.add(field.getPropertyName());
            }
        }
        return pkCols;
    }

    @JSONField(serialize = false)
    default List<String> getFieldsNameWithoutLazy(){
        List<String> pkCols = new ArrayList<>(4);
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                if(!field.isLazyFetch()) {
                    pkCols.add(field.getPropertyName());
                }
            }
        }
        return pkCols;
    }

    default Map<String, Object> fetchObjectPk(Map<String, Object> object){
        Map<String, Object> pk = new HashMap<>(8);
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField c : columns) {
                if (c.isPrimaryKey()) {
                    Object pkValue = object.get(c.getPropertyName());
                    if (pkValue == null) {
                        return null;
                    }
                    pk.put(c.getPropertyName(), pkValue);
                }
            }
        }
        return pk;
    }

    default String fetchObjectPkAsId(Map<String, Object> object){
        StringBuilder pkId = new StringBuilder();
        int pkNo = 0;
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField c : columns) {
                if (c.isPrimaryKey()) {
                    Object pkValue = object.get(c.getPropertyName());
                    if (pkNo > 0) {
                        pkId.append(":");
                    }
                    pkId.append(StringBaseOpt.castObjectToString(pkValue));
                    pkNo++;
                }
            }
        }
        return pkId.toString();
    }

    default Map<String, Object> parseObjectPkId(String pkId){
        String [] pkIds = pkId.split(":");
        int len = pkIds.length;
        Map<String, Object> pk = new HashMap<>(8);
        int pkNo = 0;
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField c : columns) {
                if (c.isPrimaryKey()) {
                    if (pkNo >= len) {
                        return null;
                    }
                    pk.put(c.getPropertyName(), pkIds[pkNo]);
                    pkNo++;
                }
            }
        }
        return pk;
    }

    /**
     * @return 获取主键列名 主键是有次序的
     */
    @JSONField(serialize = false)
    default List<? extends TableField> getPkFields(){
        List<TableField> pkCols = new ArrayList<>(4);
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                if (field.isPrimaryKey()) {
                    pkCols.add(field);
                }
            }
        }
        return pkCols;
    }

    @JSONField(serialize = false)
    default List<? extends TableField> getLzayFields(){
        List<TableField> pkCols = new ArrayList<>(4);
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                if (field.isLazyFetch()) {
                    pkCols.add(field);
                }
            }
        }
        return pkCols;
    }

    @JSONField(serialize = false)
    default List<? extends TableField> getMandatoryFields(){
        List<TableField> pkCols = new ArrayList<>(4);
        List<? extends TableField> columns = this.getColumns();
        if(columns!=null) {
            for (TableField field : columns) {
                if (field.isMandatory()) {
                    pkCols.add(field);
                }
            }
        }
        return pkCols;
    }
}
