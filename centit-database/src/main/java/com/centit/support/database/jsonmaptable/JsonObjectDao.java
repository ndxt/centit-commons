package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.metadata.TableInfo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JsonObjectDao {

    TableInfo getTableInfo();
    /**
     * 单主键表
     * @param keyValue keyValue
     * @return JSONObject
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    JSONObject getObjectById(final Object keyValue) throws SQLException, IOException;
/*
     * 联合主键表
     * @param keyValues keyValues
     * @return JSONObject
     * @throws SQLException SQLException
     * @throws IOException IOException
    JSONObject getObjectById(final Map<String,Object> keyValues) throws SQLException, IOException;*/

    /**
     * 根据属性查询对象
     * @param properties properties
     * @return JSONObject
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    JSONObject getObjectByProperties(final Map<String, Object> properties) throws SQLException, IOException;

    /**
     *
     * @param properties properties
     * @return JSONArray
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    JSONArray listObjectsByProperties(final Map<String, Object> properties) throws SQLException, IOException;

    /**
     * 根据属性进行查询
     * @param properties properties
     * @param startPos startPos
     * @param maxSize maxSize
     * @return JSONArray
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    JSONArray listObjectsByProperties(final Map<String, Object> properties,
            final int startPos,final int maxSize) throws SQLException, IOException;
    /**
     * 根据属性进行并获取总数
     * @param properties properties
     * @return Long fetchObjectsCount
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    Long fetchObjectsCount(final Map<String, Object> properties)
            throws SQLException, IOException;
    /**
     * 获取Sequence的值，不支持的Sequence数据库可以用一个表或者存储过程来模拟
     * @param sequenceName sequenceName
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    Long getSequenceNextValue(final String sequenceName) throws SQLException,IOException;
    /*
     * 保存
     * @param object object
     */
    int saveNewObject(final Map<String, Object> object) throws SQLException;

    /**
     * 更改部分属性
     * @param fields 更改部分属性 属性名 集合，应为有的Map 不允许 值为null，这样这些属性 用map就无法修改为 null
     * @param object object
     * @return Long
     * @throws SQLException SQLException
     */
    int updateObject(final Collection<String> fields,  final Map<String, Object> object) throws SQLException;

    /**
     * 更改部分属性
     * @param object object
     * @return Long
     * @throws SQLException SQLException
     */
    int updateObject(final Map<String, Object> object) throws SQLException;

    /**
     *  更改部分属性
     * @param fields 更改部分属性 属性名 集合，应为有的Map 不允许 值为null，这样这些属性 用map就无法修改为 null
     * @param object object
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    int mergeObject(final Collection<String> fields,
            final Map<String, Object> object) throws SQLException, IOException;

    /**
     * 合并
     * @param object object
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    int mergeObject(final Map<String, Object> object) throws SQLException, IOException;

    /**
     * 根据条件批量更新 对象
     * @param fieldValues properties
     * @param properties properties
     * @return Long
     * @throws SQLException SQLException
     */
    int updateObjectsByProperties(final Map<String, Object> fieldValues,
            final Map<String, Object> properties) throws SQLException;

    /**
     * 根据条件批量更新 对象
     * @param fields 更改部分属性 属性名 集合，应为有的Map 不允许 值为null，这样这些属性 用map就无法修改为 null
     * @param fieldValues fieldValues
     * @param properties properties
     * @return Long
     * @throws SQLException SQLException
     */
    int updateObjectsByProperties(final Collection<String> fields,
            final Map<String, Object> fieldValues,
            final Map<String, Object> properties) throws SQLException;
    /**
     * 删除，单主键
     * @param keyValue keyValue
     * @return Long
     * @throws SQLException SQLException
     */
    int deleteObjectById(final Object keyValue) throws SQLException;

    /*
     * 删除，联合主键
     * @param keyValue keyValue
     * @return Long
     * @throws SQLException SQLException
     int deleteObjectById(final Map<String,Object> keyValue) throws SQLException;
     */
    /**
     * 根据属性 批量删除
     * @param properties properties
     * @return Long
     * @throws SQLException SQLException
     */
    int deleteObjectsByProperties(final Map<String, Object> properties)
            throws SQLException;
    //--- 作为子表批量操作
    /**
     * 批量添加多条记录
     * @param objects JSONArray
     * @return int
     * @throws SQLException SQLException
     */
    int insertObjectsAsTabulation(final List<Map<String,Object>> objects) throws SQLException;

    /**
     * 批量删除
     * @param objects JSONArray
     * @return int
     * @throws SQLException SQLException
     */
    int deleteObjects(final List<Object> objects) throws SQLException;

    /**
     * 根据外键批量删除，单外键
     * @param propertyName String
     * @param propertyValue Object
     * @return int
     * @throws SQLException SQLException
     */
    int deleteObjectsAsTabulation(final String propertyName,
            final Object propertyValue) throws SQLException;
    /**
     * 根据外键批量删除，符合外键
     * @param properties Object
     * @return int
     * @throws SQLException SQLException
     */
    int deleteObjectsAsTabulation(final Map<String, Object> properties) throws SQLException;


    /**
     * 用新的列表覆盖数据库中的列表
     * @param dbObjects JSONArray
     * @param newObjects JSONArray
     * @return int
     * @throws SQLException SQLException
     */
    int replaceObjectsAsTabulation(final List<Map<String,Object>> newObjects,final List<Map<String,Object>> dbObjects) throws SQLException;
    /**
     * 用新的列表覆盖数据库中的内容，通过单外键查询列表
     * @param newObjects JSONArray
     * @param propertyName String
     * @param propertyValue  Object
     * @return int
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    int replaceObjectsAsTabulation(final List<Map<String,Object>> newObjects,
            final String propertyName,
            final Object propertyValue) throws SQLException, IOException;
    /**
     * 用新的列表覆盖数据库中的内容，通过复合外键查询列表
     * @param newObjects newObjects
     * @param properties properties
     * @return int
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    int replaceObjectsAsTabulation(final List<Map<String,Object>> newObjects,
            final Map<String, Object> properties) throws SQLException, IOException;

    ///////////////////////////////////////////////////////////////////////////////////////
    /**
     * 查询带参数的SQL语句
     * @param sSql String sql语句
     * @param values Object [] 参数
     * @return List Object []
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    List<Object[]> findObjectsBySql(final String sSql,final Object[] values)
            throws SQLException, IOException;

    /**
     *
     * @param sSql String sql语句
     * @param values Object [] 参数
     * @param pageNo 当前页码
     * @param pageSize 每页大小
     * @return List Object []
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    List<Object[]> findObjectsBySql(final String sSql,final Object[] values,
            final int pageNo, final int pageSize)
            throws SQLException, IOException;

    List<Object[]> findObjectsByNamedSql(
            final String sSql, final Map<String, Object> values)
            throws SQLException, IOException;

    List<Object[]> findObjectsByNamedSql(
            final String sSql, final Map<String, Object> values,
            final int pageNo, final int pageSize)
            throws SQLException, IOException;

    JSONArray findObjectsAsJSON(final String sSql,final Object[] values,
            final String[] fieldnames)
            throws SQLException, IOException;

    JSONArray findObjectsAsJSON(final String sSql,final Object[] values,
            final String[] fieldnames,final int pageNo, final int pageSize)
            throws SQLException, IOException;

    JSONArray findObjectsByNamedSqlAsJSON(
            final String sSql, final Map<String, Object> values,final String[] fieldnames)
            throws SQLException, IOException;

    JSONArray findObjectsByNamedSqlAsJSON(
            final String sSql, final Map<String, Object> values,
            final String[] fieldnames,
            final int pageNo, final int pageSize)
            throws SQLException, IOException;

    boolean doExecuteSql(final String sSql) throws SQLException;

    int doExecuteSql(final String sSql,final Object[] values) throws SQLException;

    int doExecuteNamedSql(final String sSql, final Map<String, Object> values)
            throws SQLException;

}
