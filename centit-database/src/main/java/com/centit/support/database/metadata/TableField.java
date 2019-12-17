package com.centit.support.database.metadata;

public interface TableField {
    /**
     * @return 字段属性名，是通过字段的code转化过来的
     */
    String getPropertyName();

    /**
     * @return 字段属性Field类别, 是框架自定义的， 用于兼容不同的数据库
     */
    String getFieldType();

    /**
     * @return 字段属性java类别
     */
    Class<?> getJavaType();

    /**
     * @return 字段代码，对应Pdm中的code
     */
    String getColumnName();

    /**
     * @return 字段属性在数据库表中的类型
     */
    String getColumnType();

    /**
     * @return 字段中文名，对应Pdm中的name
     */
    String getFieldLabelName();

    /**
     * @return 字段描述，对应Pdm中的Comment
     */
    String getColumnComment();

    /**
     * @return 是否有 not null 约束
     */
    boolean isMandatory();

    /**
     * @return 是否是主键
     */
    boolean isPrimaryKey();

    /**
     * @return 是否是懒加载；
     * 获取单个对象时，一般加载所有字段，获取列表时默认不加载lazy字段
     * 一般 lob字段为懒加载
     */
    boolean isLazyFetch();

    /**
     * 最大长度 Only used when sType=String
     * 这个和Precision其实可以共用一个字段
     *
     * @return 最大长度
     */
    Integer getMaxLength();

    /**
     * 有效数据位数 Only used when sType=Long Number Float
     * 这个和maxlength其实可以共用一个字段
     *
     * @return 有效数据位数
     */
    Integer getPrecision();

    /**
     * 精度 Only used when sType= Long Number Float
     *
     * @return 精度
     */
    Integer getScale();

    /**
     * @return 字段的默认值
     */
    String getDefaultValue();

}
