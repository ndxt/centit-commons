package com.centit.support.database.orm;

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
public class OrmDaoSupport {

    private Connection connection;

    public OrmDaoSupport(){}

    public OrmDaoSupport(Connection connection) {
        this.connection = connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public JsonObjectDao getJsonObjectDao(){
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection);
        } catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    public Long getSequenceNextValue(final String sequenceName) {
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection)
                    .getSequenceNextValue(sequenceName);
        } catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        } catch (IOException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }
    }

    public JsonObjectDao getJsonObjectDao(TableMapInfo mapInfo){
        try {
            return GeneralJsonObjectDao.createJsonObjectDao(connection,mapInfo);
        } catch (SQLException e){
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    public <T> int saveNewObject(T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForInsert(object, mapInfo, sqlDialect);
            return sqlDialect.saveNewObject(OrmUtils.fetchObjectDatabaseField(object, mapInfo));
        }catch (NoSuchFieldException e){
            throw  new PersistenceException(PersistenceException.NOSUCHFIELD_EXCEPTION,e);
        }catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }catch (SQLException e){
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    public <T> int updateObject(T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );

            return sqlDialect.updateObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
        }catch (NoSuchFieldException e){
            throw  new PersistenceException(PersistenceException.NOSUCHFIELD_EXCEPTION,e);
        }catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }catch (SQLException e){
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    public <T> int updateObject(Collection<String> fields,  T object)
            throws PersistenceException  {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );

            return sqlDialect.updateObject(fields, OrmUtils.fetchObjectDatabaseField(object,mapInfo));
        }catch (NoSuchFieldException e){
            throw  new PersistenceException(PersistenceException.NOSUCHFIELD_EXCEPTION,e);
        }catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }catch (SQLException e){
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    public <T> int mergeObject(T object) throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );
            return sqlDialect.mergeObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
        }catch (NoSuchFieldException e){
            throw  new PersistenceException(PersistenceException.NOSUCHFIELD_EXCEPTION,e);
        }catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }catch (SQLException e){
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
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
         try{
            PreparedStatement stmt = conn.prepareStatement(sqlAndParams.getSql());
            DatabaseAccess.setQueryStmtParameters(stmt,sqlAndParams.getParams());
            ResultSet rs = stmt.executeQuery();
            T obj =fetchDataWork.execute(rs);
            rs.close();
            stmt.close();
            return obj;
        }catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }catch (NoSuchFieldException e){
            throw  new PersistenceException(PersistenceException.NOSUCHFIELD_EXCEPTION,e);
        }catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }catch (InstantiationException e){
            throw  new PersistenceException(PersistenceException.INSTANTIATION_EXCEPTION,e);
        }catch (IllegalAccessException e){
            throw  new PersistenceException(PersistenceException.ILLEGALACCESS_EXCEPTION,e);
        }
    }

    private final static <T> T queryParamsSql(Connection conn, QueryAndParams sqlAndParams ,
                                              int startPos, int maxSize, FetchDataWork<T> fetchDataWork)
            throws PersistenceException {
        sqlAndParams.setSql( QueryUtils.buildLimitQuerySQL(
                sqlAndParams.getSql(),  startPos , maxSize , false , DBType.mapDBType(conn)
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


    public <T> T getObjectBySql(String sql, Map<String, Object> properties, Class<T> type)
            throws PersistenceException{
        //JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type)
        );
    }

    public <T> T getObjectById(Object id, final Class<T> type)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildGetObjectSqlByPk(mapInfo);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,
                        "表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(q.getKey(),
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type);
        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,
                        "缺少主键对应的属性。");
            }
            return getObjectBySql(q.getKey(),
                    idObj, type);
        }

    }

    public <T> T getObjectIncludeLzayById(Object id, final Class<T> type)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        String  sql =  "select " + mapInfo.buildFieldIncludeLazySql("") +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(sql,
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
            }
            return getObjectBySql(sql, idObj, type);
        }

    }

    public <T> T getObjectCascadeShallow(Object id, final Class<T> type)
            throws PersistenceException {

        T object = getObjectIncludeLzayById(id, type);
        fetchObjectReferences(object);
        return object;
    }

    public <T> T getObjectCascade(Object id, final Class<T> type)
            throws PersistenceException {

        T object = getObjectIncludeLzayById(id, type);
        fetchObjectReferencesCascade(object,type);
        return object;
    }

    private int deleteObjectById(Map<String, Object> id, TableMapInfo mapInfo) throws PersistenceException {
        try{
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.deleteObjectById(id);
        }catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    public <T> int deleteObjectById(Map<String, Object> id,  Class<T> type) throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        return deleteObjectById(id,mapInfo);
    }

    public <T> int deleteObject(T object) throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        return deleteObjectById(idMap,mapInfo);
    }

    public <T> int deleteObjectById(Object id, Class<T> type)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return deleteObjectById(
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id),
                    mapInfo);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
            }
            return deleteObjectById(idObj, mapInfo);
        }
    }

    public <T> T getObjectByProperties(Map<String, Object> properties, Class<T> type)
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

    public <T> List<T> listAllObjects(Class<T> type)
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

    public <T> List<T> listObjectsByProperties(Map<String, Object> properties, Class<T> type)
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

    public <T> List<T> listObjectsByProperties(Map<String, Object> properties, Class<T> type,
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

    public <T> List<T> queryObjectsBySql(String sql, Class<T> type)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        new HashMap<>()),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> List<T> queryObjectsByParamsSql(String sql, Object[] params, Class<T> type)
            throws PersistenceException {
        return queryParamsSql(
                connection, new QueryAndParams(sql,params),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> List<T> queryObjectsByNamedParamsSql(String sql,
                                              Map<String,Object> params, Class<T> type)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,params),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }


    public <T> List<T> queryObjectsBySql(String sql, Class<T> type,
                                         int startPos,  int maxSize)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        new HashMap<>()), startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> List<T> queryObjectsByParamsSql(String sql, Object[] params, Class<T> type,
                                               int startPos,  int maxSize)
            throws PersistenceException {
        return queryParamsSql(
                connection, new QueryAndParams(sql,params),startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> List<T> queryObjectsByNamedParamsSql(String sql,
                                                    Map<String,Object> params, Class<T> type,
                                                    int startPos,  int maxSize)
            throws PersistenceException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,params), startPos, maxSize,
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> T fetchObjectLazyColumn(T object,String columnName)
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

    public <T> T fetchObjectLazyColumns(T object)
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

    private <T> T fetchObjectReference(T object,SimpleTableReference ref ,TableMapInfo mapInfo , boolean casecade)
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

        List<?> refs = listObjectsByProperties( properties, refType);
        if(refs!=null && refs.size()>0) {
            if(casecade){
                for(Object refObject : refs){
                    fetchObjectReferencesCascade(refObject,refType);
                }
            }
            if (ref.getReferenceType().equals(refType) /*||
                    ref.getReferenceType().isAssignableFrom(refType) */){
                ReflectionOpt.setFieldValue(object, ref.getReferenceName(), refs.get(0) );
            }else if(ref.getReferenceType().isAssignableFrom(Set.class)){
                ReflectionOpt.setFieldValue(object, ref.getReferenceName(), new HashSet<>(refs));
            }else if(ref.getReferenceType().isAssignableFrom(List.class)){
                ReflectionOpt.setFieldValue(object, ref.getReferenceName(), refs);
            }
        }
        return object;
    }

    private <T> T fetchObjectReference(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {
        return fetchObjectReference(object,ref ,mapInfo , false);
    }

    private <T> T fetchObjectReferenceCascade(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {
        return fetchObjectReference(object,ref ,mapInfo , true);
    }

    private <T> T fetchObjectReferencesCascade(T object, Class<?> objType ){
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                fetchObjectReferenceCascade(object, ref, mapInfo);
            }
        }
        return object;
    }


    public <T> T fetchObjectReference(T object, String reference  )
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);

        return fetchObjectReference(object,ref,mapInfo);
    }

    public <T> T fetchObjectReferences(T object)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                fetchObjectReference(object, ref, mapInfo);
            }
        }
        return object;
    }


    public <T> int deleteObjectByProperties(Map<String, Object> properties, Class<T> type)
            throws PersistenceException {
        try{
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.deleteObjectsByProperties(properties);
        }catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }
    }

    private <T> int deleteObjectReference(T object,SimpleTableReference ref)
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

        return deleteObjectByProperties(properties, refType);
    }

    public <T> int deleteObjectReference(T object, String reference)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return deleteObjectReference(object,ref);
    }

    public <T> int deleteObjectReferences(T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int  n=0;
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n+= deleteObjectReference(object,ref);
            }
        }
        return n;
    }

    public <T> int deleteObjectCascadeShallow(T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);

        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                deleteObjectReference(object,ref);
            }
        }

        return deleteObjectById(idMap,mapInfo);
    }

    public <T> int deleteObjectCascade(T object)
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

                List<?> refs = listObjectsByProperties( properties, refType);
                for(Object refObject : refs){
                    deleteObjectCascade(refObject);
                }
            }
        }
        return deleteObject(object);
    }

    public <T> int deleteObjectCascadeShallowById(Object id, final Class<T> type)
            throws PersistenceException {

        return deleteObjectCascadeShallow(getObjectById(id, type));
    }

    public <T> int deleteObjectCascadeById(Object id, final Class<T> type)
            throws PersistenceException {

        return deleteObjectCascade(getObjectById(id, type));
    }

    public class OrmObjectComparator<T> implements Comparator<T>{
        private TableInfo tableInfo;
        public OrmObjectComparator(TableMapInfo tableInfo){
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

    public <T> int replaceObjectsAsTabulation(List<T> dbObjects,List<T> newObjects)
            throws PersistenceException {
        Class<T> objType =(Class<T>) newObjects.iterator().next().getClass();
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(objType);
        Triple<List<T>, List<Pair<T,T>>, List<T>>
                comRes=
                ListOpt.compareTwoList(dbObjects, newObjects,
                        new OrmObjectComparator<>(mapInfo) );
        int resN = 0;
        for(T obj:comRes.getLeft()){
            resN += saveNewObject( obj);
        }
        for(T obj:comRes.getRight()){
            resN += deleteObject(obj);
        }
        for(Pair<T,T> pobj:comRes.getMiddle()){
            resN += updateObject(pobj.getRight());
        }
        return resN;
    }

    public <T> int replaceObjectsAsTabulation(List<T> newObjects,
                           final String propertyName, final Object propertyValue )
            throws PersistenceException {
        return replaceObjectsAsTabulation(newObjects,
                JSONOpt.createHashMap(propertyName,propertyValue));
    }

    public <T> int replaceObjectsAsTabulation(List<T> newObjects,
                                               Map<String, Object> properties)
            throws PersistenceException {
        if(newObjects==null || newObjects.size()<1)
            return 0;
        Class<T> objType =(Class<T>) newObjects.iterator().next().getClass();
        List<T> dbObjects = this.listObjectsByProperties(properties, objType);
        return replaceObjectsAsTabulation(dbObjects,newObjects);
    }

    private <T> int saveNewObjectReferenceCascade(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
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
            saveNewObjectCascade(newObj);
        }else if(newObj instanceof Collection){
            for(Object subObj : (Collection<Object>)newObj){
                saveNewObjectCascade(subObj);
            }
        }
        return 1;
    }

    private <T> int saveObjectReference(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws PersistenceException {

        if(ref==null || ref.getReferenceColumns().size()<1)
            return 0;

        Object newObj = ReflectionOpt.getFieldValue( object, ref.getReferenceName());
        if(newObj==null){
            return deleteObjectReference(object,ref);
        }

        Class<?> refType = ref.getTargetEntityType();
        TableMapInfo refMapInfo = JpaMetadata.fetchTableMapInfo( refType );
        if( refMapInfo == null )
            return 0;

        Map<String, Object> properties = new HashMap<>(6);
        for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
            properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
        }

        List<?> refs = listObjectsByProperties( properties, refType);

        if (ref.getReferenceType().equals(refType)){ // OneToOne
            if(refs!=null && refs.size()>0){
                updateObject(newObj);
            }else{
                saveNewObject(newObj);
            }
        }else if(ref.getReferenceType().isAssignableFrom(Set.class)){

                replaceObjectsAsTabulation( (List<Object>) refs,
                        new ArrayList<>((Set<?>) newObj));
        }else if(ref.getReferenceType().isAssignableFrom(List.class)){
            replaceObjectsAsTabulation( (List<Object>) refs,
                    (List<Object>) newObj );
        }

        return 1;
    }

    public <T> int saveObjectReference (T object, String reference)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return saveObjectReference(object,ref,mapInfo);
    }

    public <T> int saveObjectReferences (T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n=0;
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += saveObjectReference(object, ref, mapInfo);
            }
        }
        return n;
    }

    public <T> int saveNewObjectCascadeShallow (T object)
            throws PersistenceException {
        return saveNewObject(object)
                + saveObjectReferences(object);
    }

    public <T> int saveNewObjectCascade (T object)
            throws PersistenceException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n= saveNewObject(object);
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += saveNewObjectReferenceCascade(object, ref, mapInfo);
            }
        }
        return n;
    }

    public <T> int updateObjectCascadeShallow (T object)
            throws PersistenceException {
        return updateObject(object)
           + saveObjectReferences(object);
    }

    private <T> int replaceObjectsAsTabulationCascade(List<T> dbObjects,List<T> newObjects)
            throws PersistenceException {
        Class<T> objType =(Class<T>) newObjects.iterator().next().getClass();
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(objType);
        Triple<List<T>, List<Pair<T,T>>, List<T>>
                comRes=
                ListOpt.compareTwoList(dbObjects, newObjects,
                        new OrmObjectComparator<>(mapInfo) );
        int resN = 0;
        for(T obj:comRes.getLeft()){
            resN += saveNewObjectCascade( obj);
        }
        for(T obj:comRes.getRight()){
            resN += deleteObjectCascade(obj);
        }
        for(Pair<T,T> pobj:comRes.getMiddle()){
            resN += updateObjectCascade(pobj.getRight());
        }
        return resN;
    }

    private <T> int updateObjectReferenceCascade(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
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
        List<?> refs = listObjectsByProperties( properties, refType);
        if(newObj==null){
            if(refs!=null && refs.size()>0) {
                if (ref.getReferenceType().equals(refType)) { // OneToOne
                    n += deleteObjectCascade(refs.get(0));
                } else {
                    for (Object subObj : refs) {
                        n += deleteObjectCascade(subObj);
                    }
                }
            }
            return n;
        }

        if (ref.getReferenceType().equals(refType)){ // OneToOne
            if(refs!=null && refs.size()>0){
                updateObjectCascade(newObj);
            }else{
                saveNewObjectCascade(newObj);
            }
        }else if(ref.getReferenceType().isAssignableFrom(Set.class)){
            replaceObjectsAsTabulationCascade( (List<Object>) refs,
                    new ArrayList<>((Set<?>) newObj));
        }else if(ref.getReferenceType().isAssignableFrom(List.class)){
            replaceObjectsAsTabulationCascade( (List<Object>) refs,
                    (List<Object>) newObj );
        }

        return 1;
    }

    public <T> int updateObjectCascade (T object) throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        int n= updateObject(object);
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                n += updateObjectReferenceCascade(object, ref, mapInfo);
            }
        }
        return n;
    }

    public <T> int checkObjectExists(T object)
            throws PersistenceException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String,Object> objectMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);

        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,objectMap)){
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"缺少主键对应的属性。");
        }
        String sql =
                "select count(1) as checkExists from " + mapInfo.getTableName()
                        + " where " +  GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,null);

        try {
            Long checkExists = NumberBaseOpt.castObjectToLong(
                    DatabaseAccess.getScalarObjectQuery(connection, sql, objectMap));
            return checkExists==null?0:checkExists.intValue();
        }catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        }catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }
    }

    public <T> int fetchObjectsCount(Map<String, Object> properties, Class<T> type)
            throws PersistenceException {
        try {
            TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
            JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
            return sqlDialect.fetchObjectsCount(properties).intValue();
        } catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        } catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }
    }

    public <T> int fetchObjectsCount(String sql , Map<String, Object> properties)
            throws PersistenceException {
        try {
            return NumberBaseOpt.castObjectToInteger(
                    DatabaseAccess.getScalarObjectQuery(connection,sql,properties));
        } catch (SQLException e) {
            throw  new PersistenceException(PersistenceException.DATABASE_SQL_EXCEPTION,e);
        } catch (IOException e){
            throw  new PersistenceException(PersistenceException.DATABASE_IO_EXCEPTION,e);
        }
    }

    public <T> int mergeObjectCascadeShallow(T object)
            throws PersistenceException {
        int  checkExists = checkObjectExists(object);
        if(checkExists == 0){
            return saveNewObjectCascadeShallow(object);
        }else if(checkExists == 1){
            return updateObjectCascadeShallow(object);
        }else{
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"主键属性有误，返回多个条记录。");
        }
    }

    public <T> int mergeObjectCascade(T object) throws PersistenceException {
        int  checkExists = checkObjectExists(object);
        if(checkExists == 0){
            return saveNewObjectCascadeShallow(object);
        }else if(checkExists == 1){
            return saveNewObjectCascade(object);
        }else if(checkExists == 1){
            return updateObjectCascade(object);
        }else{
            throw new PersistenceException(PersistenceException.ORM_METADATA_EXCEPTION,"主键属性有误，返回多个条记录。");
        }
    }
}
