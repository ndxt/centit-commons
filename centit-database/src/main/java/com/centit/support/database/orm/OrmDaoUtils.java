package com.centit.support.database.orm;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.jsonmaptable.GeneralJsonObjectDao;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableReference;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by codefan on 17-8-29.
 */
@SuppressWarnings("unused")
public abstract class OrmDaoUtils {
    private static final Logger logger = LoggerFactory.getLogger(OrmDaoUtils.class);

    private OrmDaoUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * MySql使用存储过程来模拟序列的
     * 获取 Sequence 的值
     *
     * @param connection   数据库连接
     * @param sequenceName 序列名称
     * @return 序列值
     */
    public static Long getSequenceNextValue(Connection connection, final String sequenceName) {
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection)
                .getSequenceNextValue(sequenceName);
        } catch (SQLException | IOException e) {
            throw new PersistenceException(e);
        }
    }

    public static JsonObjectDao getJsonObjectDao(Connection connection, TableMapInfo mapInfo) {
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int saveNewObject(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForInsert(object, mapInfo, sqlDialect);
            return sqlDialect.saveNewObject(OrmUtils.fetchObjectDatabaseField(object, mapInfo));
        } catch (IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int updateObject(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object, mapInfo, sqlDialect);

            return sqlDialect.updateObject(OrmUtils.fetchObjectDatabaseField(object, mapInfo));
        } catch (IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * 只更改对象object的部分属性 fields
     *
     * @param connection 数据库连接
     * @param fields     需要修改的属性
     * @param object     修改的对象，主键必须有值
     * @param <T>        对象类型
     * @return 更改的记录数
     * @throws PersistenceException 运行时异常
     */
    public static <T> int updateObject(Connection connection, Collection<String> fields, T object)
        throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object, mapInfo, sqlDialect);

            return sqlDialect.updateObject(fields, OrmUtils.fetchObjectDatabaseField(object, mapInfo));
        } catch (IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * 批量修改 对象
     *
     * @param connection       数据库连接
     * @param fields           需要修改的属性，对应的值从 object 对象中找
     * @param object           对应 fields 中的属性必须有值，如果没有值 将被设置为null
     * @param propertiesFilter 过滤条件对
     * @param <T>              类型
     * @return 更改的条数
     * @throws PersistenceException 运行时异常
     */
    public static <T> int batchUpdateObject(
        Connection connection, Collection<String> fields, T object,
        Map<String, Object> propertiesFilter)
        throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object, mapInfo, sqlDialect);

            return sqlDialect.updateObjectsByProperties(
                fields,
                OrmUtils.fetchObjectDatabaseField(object, mapInfo),
                propertiesFilter);
        } catch (IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * 批量修改 对象
     *
     * @param connection       数据库连接
     * @param type             对象类型
     * @param propertiesValue  值对
     * @param propertiesFilter 过滤条件对
     * @return 更改的条数
     * @throws PersistenceException 运行时异常
     */
    public static int batchUpdateObject(
        Connection connection, Class<?> type,
        Map<String, Object> propertiesValue,
        Map<String, Object> propertiesFilter)
        throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);

            return sqlDialect.updateObjectsByProperties(
                propertiesValue.keySet(),
                propertiesValue,
                propertiesFilter);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int mergeObject(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForMerge(object, mapInfo, sqlDialect);
            return sqlDialect.mergeObject(OrmUtils.fetchObjectDatabaseField(object, mapInfo));
        } catch (IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * 查询数据库模板代码
     *
     * @param conn          数据库链接
     * @param sqlAndParams  命名查询语句
     * @param fetchDataWork 获取数据的方法
     * @param <T>           返回类型嗯
     * @return 返回结果
     * @throws PersistenceException 异常
     */

    private final static <T> T queryParamsSql(Connection conn, QueryAndParams sqlAndParams,
                                              FetchDataWork<T> fetchDataWork)
        throws PersistenceException {
        QueryLogUtils.printSql(logger, sqlAndParams.getQuery(), sqlAndParams.getParams());
        try (PreparedStatement stmt = conn.prepareStatement(sqlAndParams.getQuery())) {
            DatabaseAccess.setQueryStmtParameters(stmt, sqlAndParams.getParams());
            try (ResultSet rs = stmt.executeQuery()) {
                return fetchDataWork.execute(rs);
            }
            //rs.close();
            //stmt.close();
            //return obj;
        } catch (SQLException e) {
            throw new PersistenceException(sqlAndParams.getQuery(), e);
        } catch (IOException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            throw new PersistenceException(PersistenceException.ILLEGALACCESS_EXCEPTION, e);
        }
    }

    private final static <T> T queryParamsSql(Connection conn, QueryAndParams sqlAndParams,
                                              int startPos, int maxSize, FetchDataWork<T> fetchDataWork)
        throws PersistenceException {
        sqlAndParams.setQuery(QueryUtils.buildLimitQuerySQL(
            sqlAndParams.getQuery(), startPos, maxSize, false, DBType.mapDBType(conn)
        ));
        return queryParamsSql(conn, sqlAndParams, fetchDataWork);
    }

    /**
     * 查询数据库模板代码
     *
     * @param conn          数据库链接
     * @param sqlAndParams  命名查询语句
     * @param fetchDataWork 获取数据的方法
     * @param <T>           返回类型嗯
     * @return 返回结果
     * @throws PersistenceException 异常
     */
    private static <T> T queryNamedParamsSql(Connection conn, QueryAndNamedParams sqlAndParams,
                                             FetchDataWork<T> fetchDataWork)
        throws PersistenceException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(sqlAndParams);
        return queryParamsSql(conn, qap, fetchDataWork);
    }

    private static <T> T queryNamedParamsSql(Connection conn, QueryAndNamedParams sqlAndParams,
                                             int startPos, int maxSize, FetchDataWork<T> fetchDataWork)
        throws PersistenceException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(sqlAndParams);
        return queryParamsSql(conn, qap, startPos, maxSize, fetchDataWork);
    }

    public static <T> T getObjectBySql(Connection connection, String sql, Map<String, Object> properties, Class<T> type)
        throws PersistenceException {
        //JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql,
                properties),
            (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type)
        );
    }

    public static <T> T getObjectByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String, TableField[]> q =
            GeneralJsonObjectDao.buildFieldSqlWithFields(mapInfo, null, false,
                GeneralJsonObjectDao.buildFilterSql(mapInfo, null, properties.keySet()), false, null);

        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(q.getLeft(),
                properties),
            (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type, q.getRight()));
    }

    public static <T> T getObjectById(Connection connection, Object id, final Class<T> type)
        throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        //Pair<String,String[]> q = GeneralJsonObjectDao.buildGetObjectSqlByPk(mapInfo, false);
        Pair<String, TableField[]> q =
            GeneralJsonObjectDao.buildFieldSqlWithFields(mapInfo, null, false,
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo, null), false, null);

        if (ReflectionOpt.isScalarType(id.getClass())) {
            if (mapInfo.countPkColumn() != 1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,
                    "表" + mapInfo.getTableName() + "不是单主键表，这个方法不适用。");
            return queryNamedParamsSql(connection, new QueryAndNamedParams(q.getKey(),
                    CollectionsOpt.createHashMap(mapInfo.getPkFields().get(0).getPropertyName(), id)),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type, q.getRight()));
        } else {
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, idObj)) {
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,
                    "缺少主键对应的属性。");
            }
            return queryNamedParamsSql(connection,
                new QueryAndNamedParams(q.getKey(), idObj),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type, q.getRight()));
        }

    }

    public static <T> T getObjectExcludeLazyById(Connection connection, Object id, final Class<T> type)
        throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        String sql = "select " + GeneralJsonObjectDao.buildFieldSql(mapInfo, "", 1) +
            " from " + mapInfo.getTableName() + " where " +
            GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo, null);

        if (ReflectionOpt.isScalarType(id.getClass())) {
            if (mapInfo.countPkColumn() != 1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "表" + mapInfo.getTableName() + "不是单主键表，这个方法不适用。");
            return getObjectBySql(connection, sql,
                CollectionsOpt.createHashMap(mapInfo.getPkFields().get(0).getPropertyName(), id), type);

        } else {
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, idObj)) {
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "缺少主键对应的属性。");
            }
            return getObjectBySql(connection, sql, idObj, type);
        }

    }

    public static <T> T getObjectWithReferences(Connection connection, Object id, final Class<T> type)
        throws PersistenceException {

        T object = getObjectById(connection, id, type);
        fetchObjectReferences(connection, object);
        return object;
    }

    public static <T> T getObjectCascadeById(Connection connection, Object id, final Class<T> type, int depth)
        throws PersistenceException {

        T object = getObjectById(connection, id, type);
        fetchObjectReferencesCascade(connection, object, type, depth);
        return object;
    }

    private static int deleteObjectById(Connection connection, Map<String, Object> id, TableMapInfo mapInfo) throws PersistenceException {
        try {
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.deleteObjectById(id);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int deleteObjectById(Connection connection, Map<String, Object> id, Class<T> type) throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        return deleteObjectById(connection, id, mapInfo);
    }

    public static <T> int deleteObject(Connection connection, T object) throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);
        return deleteObjectById(connection, idMap, mapInfo);
    }

    public static <T> int deleteObjectById(Connection connection, Object id, Class<T> type)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        if (ReflectionOpt.isScalarType(id.getClass())) {
            if (mapInfo.countPkColumn() != 1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "表" + mapInfo.getTableName() + "不是单主键表，这个方法不适用。");
            return deleteObjectById(connection,
                CollectionsOpt.createHashMap(mapInfo.getPkFields().get(0).getPropertyName(), id),
                mapInfo);
        } else {
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, idObj)) {
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "缺少主键对应的属性。");
            }
            return deleteObjectById(connection, idObj, mapInfo);
        }
    }

    public static <T> List<T> listAllObjects(Connection connection, Class<T> type)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String, TableField[]> q =
            GeneralJsonObjectDao.buildFieldSqlWithFields(mapInfo, null, true,
                null, true, null);
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(q.getLeft(),
                new HashMap<>(1)),
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type, q.getRight()));
    }

    public static <T> List<T> listObjectsByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String, TableField[]> q =
            GeneralJsonObjectDao.buildFieldSqlWithFields(mapInfo, null, true,
                GeneralJsonObjectDao.buildFilterSql(mapInfo, null, properties.keySet()),
                true, GeneralJsonObjectDao.fetchSelfOrderSql(mapInfo, properties));
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(q.getLeft(),
                properties),
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type, q.getRight()));
    }

    public static <T> int countObjectByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        String countSql = GeneralJsonObjectDao.buildCountSqlByProperties(mapInfo, properties);
        try {
            return NumberBaseOpt.castObjectToInteger(
                DatabaseAccess.getScalarObjectQuery(connection, countSql, properties), 0);
        } catch (SQLException e) {
            throw new PersistenceException(countSql, e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> List<T> listObjectsByProperties(Connection connection, Map<String, Object> properties, Class<T> type,
                                                      final int startPos, final int maxSize)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String, TableField[]> q =
            GeneralJsonObjectDao.buildFieldSqlWithFields(mapInfo, null, true,
                GeneralJsonObjectDao.buildFilterSql(mapInfo, null, properties.keySet())
                , true, GeneralJsonObjectDao.fetchSelfOrderSql(mapInfo, properties));
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(q.getLeft(),
                properties), startPos, maxSize,
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type, q.getRight()));
    }

    public static <T> List<T> queryObjectsBySql(Connection connection, String sql, Class<T> type)
        throws PersistenceException {
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql,
                new HashMap<>()),
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByParamsSql(Connection connection, String sql, Object[] params, Class<T> type)
        throws PersistenceException {
        return queryParamsSql(
            connection, new QueryAndParams(sql, params),
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByNamedParamsSql(Connection connection, String sql,
                                                           Map<String, Object> params, Class<T> type)
        throws PersistenceException {
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql, params),
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsBySql(Connection connection, String sql, Class<T> type,
                                                int startPos, int maxSize)
        throws PersistenceException {
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql,
                new HashMap<>()), startPos, maxSize,
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByParamsSql(Connection connection, String sql, Object[] params, Class<T> type,
                                                      int startPos, int maxSize)
        throws PersistenceException {
        return queryParamsSql(
            connection, new QueryAndParams(sql, params), startPos, maxSize,
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByNamedParamsSql(Connection connection, String sql,
                                                           Map<String, Object> params, Class<T> type,
                                                           int startPos, int maxSize)
        throws PersistenceException {
        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql, params), startPos, maxSize,
            (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> T fetchObjectLazyColumn(Connection connection, T object, String columnName)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);
        if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, idMap)) {
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "缺少主键对应的属性。");
        }

        String sql = "select " + mapInfo.findFieldByName(columnName).getColumnName() +
            " from " + mapInfo.getTableName() + " where " +
            GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo, null);

        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql, idMap),
            (rs) -> OrmUtils.fetchFieldsFormResultSet(rs, object, mapInfo));
    }

    public static <T> T fetchObjectLazyColumns(Connection connection, T object)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        String fieldSql = GeneralJsonObjectDao.buildFieldSql(mapInfo, "", 2);
        if (StringUtils.isBlank(fieldSql)) {
            return object;
        }
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);
        if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, idMap)) {
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "缺少主键对应的属性。");
        }

        String sql = "select " + fieldSql +
            " from " + mapInfo.getTableName() + " where " +
            GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo, null);

        return queryNamedParamsSql(
            connection, new QueryAndNamedParams(sql, idMap),
            (rs) -> OrmUtils.fetchFieldsFormResultSet(rs, object, mapInfo));
    }

    private static <T> T innerFetchObjectReferencesCascade(Connection connection, T object, SimpleTableReference ref,
                                                           TableMapInfo mapInfo, int depth)
        throws PersistenceException {

        if (ref == null || ref.getReferenceColumns().size() < 1)
            return object;

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo(refType);
        if (refMapInfo == null)
            return object;

        Map<String, Object> properties = ref.fetchChildFk(object);

        List<?> refs = listObjectsByProperties(connection, properties, refType);

        if (refs != null && refs.size() > 0) {
            if (depth > 1) {
                for (Object refObject : refs) {
                    fetchObjectReferencesCascade(connection, refObject, refType, depth - 1);
                }
            }
            if (//ref.getReferenceFieldType().equals(refType) || oneToOne
                ref.getReferenceFieldType().isAssignableFrom(refType)) {
                ref.setObjectFieldValue(object, refs.get(0));
            } else if (Set.class.isAssignableFrom(ref.getReferenceFieldType())) {
                ref.setObjectFieldValue(object, new HashSet<>(refs));
            } else if (List.class.isAssignableFrom(ref.getReferenceFieldType())) {
                ref.setObjectFieldValue(object, refs);
            }
        }
        return object;
    }

    private static <T> T fetchObjectReference(Connection connection, T object, SimpleTableReference ref, TableMapInfo mapInfo)
        throws PersistenceException {
        return innerFetchObjectReferencesCascade(connection, object, ref, mapInfo, 1);
    }

    public static <T> T fetchObjectReferencesCascade(Connection connection, T object, Class<?> objType, int depth) {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if (mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                innerFetchObjectReferencesCascade(connection, object, ref, mapInfo, depth);
            }
        }
        return object;
    }

    public static <T> T fetchObjectReference(Connection connection, T object, String reference)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);

        return fetchObjectReference(connection, object, ref, mapInfo);
    }

    public static <T> T fetchObjectReferences(Connection connection, T object)
        throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if (mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                fetchObjectReference(connection, object, ref, mapInfo);
            }
        }
        return object;
    }

    public static <T> int deleteObjectByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
        throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.deleteObjectsByProperties(properties);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int deleteObjectReference(Connection connection, T object, SimpleTableReference ref)
        throws PersistenceException {

        if (ref == null || ref.getReferenceColumns().size() < 1)
            return 0;
        Class<?> refType = ref.getTargetEntityType();
        Map<String, Object> properties = ref.fetchChildFk(object);

        return deleteObjectByProperties(connection, properties, refType);
    }

    public static <T> int deleteObjectReference(Connection connection, T object, String reference)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return deleteObjectReference(connection, object, ref);
    }

    public static <T> int deleteObjectReferences(Connection connection, T object)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n = 0;
        if (mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += deleteObjectReference(connection, object, ref);
            }
        }
        return n;
    }

    public static <T> int deleteObjectWithReferences(Connection connection, T object)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);

        if (mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                deleteObjectReference(connection, object, ref);
            }
        }

        return deleteObjectById(connection, idMap, mapInfo);
    }

    public static <T> int deleteObjectCascade(Connection connection, T object, int depth)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);
        int res = deleteObjectById(connection, idMap, mapInfo);

        if (depth > 0 && mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                Map<String, Object> properties = ref.fetchChildFk(object);
                Class<?> refType = ref.getTargetEntityType();

                List<?> refs = listObjectsByProperties(connection, properties, refType);
                for (Object refObject : refs) {
                    deleteObjectCascade(connection, refObject, depth - 1);
                }
            }
        }
        return res;
    }

    public static <T> int deleteObjectWithReferencesById(Connection connection, Object id, final Class<T> type)
        throws PersistenceException {

        return deleteObjectWithReferences(connection, getObjectById(connection, id, type));
    }

    public static <T> int deleteObjectCascadeById(Connection connection, Object id, final Class<T> type, int depth)
        throws PersistenceException {
        return deleteObjectCascade(connection, getObjectById(connection, id, type), depth);
    }

    public static <T> int replaceObjectsAsTabulation(Connection connection, List<T> dbObjects, List<T> newObjects)
        throws PersistenceException {
        if (newObjects == null || newObjects.size() == 0) {
            if (dbObjects == null || dbObjects.size() == 0) {
                return 0;
            }
            for (T obj : dbObjects) {
                deleteObject(connection, obj);
            }
            return dbObjects.size();
        }
        Class<T> objType = (Class<T>) newObjects.get(0).getClass();
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(objType);
        Triple<List<T>, List<Pair<T, T>>, List<T>>
            comRes =
            CollectionsOpt.compareTwoList(dbObjects, newObjects,
                new OrmObjectComparator<>(mapInfo));
        int resN = 0;
        if (comRes.getLeft() != null) {
            for (T obj : comRes.getLeft()) {
                resN += saveNewObject(connection, obj);
            }
        }
        if (comRes.getRight() != null) {
            for (T obj : comRes.getRight()) {
                resN += deleteObject(connection, obj);
            }
        }
        if (comRes.getMiddle() != null) {
            for (Pair<T, T> pobj : comRes.getMiddle()) {
                resN += updateObject(connection, pobj.getRight());
            }
        }
        return resN;
    }

    public static <T> int replaceObjectsAsTabulation(Connection connection, List<T> newObjects,
                                                     final String propertyName, final Object propertyValue)
        throws PersistenceException {
        return replaceObjectsAsTabulation(connection, newObjects,
            CollectionsOpt.createHashMap(propertyName, propertyValue));
    }

    public static <T> int replaceObjectsAsTabulation(Connection connection, List<T> newObjects,
                                                     Map<String, Object> properties)
        throws PersistenceException {
        if (newObjects == null || newObjects.size() < 1)
            return 0;
        Class<T> objType = (Class<T>) newObjects.iterator().next().getClass();
        List<T> dbObjects = listObjectsByProperties(connection, properties, objType);
        return replaceObjectsAsTabulation(connection, dbObjects, newObjects);
    }

    private static <T> int innerSaveNewObjectReferenceCascade(Connection connection, T object,
                                                              SimpleTableReference ref, TableMapInfo mapInfo, int depth)
        throws PersistenceException {

        if (ref == null || ref.getReferenceColumns().size() < 1)
            return 0;

        Object newObj = ref.getObjectFieldValue(object);
        if (newObj == null) {
            return 0;
        }

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo(refType);
        if (refMapInfo == null)
            return 0;
        if (//ref.getReferenceFieldType().equals(refType) || oneToOne
            ref.getReferenceFieldType().isAssignableFrom(refType)) {
            for (Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()) {
                Object obj = mapInfo.getObjectFieldValue(object, ent.getKey());
                refMapInfo.setObjectFieldValue(newObj, ent.getValue(), obj);
            }
            saveNewObjectCascade(connection, newObj, depth - 1);
        } else if (newObj instanceof Collection) {
            for (Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()) {
                Object obj = mapInfo.getObjectFieldValue(object, ent.getKey());
                for (Object subObj : (Collection<Object>) newObj) {
                    refMapInfo.setObjectFieldValue(subObj, ent.getValue(), obj);
                }
            }
            for (Object subObj : (Collection<Object>) newObj) {
                saveNewObjectCascade(connection, subObj, depth - 1);
            }
        }
        return 1;
    }

    private static <T> int saveObjectReference(Connection connection, T object, SimpleTableReference ref, TableMapInfo mapInfo)
        throws PersistenceException {

        if (ref == null || ref.getReferenceColumns().size() < 1)
            return 0;

        Object newObj = ref.getObjectFieldValue(object);
        if (newObj == null) {
            return deleteObjectReference(connection, object, ref);
        }

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo(refType);
        if (refMapInfo == null)
            return 0;

        Map<String, Object> properties = ref.fetchChildFk(object);
        List<?> refs = listObjectsByProperties(connection, properties, refType);

        if (//ref.getReferenceFieldType().equals(refType) || oneToOne
            ref.getReferenceFieldType().isAssignableFrom(refType)) {

            for (Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()) {
                Object obj = mapInfo.getObjectFieldValue(object, ent.getKey());
                refMapInfo.setObjectFieldValue(newObj, ent.getValue(), obj);
            }

            if (refs != null && refs.size() > 0) {
                updateObject(connection, newObj);
            } else {
                saveNewObject(connection, newObj);
            }
        } else {
            List<Object> newListObj = Set.class.isAssignableFrom(ref.getReferenceFieldType()) ?
                new ArrayList<>((Set<?>) newObj) : (List<Object>) newObj;
            for (Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()) {
                Object obj = mapInfo.getObjectFieldValue(object, ent.getKey());
                for (Object subObj : newListObj) {
                    refMapInfo.setObjectFieldValue(subObj, ent.getValue(), obj);
                }
            }
            replaceObjectsAsTabulation(connection, (List<Object>) refs, newListObj);
        }
        return 1;
    }

    public static <T> int saveObjectReference(Connection connection, T object, String reference)
        throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return saveObjectReference(connection, object, ref, mapInfo);
    }

    public static <T> int saveObjectReferences(Connection connection, T object)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n = 0;
        if (mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += saveObjectReference(connection, object, ref, mapInfo);
            }
        }
        return n;
    }

    public static <T> int saveNewObjectWithReferences(Connection connection, T object)
        throws PersistenceException {
        return saveNewObject(connection, object)
            + saveObjectReferences(connection, object);
    }

    public static <T> int saveNewObjectCascade(Connection connection, T object, int depth)
        throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n = saveNewObject(connection, object);

        if (depth > 0 && mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += innerSaveNewObjectReferenceCascade(connection, object, ref, mapInfo, depth);
            }
        }
        return n;
    }

    public static <T> int updateObjectWithReferences(Connection connection, T object)
        throws PersistenceException {
        return updateObject(connection, object)
            + saveObjectReferences(connection, object);
    }

    private static <T> int innerReplaceObjectsAsTabulationCascade(Connection connection, List<T> dbObjects,
                                                                  List<T> newObjects, int depth)
        throws PersistenceException {

        if (newObjects == null || newObjects.size() == 0) {
            if (dbObjects == null || dbObjects.size() == 0) {
                return 0;
            }
            for (T obj : dbObjects) {
                deleteObjectCascade(connection, obj, depth - 1);
            }
            return dbObjects.size();
        }

        Class<T> objType = (Class<T>) newObjects.get(0).getClass();
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(objType);
        Triple<List<T>, List<Pair<T, T>>, List<T>>
            comRes =
            CollectionsOpt.compareTwoList(dbObjects, newObjects,
                new OrmObjectComparator<>(mapInfo));
        int resN = 0;
        if (comRes.getLeft() != null) {
            for (T obj : comRes.getLeft()) {
                resN += saveNewObjectCascade(connection, obj, depth - 1);
            }
        }
        if (comRes.getRight() != null) {
            for (T obj : comRes.getRight()) {
                resN += deleteObjectCascade(connection, obj, depth - 1);
            }
        }
        if (comRes.getMiddle() != null) {
            for (Pair<T, T> pobj : comRes.getMiddle()) {
                resN += updateObjectCascade(connection, pobj.getRight(), depth - 1);
            }
        }
        return resN;
    }

    private static <T> int innerUpdateObjectReferenceCascade(Connection connection, T object,
                                                             SimpleTableReference ref, TableMapInfo mapInfo, int depth)
        throws PersistenceException {

        if (ref == null || ref.getReferenceColumns().size() < 1)
            return 0;

        Object newObj = ref.getObjectFieldValue(object);
        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo(refType);
        if (refMapInfo == null)
            return 0;

        Map<String, Object> properties = ref.fetchChildFk(object);

        int n = 0;
        List<?> refs = listObjectsByProperties(connection, properties, refType);
        if (newObj == null) {
            if (refs != null && refs.size() > 0) {
                if (//ref.getReferenceFieldType().equals(refType) || oneToOne
                    ref.getReferenceFieldType().isAssignableFrom(refType)) {
                    n += deleteObjectCascade(connection, refs.get(0), depth);
                } else {
                    for (Object subObj : refs) {
                        n += deleteObjectCascade(connection, subObj, depth);
                    }
                }
            }
            return n;
        }

        if (//ref.getReferenceFieldType().equals(refType) || oneToOne
            ref.getReferenceFieldType().isAssignableFrom(refType)) {
            if (refs != null && refs.size() > 0) {
                updateObjectCascade(connection, newObj, depth);
            } else {
                saveNewObjectCascade(connection, newObj, depth);
            }
        } else if (Set.class.isAssignableFrom(ref.getReferenceFieldType())) {
            innerReplaceObjectsAsTabulationCascade(connection, (List<Object>) refs,
                new ArrayList<>((Set<?>) newObj), depth);
        } else if (List.class.isAssignableFrom(ref.getReferenceFieldType())) {
            innerReplaceObjectsAsTabulationCascade(connection, (List<Object>) refs,
                (List<Object>) newObj, depth);
        }

        return 1;
    }

    public static <T> int updateObjectCascade(Connection connection, T object, int depth) throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n = updateObject(connection, object);
        if (depth > 0 && mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += innerUpdateObjectReferenceCascade(connection, object, ref, mapInfo, depth);
            }
        }
        return n;
    }

    public static <T> int checkObjectExists(Connection connection, T object)
        throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> objectMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);

        if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, objectMap)) {
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "缺少主键对应的属性。");
        }
        String sql =
            "select count(*) as checkExists from " + mapInfo.getTableName()
                + " where " + GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, null);

        try {
            Long checkExists = NumberBaseOpt.castObjectToLong(
                DatabaseAccess.getScalarObjectQuery(connection, sql, objectMap));
            return checkExists == null ? 0 : checkExists.intValue();
        } catch (SQLException e) {
            throw new PersistenceException(sql, e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int fetchObjectsCount(Connection connection, Map<String, Object> properties, Class<T> type)
        throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.fetchObjectsCount(properties).intValue();
        } catch (SQLException | IOException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int fetchObjectsCount(Connection connection, String sql, Map<String, Object> properties)
        throws PersistenceException {
        try {
            return NumberBaseOpt.castObjectToInteger(
                DatabaseAccess.getScalarObjectQuery(connection, sql, properties));
        } catch (SQLException e) {
            throw new PersistenceException(sql, e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    private static <T> T prepareObjectForMerge(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return OrmUtils.prepareObjectForMerge(object, mapInfo, sqlDialect);
        } catch (IOException | SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public static <T> int mergeObjectWithReferences(Connection connection, T object)
        throws PersistenceException {
        object = prepareObjectForMerge(connection, object);
        int checkExists = checkObjectExists(connection, object);
        if (checkExists == 0) {
            return saveNewObjectWithReferences(connection, object);
        } else if (checkExists == 1) {
            return updateObjectWithReferences(connection, object);
        } else {
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "主键属性有误，返回多个条记录。");
        }
    }

    public static <T> int mergeObjectCascade(Connection connection, T object, int depth) throws PersistenceException {
        object = prepareObjectForMerge(connection, object);
        int checkExists = checkObjectExists(connection, object);
        if (checkExists == 0) {
            return saveNewObjectCascade(connection, object, depth);
        } else if (checkExists == 1) {
            return updateObjectCascade(connection, object, depth);
        } else {
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "主键属性有误，返回多个条记录。");
        }
    }

    public interface FetchDataWork<T> {
        T execute(ResultSet rs) throws SQLException, IOException, NoSuchFieldException,
            InstantiationException, IllegalAccessException;
    }

    /**
     * BaseDaoImpl 中有引用，所有必须是 public
     *
     * @param <T> T为持久化对象 必须 和 tableInfo 一致
     */
    public static class OrmObjectComparator<T> implements Comparator<T> {
        private TableMapInfo tableInfo;

        public OrmObjectComparator(TableMapInfo tableInfo) {
            this.tableInfo = tableInfo;
        }

        @Override
        public int compare(T o1, T o2) {
            for (TableField pkc : tableInfo.getPkFields()) {
                Object f1 = tableInfo.getObjectFieldValue(o1, (SimpleTableField) pkc);
                Object f2 = tableInfo.getObjectFieldValue(o2, (SimpleTableField) pkc);
                if (f1 == null) {
                    if (f2 != null)
                        return -1;
                } else {
                    if (f2 == null)
                        return 1;
                    if (ReflectionOpt.isNumberType(f1.getClass())) {
                        double db1 = ((Number) f1).doubleValue();
                        double db2 = ((Number) f2).doubleValue();
                        if (db1 > db2)
                            return 1;
                        if (db1 < db2)
                            return -1;
                    } else {
                        String s1 = StringBaseOpt.objectToString(f1);
                        String s2 = StringBaseOpt.objectToString(f2);
                        int nc = s1.compareTo(s2);
                        if (nc != 0)
                            return nc;
                    }
                }
            }
            return 0;
        }

    }
}
