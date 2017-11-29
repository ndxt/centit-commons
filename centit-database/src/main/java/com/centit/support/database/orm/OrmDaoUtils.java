package com.centit.support.database.orm;

import com.alibaba.fastjson.JSON;
import com.centit.support.algorithm.ListOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.jsonmaptable.GeneralJsonObjectDao;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.metadata.SimpleTableReference;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.*;
import com.centit.support.json.JSONOpt;
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
    private OrmDaoUtils() {
        throw new IllegalAccessError("Utility class");
    }

    private static final Logger logger = LoggerFactory.getLogger(OrmDaoUtils.class);
    public static Long getSequenceNextValue(Connection connection, final String sequenceName) {
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection)
                    .getSequenceNextValue(sequenceName);
        } catch (SQLException| IOException e) {
            throw  new PersistenceException(e);
        }
    }

    public static JsonObjectDao getJsonObjectDao(Connection connection, TableMapInfo mapInfo){
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection,mapInfo);
        } catch (SQLException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int saveNewObject(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForInsert(object, mapInfo, sqlDialect);
            return sqlDialect.saveNewObject(OrmUtils.fetchObjectDatabaseField(object, mapInfo));
        }catch (NoSuchFieldException | IOException | SQLException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int updateObject(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );

            return sqlDialect.updateObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
        }catch (NoSuchFieldException | IOException | SQLException e){
            throw  new PersistenceException(e);
        }
    }


    public static <T> int updateObject(Connection connection, Collection<String> fields,  T object)
            throws PersistenceException  {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );

            return sqlDialect.updateObject(fields, OrmUtils.fetchObjectDatabaseField(object,mapInfo));
        }catch (NoSuchFieldException | IOException | SQLException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int batchUpdateObject(
            Connection connection, Collection<String> fields,   T object, Map<String, Object> properties)
            throws PersistenceException  {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );

            return sqlDialect.updateObjectsByProperties(
                    fields,
                    OrmUtils.fetchObjectDatabaseField(object,mapInfo),
                    properties);
        }catch (NoSuchFieldException | IOException | SQLException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int mergeObject(Connection connection, T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );
            return sqlDialect.mergeObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
        }catch (NoSuchFieldException | IOException | SQLException e){
            throw  new PersistenceException(e);
        }
    }

    public interface FetchDataWork<T> {
        T execute(ResultSet rs) throws SQLException, IOException,NoSuchFieldException,
                InstantiationException, IllegalAccessException;
    }
    /**
     * 查询数据库模板代码
     * @param conn 数据库链接
     * @param sqlAndParams 命名查询语句
     * @param fetchDataWork 获取数据的方法
     * @param <T> 返回类型嗯
     * @return 返回结果
     * @throws PersistenceException 异常
     */

    private final static <T> T queryParamsSql(Connection conn, QueryAndParams sqlAndParams ,
                                             FetchDataWork<T> fetchDataWork)
            throws PersistenceException {
        QueryLogUtils.printSql(logger, sqlAndParams.getQuery(), sqlAndParams.getParams() );
        try(PreparedStatement stmt = conn.prepareStatement(sqlAndParams.getQuery())){
            DatabaseAccess.setQueryStmtParameters(stmt,sqlAndParams.getParams());
            try(ResultSet rs = stmt.executeQuery()) {
                return fetchDataWork.execute(rs);
            }
            //rs.close();
            //stmt.close();
            //return obj;
        }catch (SQLException e) {
            throw  new PersistenceException(sqlAndParams.getQuery(), e);
        }catch (NoSuchFieldException | IOException | InstantiationException | IllegalAccessException e){
            throw  new PersistenceException(PersistenceException.ILLEGALACCESS_EXCEPTION,e);
        }
    }

    private final static <T> T queryParamsSql(Connection conn, QueryAndParams sqlAndParams ,
                                              int startPos, int maxSize, FetchDataWork<T> fetchDataWork)
            throws PersistenceException {
        sqlAndParams.setQuery(QueryUtils.buildLimitQuerySQL(
                sqlAndParams.getQuery(),  startPos , maxSize , false , DBType.mapDBType(conn)
            ));
        return queryParamsSql(conn,  sqlAndParams , fetchDataWork);
    }
    /**
     * 查询数据库模板代码
     * @param conn 数据库链接
     * @param sqlAndParams 命名查询语句
     * @param fetchDataWork 获取数据的方法
     * @param <T> 返回类型嗯
     * @return 返回结果
     * @throws PersistenceException 异常
     */
    private static <T> T queryNamedParamsSql(Connection conn, QueryAndNamedParams sqlAndParams,
                                                   FetchDataWork<T> fetchDataWork)
            throws PersistenceException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(sqlAndParams);
        return queryParamsSql(conn, qap ,fetchDataWork);
    }

    private static <T> T queryNamedParamsSql(Connection conn, QueryAndNamedParams sqlAndParams,
                                             int startPos, int maxSize, FetchDataWork<T> fetchDataWork)
            throws PersistenceException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(sqlAndParams);
        return queryParamsSql(conn, qap,  startPos, maxSize ,fetchDataWork);
    }


    public static <T> T getObjectBySql(Connection connection, String sql, Map<String, Object> properties, Class<T> type)
            throws PersistenceException{
        //JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type)
        );
    }

    public static <T> T getObjectById(Connection connection, Object id, final Class<T> type)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildGetObjectSqlByPk(mapInfo);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,
                        "表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(connection,q.getKey(),
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type);
        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,
                        "缺少主键对应的属性。");
            }
            return getObjectBySql(connection, q.getKey(),
                    idObj, type);
        }

    }

    public static <T> T getObjectIncludeLazyById(Connection connection, Object id, final Class<T> type)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        String  sql =  "select " + mapInfo.buildFieldIncludeLazySql("") +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(connection, sql,
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
            }
            return getObjectBySql(connection, sql, idObj, type);
        }

    }

    public static <T> T getObjectCascadeShallowById(Connection connection, Object id, final Class<T> type)
            throws PersistenceException {

        T object = getObjectById(connection, id, type);
        fetchObjectReferences(connection, object);
        return object;
    }

    public static <T> T getObjectCascadeById(Connection connection, Object id, final Class<T> type)
            throws PersistenceException {

        T object = getObjectById(connection, id, type);
        fetchObjectReferencesCascade(connection, object,type);
        return object;
    }

    private static int deleteObjectById(Connection connection, Map<String, Object> id, TableMapInfo mapInfo) throws PersistenceException {
        try{
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.deleteObjectById(id);
        }catch (SQLException e) {
            throw  new PersistenceException(e);
        }
    }

    public static <T> int deleteObjectById(Connection connection, Map<String, Object> id,  Class<T> type) throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        return deleteObjectById(connection, id, mapInfo);
    }

    public static <T> int deleteObject(Connection connection, T object) throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        return deleteObjectById(connection, idMap,mapInfo);
    }

    public static <T> int deleteObjectById(Connection connection, Object id, Class<T> type)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return deleteObjectById(connection,
                    QueryUtils.createSqlParamsMap( mapInfo.getPkColumns().get(0),id),
                    mapInfo);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
            }
            return deleteObjectById(connection, idObj, mapInfo);
        }
    }

    public static <T> T getObjectByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildFieldSqlWithFieldName(mapInfo,null);
        String filter = GeneralJsonObjectDao.buildFilterSql(mapInfo,null,properties.keySet());
        String sql = "select " + q.getLeft() +" from " +mapInfo.getTableName();
        if(StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type));
    }

    public static <T> List<T> listAllObjects(Connection connection, Class<T> type)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildFieldSqlWithFieldName(mapInfo,null);
        String sql = "select " + q.getLeft() +" from " +mapInfo.getTableName();

        if(StringUtils.isNotBlank(mapInfo.getOrderBy()))
            sql = sql + " order by " + mapInfo.getOrderBy();
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        new HashMap<>(1)),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> listObjectsByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildFieldSqlWithFieldName(mapInfo,null);
        String filter = GeneralJsonObjectDao.buildFilterSql(mapInfo,null,properties.keySet());
        String sql = "select " + q.getLeft() +" from " +mapInfo.getTableName();
        if(StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;
        if(StringUtils.isNotBlank(mapInfo.getOrderBy()))
            sql = sql + " order by " + mapInfo.getOrderBy();

        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> listObjectsByProperties(Connection connection, Map<String, Object> properties, Class<T> type,
                                               final int startPos, final int maxSize)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildFieldSqlWithFieldName(mapInfo,null);
        String filter = GeneralJsonObjectDao.buildFilterSql(mapInfo,null,properties.keySet());
        String sql = "select " + q.getLeft() +" from " +mapInfo.getTableName();
        if(StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;
        if(StringUtils.isNotBlank(mapInfo.getOrderBy()))
            sql = sql + " order by " + mapInfo.getOrderBy();

        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
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
                connection, new QueryAndParams(sql,params),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByNamedParamsSql(Connection connection, String sql,
                                              Map<String,Object> params, Class<T> type)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,params),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }


    public static <T> List<T> queryObjectsBySql(Connection connection, String sql, Class<T> type,
                                         int startPos,  int maxSize)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        new HashMap<>()), startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByParamsSql(Connection connection, String sql, Object[] params, Class<T> type,
                                               int startPos,  int maxSize)
            throws PersistenceException {
        return queryParamsSql(
                connection, new QueryAndParams(sql,params),startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> List<T> queryObjectsByNamedParamsSql(Connection connection, String sql,
                                                    Map<String,Object> params, Class<T> type,
                                                    int startPos,  int maxSize)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,params), startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public static <T> T fetchObjectLazyColumn(Connection connection, T object,String columnName)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idMap)){
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION, "缺少主键对应的属性。");
        }

        String  sql =  "select " + mapInfo.findFieldByName(columnName).getColumnName() +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,idMap),
                (rs) -> OrmUtils.fetchFieldsFormResultSet(rs,object,mapInfo));
    }

    public static <T> T fetchObjectLazyColumns(Connection connection, T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        String fieldSql = mapInfo.buildLazyFieldSql(null);
        if(fieldSql==null)
            return object;
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idMap)){
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
        }

        String  sql =  "select " + fieldSql +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,idMap),
                (rs) -> OrmUtils.fetchFieldsFormResultSet(rs,object,mapInfo));
    }

    private static <T> T fetchObjectReference(Connection connection, T object,SimpleTableReference ref ,TableMapInfo mapInfo , boolean casecade)
            throws PersistenceException {

        if(ref==null || ref.getReferenceColumns().size()<1)
            return object;

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo( refType );
        if( refMapInfo == null )
            return object;

        Map<String, Object> properties = new HashMap<>(6);
        for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
            properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
        }

        List<?> refs = listObjectsByProperties( connection, properties, refType);
        if(refs!=null && refs.size()>0) {
            if(casecade){
                for(Object refObject : refs){
                    fetchObjectReferencesCascade(connection, refObject,refType);
                }
            }
            if (ref.getReferenceType().equals(refType) /*||
                    ref.getReferenceType().isAssignableFrom(refType) */){
                ReflectionOpt.setFieldValue(object, ref.getReferenceName(),
                        refs.get(0), ref.getReferenceType());
            }else if(Set.class.isAssignableFrom(ref.getReferenceType())){
                ReflectionOpt.setFieldValue(object, ref.getReferenceName(),
                        new HashSet<>(refs), ref.getReferenceType());
            }else if(List.class.isAssignableFrom(ref.getReferenceType())){
                ReflectionOpt.setFieldValue(object, ref.getReferenceName(),
                        refs, ref.getReferenceType());
            }
        }
        return object;
    }

    private static <T> T fetchObjectReference(Connection connection, T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {
        return fetchObjectReference(connection, object,ref ,mapInfo , false);
    }

    private static <T> T fetchObjectReferenceCascade(Connection connection, T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {
        return fetchObjectReference(connection, object,ref ,mapInfo , true);
    }

    private static <T> T fetchObjectReferencesCascade(Connection connection, T object, Class<?> objType ){
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                fetchObjectReferenceCascade(connection, object, ref, mapInfo);
            }
        }
        return object;
    }


    public static <T> T fetchObjectReference(Connection connection, T object, String reference  )
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);

        return fetchObjectReference(connection, object,ref,mapInfo);
    }

    public static <T> T fetchObjectReferences(Connection connection, T object)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                fetchObjectReference(connection, object, ref, mapInfo);
            }
        }
        return object;
    }


    public static <T> int deleteObjectByProperties(Connection connection, Map<String, Object> properties, Class<T> type)
            throws PersistenceException {
        try{
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.deleteObjectsByProperties(properties);
        }catch (SQLException e) {
            throw  new PersistenceException(e);
        }
    }

    private static <T> int deleteObjectReference(Connection connection, T object,SimpleTableReference ref)
            throws PersistenceException {

        if(ref==null || ref.getReferenceColumns().size()<1)
            return 0;

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo( refType );
        if( refMapInfo == null )
            return 0;

        Map<String, Object> properties = new HashMap<>(6);
        for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
            properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
        }

        return deleteObjectByProperties(connection, properties, refType);
    }

    public static <T> int deleteObjectReference(Connection connection, T object, String reference)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return deleteObjectReference(connection, object,ref);
    }

    public static <T> int deleteObjectReferences(Connection connection, T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int  n=0;
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n+= deleteObjectReference(connection, object,ref);
            }
        }
        return n;
    }

    public static <T> int deleteObjectCascadeShallow(Connection connection, T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);

        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                deleteObjectReference(connection, object,ref);
            }
        }

        return deleteObjectById(connection, idMap,mapInfo);
    }

    public static <T> int deleteObjectCascade(Connection connection, T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                Map<String, Object> properties = new HashMap<>(6);
                Class<?> refType = ref.getTargetEntityType();
                for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
                    properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
                }

                List<?> refs = listObjectsByProperties(connection,  properties, refType);
                for(Object refObject : refs){
                    deleteObjectCascade(connection, refObject);
                }
            }
        }
        return deleteObject(connection, object);
    }

    public static <T> int deleteObjectCascadeShallowById(Connection connection, Object id, final Class<T> type)
            throws PersistenceException {

        return deleteObjectCascadeShallow(connection, getObjectById(connection, id, type));
    }

    public static <T> int deleteObjectCascadeById(Connection connection, Object id, final Class<T> type)
            throws PersistenceException {

        return deleteObjectCascade(connection, getObjectById(connection, id, type));
    }

    public static class OrmObjectComparator<T> implements Comparator<T>{
        private TableInfo tableInfo;
        public  OrmObjectComparator(TableMapInfo tableInfo){
            this.tableInfo = tableInfo;
        }
        @Override
        public int compare(T o1, T o2) {
            for(String pkc : tableInfo.getPkColumns() ){
                Object f1 = ReflectionOpt.getFieldValue(o1,pkc);
                Object f2 = ReflectionOpt.getFieldValue(o2,pkc);
                if(f1==null){
                    if(f2!=null)
                        return -1;
                }else{
                    if(f2==null)
                        return 1;
                    if( ReflectionOpt.isNumberType(f1.getClass())){
                        double db1 = ((Number)f1).doubleValue();
                        double db2 = ((Number)f2).doubleValue();
                        if(db1>db2)
                            return 1;
                        if(db1<db2)
                            return -1;
                    }else{
                        String s1 = StringBaseOpt.objectToString(f1);
                        String s2 = StringBaseOpt.objectToString(f2);
                        int nc = s1.compareTo(s2);
                        if(nc!=0)
                            return nc;
                    }
                }
            }
            return 0;
        }

    }

    public static <T> int replaceObjectsAsTabulation(Connection connection, List<T> dbObjects,List<T> newObjects)
            throws PersistenceException {
        if(newObjects == null || newObjects.size()==0){
            if(dbObjects==null || dbObjects.size()==0){
                return 0;
            }
            for(T obj: dbObjects){
                deleteObject(connection, obj);
            }
            return dbObjects.size();
        }
        Class<T> objType =(Class<T>) newObjects.get(0).getClass();
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(objType);
        Triple<List<T>, List<Pair<T,T>>, List<T>>
                comRes=
                ListOpt.compareTwoList(dbObjects, newObjects,
                        new OrmObjectComparator<>( mapInfo) );
        int resN = 0;
        if(comRes.getLeft() != null) {
            for (T obj : comRes.getLeft()) {
                resN += saveNewObject(connection, obj);
            }
        }
        if(comRes.getRight() != null) {
            for (T obj : comRes.getRight()) {
                resN += deleteObject(connection, obj);
            }
        }
        if(comRes.getMiddle() != null) {
            for (Pair<T, T> pobj : comRes.getMiddle()) {
                resN += updateObject(connection, pobj.getRight());
            }
        }
        return resN;
    }

    public static <T> int replaceObjectsAsTabulation(Connection connection, List<T> newObjects,
                           final String propertyName, final Object propertyValue )
            throws PersistenceException {
        return replaceObjectsAsTabulation(connection, newObjects,
                JSONOpt.createHashMap(propertyName,propertyValue));
    }

    public static <T> int replaceObjectsAsTabulation(Connection connection, List<T> newObjects,
                                               Map<String, Object> properties)
            throws PersistenceException {
        if(newObjects==null || newObjects.size()<1)
            return 0;
        Class<T> objType =(Class<T>) newObjects.iterator().next().getClass();
        List<T> dbObjects = listObjectsByProperties(connection, properties, objType);
        return replaceObjectsAsTabulation(connection, dbObjects,newObjects);
    }

    private static <T> int saveNewObjectReferenceCascade(Connection connection, T object,
                                                         SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {

        if(ref==null || ref.getReferenceColumns().size()<1)
            return 0;

        Object newObj = ReflectionOpt.getFieldValue( object, ref.getReferenceName());
        if(newObj==null){
            return 0;
        }

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo( refType );
        if( refMapInfo == null )
            return 0;
        if (ref.getReferenceType().equals(refType)){ // OneToOne
            for(Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()){
                Object obj = mapInfo.findFieldByName(ent.getKey()).getObjectFieldValue(object);
                refMapInfo.findFieldByName(ent.getValue()).setObjectFieldValue(newObj,obj);
            }
            saveNewObjectCascade(connection, newObj);
        }else if(newObj instanceof Collection){
            for(Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()){
                Object obj = mapInfo.findFieldByName(ent.getKey()).getObjectFieldValue(object);
                for(Object subObj : (Collection<Object>)newObj) {
                    refMapInfo.findFieldByName(ent.getValue()).setObjectFieldValue(subObj, obj);
                }
            }
            for(Object subObj : (Collection<Object>)newObj){
                saveNewObjectCascade(connection, subObj);
            }
        }
        return 1;
    }

    private static <T> int saveObjectReference(Connection connection, T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {

        if(ref==null || ref.getReferenceColumns().size()<1)
            return 0;

        Object newObj = ReflectionOpt.getFieldValue( object, ref.getReferenceName());
        if(newObj==null){
            return deleteObjectReference(connection, object,ref);
        }

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo( refType );
        if( refMapInfo == null )
            return 0;

        Map<String, Object> properties = new HashMap<>(6);
        for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
            properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
        }

        List<?> refs = listObjectsByProperties(connection,  properties, refType);

        if (ref.getReferenceType().equals(refType)){ // OneToOne
            for(Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()){
                Object obj = mapInfo.findFieldByName(ent.getKey()).getObjectFieldValue(object);
                refMapInfo.findFieldByName(ent.getValue()).setObjectFieldValue(newObj,obj);
            }
            if(refs!=null && refs.size()>0){
                updateObject(connection, newObj);
            }else{
                saveNewObject(connection, newObj);
            }
        }else {
            List<Object> newListObj = Set.class.isAssignableFrom(ref.getReferenceType())?
                  new ArrayList<>((Set<?>) newObj):(List<Object>) newObj;

            for(Map.Entry<String, String> ent : ref.getReferenceColumns().entrySet()){
                Object obj = mapInfo.findFieldByName(ent.getKey()).getObjectFieldValue(object);
                for(Object subObj : newListObj) {
                    refMapInfo.findFieldByName(ent.getValue()).setObjectFieldValue(subObj, obj);
                }
            }
            replaceObjectsAsTabulation(connection, (List<Object>) refs, newListObj);

        }
        return 1;
    }

    public static <T> int saveObjectReference (Connection connection, T object, String reference)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return saveObjectReference(connection, object,ref,mapInfo);
    }

    public static <T> int saveObjectReferences (Connection connection, T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n=0;
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += saveObjectReference(connection, object, ref, mapInfo);
            }
        }
        return n;
    }

    public static <T> int saveNewObjectCascadeShallow (Connection connection, T object)
            throws PersistenceException {
        return saveNewObject(connection, object)
                + saveObjectReferences(connection, object);
    }

    public static <T> int saveNewObjectCascade (Connection connection, T object)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n= saveNewObject(connection, object);
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += saveNewObjectReferenceCascade(connection, object, ref, mapInfo);
            }
        }
        return n;
    }

    public static <T> int updateObjectCascadeShallow (Connection connection, T object)
            throws PersistenceException {
        return updateObject(connection, object)
           + saveObjectReferences(connection, object);
    }

    private static <T> int replaceObjectsAsTabulationCascade(Connection connection, List<T> dbObjects,List<T> newObjects)
            throws PersistenceException {

        if(newObjects == null || newObjects.size()==0){
            if(dbObjects==null || dbObjects.size()==0){
                return 0;
            }
            for(T obj: dbObjects){
                deleteObjectCascade(connection, obj);
            }
            return dbObjects.size();
        }

        Class<T> objType =(Class<T>) newObjects.get(0).getClass();
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(objType);
        Triple<List<T>, List<Pair<T,T>>, List<T>>
                comRes=
                ListOpt.compareTwoList(dbObjects, newObjects,
                        new OrmObjectComparator<>(mapInfo) );
        int resN = 0;
        if(comRes.getLeft() != null) {
            for (T obj : comRes.getLeft()) {
                resN += saveNewObjectCascade(connection, obj);
            }
        }
        if(comRes.getRight() != null) {
            for (T obj : comRes.getRight()) {
                resN += deleteObjectCascade(connection, obj);
            }
        }
        if(comRes.getMiddle() != null) {
            for (Pair<T, T> pobj : comRes.getMiddle()) {
                resN += updateObjectCascade(connection, pobj.getRight());
            }
        }
        return resN;
    }

    private static <T> int updateObjectReferenceCascade(Connection connection, T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {

        if(ref==null || ref.getReferenceColumns().size()<1)
            return 0;

        Object newObj = ReflectionOpt.getFieldValue( object, ref.getReferenceName());
        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo( refType );
        if( refMapInfo == null )
            return 0;

        Map<String, Object> properties = new HashMap<>(6);
        for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
            properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
        }
        int  n = 0;
        List<?> refs = listObjectsByProperties(connection,  properties, refType);
        if(newObj==null){
            if(refs!=null && refs.size()>0) {
                if (ref.getReferenceType().equals(refType)) { // OneToOne
                    n += deleteObjectCascade(connection, refs.get(0));
                } else {
                    for (Object subObj : refs) {
                        n += deleteObjectCascade(connection, subObj);
                    }
                }
            }
            return n;
        }

        if (ref.getReferenceType().equals(refType)){ // OneToOne
            if(refs!=null && refs.size()>0){
                updateObjectCascade(connection, newObj);
            }else{
                saveNewObjectCascade(connection, newObj);
            }
        }else if(Set.class.isAssignableFrom(ref.getReferenceType())){
            replaceObjectsAsTabulationCascade(connection,  (List<Object>) refs,
                    new ArrayList<>((Set<?>) newObj));
        }else if(List.class.isAssignableFrom(ref.getReferenceType())){
            replaceObjectsAsTabulationCascade(connection,  (List<Object>) refs,
                    (List<Object>) newObj );
        }

        return 1;
    }

    public static <T> int updateObjectCascade (Connection connection, T object) throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n= updateObject(connection, object);
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += updateObjectReferenceCascade(connection, object, ref, mapInfo);
            }
        }
        return n;
    }

    public static <T> int checkObjectExists(Connection connection, T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String,Object> objectMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);

        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,objectMap)){
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
        }
        String sql =
                "select count(*) as checkExists from " + mapInfo.getTableName()
                        + " where " +  GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,null);

        try {
            Long checkExists = NumberBaseOpt.castObjectToLong(
                    DatabaseAccess.getScalarObjectQuery(connection, sql, objectMap));
            return checkExists==null?0:checkExists.intValue();
        }catch (SQLException e) {
            throw  new PersistenceException(sql,e);
        }catch (IOException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int fetchObjectsCount(Connection connection, Map<String, Object> properties, Class<T> type)
            throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.fetchObjectsCount(properties).intValue();
        } catch (SQLException | IOException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int fetchObjectsCount(Connection connection, String sql , Map<String, Object> properties)
            throws PersistenceException {
        try {
            return NumberBaseOpt.castObjectToInteger(
                    DatabaseAccess.getScalarObjectQuery(connection,sql,properties));
        } catch (SQLException e) {
            throw  new PersistenceException(sql,e);
        } catch (IOException e){
            throw  new PersistenceException(e);
        }
    }

    public static <T> int mergeObjectCascadeShallow(Connection connection, T object)
            throws PersistenceException {
        int  checkExists = checkObjectExists(connection, object);
        if(checkExists == 0){
            return saveNewObjectCascadeShallow(connection, object);
        }else if(checkExists == 1){
            return updateObjectCascadeShallow(connection, object);
        }else{
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"主键属性有误，返回多个条记录。");
        }
    }

    public static <T> int mergeObjectCascade(Connection connection, T object) throws PersistenceException {
        int  checkExists = checkObjectExists(connection,object);
        if(checkExists == 0){
            return saveNewObjectCascade(connection,object);
        }else if(checkExists == 1){
            return updateObjectCascade(connection, object);
        }else{
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"主键属性有误，返回多个条记录。");
        }
    }
}
