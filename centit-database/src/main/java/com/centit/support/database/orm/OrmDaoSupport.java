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

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public <T> int saveNewObject(T object) throws NoSuchFieldException, SQLException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        object = OrmUtils.prepareObjectForInsert(object,mapInfo,sqlDialect );
        return sqlDialect.saveNewObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
    }

    public <T> int updateObject(T object) throws SQLException, NoSuchFieldException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );
        return sqlDialect.updateObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
    }

    public <T> int updateObject(Collection<String> fields,  T object)
            throws SQLException, NoSuchFieldException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );
        return sqlDialect.updateObject(fields, OrmUtils.fetchObjectDatabaseField(object,mapInfo));
    }

    public <T> int mergeObject(T object)throws SQLException, NoSuchFieldException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        object = OrmUtils.prepareObjectForUpdate(object,mapInfo,sqlDialect );
        return sqlDialect.mergeObject( OrmUtils.fetchObjectDatabaseField(object,mapInfo));
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
     * @throws SQLException 异常
     * @throws IOException 异常
     * @throws NoSuchFieldException 异常
     * @throws InstantiationException 异常
     * @throws IllegalAccessException 异常
     */

    private final static <T> T queryParamsSql(Connection conn, QueryAndParams sqlAndParams ,
                                             FetchDataWork<T> fetchDataWork)
            throws SQLException, IOException,NoSuchFieldException,  InstantiationException, IllegalAccessException {
         try{
            PreparedStatement stmt = conn.prepareStatement(sqlAndParams.getSql());
            DatabaseAccess.setQueryStmtParameters(stmt,sqlAndParams.getParams());
            ResultSet rs = stmt.executeQuery();
            T obj =fetchDataWork.execute(rs);
            rs.close();
            stmt.close();
            return obj;
        }catch (SQLException e) {
            throw new DatabaseAccessException(sqlAndParams.getSql(),e);
        }
    }
    /**
     * 查询数据库模板代码
     * @param conn 数据库链接
     * @param sqlAndParams 命名查询语句
     * @param fetchDataWork 获取数据的方法
     * @param <T> 返回类型嗯
     * @return 返回结果
     * @throws SQLException 异常
     * @throws IOException 异常
     * @throws NoSuchFieldException 异常
     * @throws InstantiationException 异常
     * @throws IllegalAccessException 异常
     */
    private static <T> T queryNamedParamsSql(Connection conn, QueryAndNamedParams sqlAndParams,
                                                   FetchDataWork<T> fetchDataWork)
            throws SQLException, IOException,NoSuchFieldException,  InstantiationException, IllegalAccessException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(sqlAndParams);
        return queryParamsSql(conn, qap ,fetchDataWork);
    }

    public <T> T getObjectBySql(String sql, Map<String, Object> properties, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        //JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type)
        );
    }

    public <T> T getObjectById(Object id, final Class<T> type)
            throws IOException, SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildGetObjectSqlByPk(mapInfo);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new SQLException("表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(q.getKey(),
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type);
        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new SQLException("缺少主键对应的属性。");
            }
            return getObjectBySql(q.getKey(),
                    idObj, type);
        }
    }

    public <T> T getObjectIncludeLzayById(Object id, final Class<T> type)
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        String  sql =  "select " + mapInfo.buildFieldIncludeLazySql("") +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new SQLException("表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(sql,
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new SQLException("缺少主键对应的属性。");
            }
            return getObjectBySql(sql, idObj, type);
        }
    }

    private int deleteObjectById(Map<String, Object> id, TableMapInfo mapInfo) throws SQLException {
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return sqlDialect.deleteObjectById(id);
    }

    public <T> int deleteObjectById(Map<String, Object> id,  Class<T> type) throws SQLException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        return deleteObjectById(id,mapInfo);
    }

    public <T> int deleteObject(T object) throws NoSuchFieldException, SQLException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        return deleteObjectById(idMap,mapInfo);
    }

    public <T> int deleteObjectById(Object id, Class<T> type)
            throws NoSuchFieldException, SQLException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new SQLException("表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return deleteObjectById(
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id),
                    mapInfo);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new SQLException("缺少主键对应的属性。");
            }
            return deleteObjectById(idObj, mapInfo);
        }
    }

    public <T> T getObjectByProperties(Map<String, Object> properties, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildFieldSql(mapInfo,null);
        String filter = GeneralJsonObjectDao.buildFilterSql(mapInfo,null,properties.keySet());
        String sql = "select " + q.getLeft() +" from " +mapInfo.getTableName();
        if(StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type));
    }

    public <T> List<T> listObjectByProperties(Map<String, Object> properties, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildFieldSql(mapInfo,null);
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

    public <T> List<T> queryObjectsBySql(String sql, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        new HashMap<>()),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> List<T> queryObjectsByParamsSql(String sql, Object[] params, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        return queryParamsSql(
                connection, new QueryAndParams(sql,params),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> List<T> queryObjectsByNamedParamsSql(String sql,
                                              Map<String,Object> params, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,params),
                (rs) -> OrmUtils.fetchObjectListFormResultSet(rs, type));
    }

    public <T> T fetchObjectLazyColumn(T object,String columnName)
            throws NoSuchFieldException, SQLException,
            IllegalAccessException, IOException, InstantiationException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idMap)){
            throw new SQLException("缺少主键对应的属性。");
        }

        String  sql =  "select " + mapInfo.findFieldByName(columnName).getColumnName() +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,idMap),
                (rs) -> OrmUtils.fetchFieldsFormResultSet(rs,object,mapInfo));
    }

    public <T> T fetchObjectLazyColumns(T object)
            throws SQLException, NoSuchFieldException, InstantiationException,
            IllegalAccessException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        String fieldSql = mapInfo.buildLazyFieldSql(null);
        if(fieldSql==null)
            return object;
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idMap)){
            throw new SQLException("缺少主键对应的属性。");
        }

        String  sql =  "select " + fieldSql +
                " from " +mapInfo.getTableName() + " where " +
                GeneralJsonObjectDao.buildFilterSqlByPk(mapInfo,null);

        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,idMap),
                (rs) -> OrmUtils.fetchFieldsFormResultSet(rs,object,mapInfo));
    }

    private <T> T fetchObjectReference(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {

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

        List<?> refs = listObjectByProperties( properties, refType);

        if(refs!=null && refs.size()>0) {
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

    public <T> T fetchObjectReference(T object, String reference  )
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);

        return fetchObjectReference(object,ref,mapInfo);
    }

    public <T> T fetchObjectReferences(T object)
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                fetchObjectReference(object, ref, mapInfo);
            }
        }
        return object;
    }


    public <T> int deleteObjectByProperties(Map<String, Object> properties, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return sqlDialect.deleteObjectsByProperties(properties);
    }

    private <T> int deleteObjectReference(T object,SimpleTableReference ref)
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {

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
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return deleteObjectReference(object,ref);
    }

    public <T> int deleteObjectReferences(T object)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
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
            throws NoSuchFieldException, SQLException, IllegalAccessException, IOException, InstantiationException {
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
            throws NoSuchFieldException, SQLException, IllegalAccessException, IOException, InstantiationException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String, Object> idMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(mapInfo.hasReferences()) {
            for (SimpleTableReference ref : mapInfo.getReferences()) {
                Map<String, Object> properties = new HashMap<>(6);
                Class<?> refType = ref.getTargetEntityType();
                for(Map.Entry<String,String> ent : ref.getReferenceColumns().entrySet()){
                    properties.put(ent.getValue(), ReflectionOpt.getFieldValue(object,ent.getKey()));
                }

                List<?> refs = listObjectByProperties( properties, refType);
                for(Object refObject : refs){
                    deleteObjectCascade(refObject);
                }
            }
        }
        return deleteObject(object);
    }

    public <T> int deleteObjectCascadeShallowById(Object id, final Class<T> type)
            throws NoSuchFieldException, SQLException, IllegalAccessException, IOException, InstantiationException {

        return deleteObjectCascadeShallow(getObjectById(id, type));
    }

    public <T> int deleteObjectCascadeById(Object id, final Class<T> type)
            throws NoSuchFieldException, SQLException, IllegalAccessException, IOException, InstantiationException {

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
            throws NoSuchFieldException, IOException, SQLException {
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
            throws SQLException, NoSuchFieldException, InstantiationException, IOException, IllegalAccessException {
        return replaceObjectsAsTabulation(newObjects,
                JSONOpt.createHashMap(propertyName,propertyValue));
    }

    public <T> int replaceObjectsAsTabulation(List<T> newObjects,
                                               Map<String, Object> properties)
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {
        if(newObjects==null || newObjects.size()<1)
            return 0;
        Class<T> objType =(Class<T>) newObjects.iterator().next().getClass();
        List<T> dbObjects = this.listObjectByProperties(properties, objType);
        return replaceObjectsAsTabulation(dbObjects,newObjects);
    }

    private <T> int saveNewObjectReferenceCascade(T object,SimpleTableReference ref ,TableMapInfo mapInfo )
            throws SQLException, NoSuchFieldException, InstantiationException, IOException, IllegalAccessException {

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
            throws SQLException, NoSuchFieldException, InstantiationException, IOException, IllegalAccessException {

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

        List<?> refs = listObjectByProperties( properties, refType);

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
            throws SQLException, InstantiationException, IllegalAccessException,
            IOException, NoSuchFieldException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        SimpleTableReference ref = mapInfo.findReference(reference);
        return saveObjectReference(object,ref,mapInfo);
    }

    public <T> int saveObjectReferences (T object)
            throws SQLException, InstantiationException, IllegalAccessException,
            IOException, NoSuchFieldException {
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
            throws NoSuchFieldException, IOException, SQLException,
            IllegalAccessException, InstantiationException {
        return saveNewObject(object)
                + saveObjectReferences(object);
    }

    public <T> int saveNewObjectCascade (T object)
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {

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
            throws NoSuchFieldException, SQLException, IOException,
            IllegalAccessException, InstantiationException {
        return updateObject(object)
           + saveObjectReferences(object);
    }

    private <T> int replaceObjectsAsTabulationCascade(List<T> dbObjects,List<T> newObjects)
            throws NoSuchFieldException, IOException, SQLException, InstantiationException, IllegalAccessException {
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
            throws SQLException, NoSuchFieldException, InstantiationException, IOException, IllegalAccessException {

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
        List<?> refs = listObjectByProperties( properties, refType);
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

    public <T> int updateObjectCascade (T object) throws NoSuchFieldException, SQLException,
            IOException, IllegalAccessException, InstantiationException {
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
            throws NoSuchFieldException, SQLException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(object.getClass());
        Map<String,Object> objectMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);

        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,objectMap)){
            throw new SQLException("缺少主键对应的属性。");
        }
        String sql =
                "select count(1) as checkExists from " + mapInfo.getTableName()
                        + " where " +  GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,null);
        Long checkExists = NumberBaseOpt.castObjectToLong(
                DatabaseAccess.getScalarObjectQuery(connection, sql, objectMap));
        return checkExists==null?0:checkExists.intValue();
    }

    public <T> int mergeObjectCascadeShallow(T object)
            throws SQLException, NoSuchFieldException, IOException, InstantiationException, IllegalAccessException {
        int  checkExists = checkObjectExists(object);
        if(checkExists == 0){
            return saveNewObjectCascadeShallow(object);
        }else if(checkExists == 1){
            return updateObjectCascadeShallow(object);
        }else{
            throw new SQLException("主键属性有误，返回多个条记录。");
        }
    }

    public <T> int mergeObjectCascade(T object) throws SQLException, NoSuchFieldException, IOException, InstantiationException, IllegalAccessException {
        int  checkExists = checkObjectExists(object);
        if(checkExists == 0){
            return saveNewObjectCascadeShallow(object);
        }else if(checkExists == 1){
            return saveNewObjectCascade(object);
        }else if(checkExists == 1){
            return updateObjectCascade(object);
        }else{
            throw new SQLException("主键属性有误，返回多个条记录。");
        }
    }
}
