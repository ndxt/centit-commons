package com.centit.support.database.metadata;

import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.common.JavaBeanField;
import com.centit.support.database.utils.FieldType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SimpleTableField implements TableField {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTableField.class);

    private String propertyName;// 字段属性名称
    private String fieldLabelName;// 字段的中文名称 label ，PDM中的 Name 和 元数据表格中的Name对应
    private String columnType;// 数据库中的字段类型
    private String columnName;// 字段代码 PDM中的CODE
    private String columnComment;// 字段注释
    private String defaultValue;
    // 这个不是java 的类型，这个是 我们框架抽象出来的 field 类型，解决不同数据库的 columnType 不一样的问题
    //private String javaTypeFullName;
    private String fieldType;
    private boolean mandatory;
    private Integer  maxLength;//最大长度 Only used when sType=String
    private Integer  precision;//有效数据位数 Only used when sType=Long Number Float
    private Integer  scale;//精度 Only used when sType= Long Number Float
    private JavaBeanField beanField;
    private boolean lazyFetch;

    public void mapToMetadata(){
        //这个和下面的 mapToDatabaseType 不对称
        propertyName = FieldType.mapPropName(columnName);
        fieldType = FieldType.mapToFieldType(columnType,scale);
        lazyFetch = FieldType.TEXT.equals(fieldType) ||
            FieldType.BYTE_ARRAY.equals(fieldType) ||
            FieldType.JSON_OBJECT.equals(fieldType) ;

        if( (FieldType.LONG.equals(fieldType) || FieldType.DOUBLE.equals(fieldType))
                && maxLength <= 0 )
            maxLength = 8;
        if( (FieldType.DATE.equals(fieldType) || FieldType.DATETIME.equals(fieldType)
            || FieldType.TIMESTAMP.equals(fieldType))
                && maxLength <= 0 )
            maxLength = 7;
    }

    /**
     * java type's full name
     * @return String
     */
    @Override
    public Class<?> getJavaType(){
        if(beanField != null){
            return beanField.getFieldType();
        }
        return FieldType.mapToJavaType(this.fieldType);
    }

    public SimpleTableField()
    {
        mandatory = false;
        maxLength = 0;
        precision = 0;//有效数据位数 Only used when sType=Long Number Float
        scale = 0;//精度 Only used when sType= Long Number Float
    }
    /**
     * 字段属性名，是通过字段的code转化过来的
     * @return String
     */
    @Override
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String name) {
        propertyName = name;
    }
    /**
     * 字段属性java类别
     * @return String
     */
    @Override
    public String getFieldType() {
        if(StringUtils.isBlank(fieldType)) {
            return FieldType.mapToFieldType(columnType, scale);
        }
        return fieldType;
    }


    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }



    /**
     * 字段中文名，对应Pdm中的name
     * @return String
     */
    @Override
    public String getFieldLabelName() {
        return fieldLabelName;
    }

    /**
     * 字段中文名，对应Pdm中的name
     * @param desc String
     */
    public void setFieldLabelName(String desc) {
        fieldLabelName = desc;
    }

    /**
     * 字段代码，对应Pdm中的code
     * @return String
     */
    @Override
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param  column 字段代码，对应Pdm中的code
     */
    public void setColumnName(String column) {
        columnName = column;
    }

    /**
     * 字段描述，对应Pdm中的Comment
     * @return String
     */
    @Override
    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String comment) {
        columnComment = comment;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean notnull) {
        this.mandatory = notnull;
    }

    @Override
    public boolean isLazyFetch() {
        return lazyFetch;
    }

    public void setLazyFetch(boolean lazyFetch) {
        this.lazyFetch = lazyFetch;
    }

    public void setMandatory(String notnull) {
        mandatory = StringRegularOpt.isTrue(notnull);
    }

    public void setNullEnable(String nullEnable) {
        mandatory = StringRegularOpt.isFalse(nullEnable);
    }

    /**
     * 最大长度 Only used when sType=String
     * 这个和Precision其实可以共用一个字段
     * @return 最大长度
     */
    @Override
    public Integer getMaxLength() {
        return maxLength;
    }
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * 有效数据位数 Only used when sType=Long Number Float
     * 这个和maxlength其实可以共用一个字段
     * @return 有效数据位数
     */
    @Override
    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }
    /**
     * 精度 Only used when sType= Long Number Float
     * @return 精度
     */
    @Override
    public Integer getScale() {
        return scale;
    }
    public void setScale(Integer scale) {
        this.scale = scale;
    }
    /**
     * 字段属性在数据库表中的类型
     * @return String
     */
    @Override
    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String type) {
        if(type !=null){
            columnType = type.trim();
            int nPos = columnType.indexOf('(');
            if(nPos>0)
                columnType = columnType.substring(0,nPos);
        }
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setObjectField(Field objectField) {
        if(beanField==null)
            beanField = new JavaBeanField();
        beanField.setObjectField(objectField);
    }

    public void setObjectSetFieldValueFunc(Method objectSetFieldValueFunc) {
        if(beanField==null)
            beanField = new JavaBeanField();
        beanField.setSetFieldValueFunc(objectSetFieldValueFunc);
    }

    public void setObjectGetFieldValueFunc(Method objectGetFieldValueFunc) {
        if(beanField==null)
            beanField = new JavaBeanField();
        beanField.setGetFieldValueFunc(objectGetFieldValueFunc);
    }

    public void setObjectFieldValue(Object obj, Object fieldValue) {
        beanField.setObjectFieldValue(obj,fieldValue);
    }

    public Object getObjectFieldValue(Object obj) {
        return beanField.getObjectFieldValue(obj);
    }
}
