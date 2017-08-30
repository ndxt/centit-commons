package com.centit.support.database.orm;

import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.database.jsonmaptable.GeneralJsonObjectDao;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.utils.*;
import com.centit.support.json.JSONOpt;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private final static <T> T queryNamedParamsSql(Connection conn, QueryAndNamedParams sqlAndParams,
                                                   FetchDataWork<T> fetchDataWork)
            throws SQLException, IOException,NoSuchFieldException,  InstantiationException, IllegalAccessException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(sqlAndParams);
        return queryParamsSql(conn, qap ,fetchDataWork);
    }

    public <T> T getObjectBySql(String sql, Map<String, Object> properties, Class<T> type, TableMapInfo mapInfo)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {
        JsonObjectDao sqlDialect = GeneralJsonObjectDao.createJsonObjectDao(connection, mapInfo);
        return queryNamedParamsSql(
                connection, new QueryAndNamedParams(sql,
                        properties),
                (rs) -> OrmUtils.fetchObjectFormResultSet(rs, type)
        );
    }

    public <T> T getObjectBySql(String sql, Map<String, Object> properties, Class<T> type)
            throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        return getObjectBySql(sql, properties,  type,  mapInfo);
    }

    public <T> T getObjectById(Object id, final Class<T> type)
            throws IOException, SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        Pair<String,String[]> q = GeneralJsonObjectDao.buildGetObjectSqlByPk(mapInfo);

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new SQLException("表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(q.getKey(),
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type,  mapInfo);
        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new SQLException("缺少主键对应的属性。");
            }
            return getObjectBySql(q.getKey(),
                    idObj, type,  mapInfo);
        }
    }

    public <T> T getObjectIncludeLzayById(Object id, final Class<T> type)
            throws SQLException, InstantiationException, IllegalAccessException, IOException, NoSuchFieldException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        String  sql = mapInfo.buildFieldIncludeLazySql("");

        if(ReflectionOpt.isScalarType(id.getClass())){
            if(mapInfo.getPkColumns()==null || mapInfo.getPkColumns().size()!=1)
                throw new SQLException("表"+mapInfo.getTableName()+"不是单主键表，这个方法不适用。");
            return getObjectBySql(sql,
                    QueryUtils.createSqlParamsMap(mapInfo.getPkColumns().get(0),id), type,  mapInfo);

        }else{
            Map<String, Object> idObj = OrmUtils.fetchObjectField(id);
            if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,idObj)){
                throw new SQLException("缺少主键对应的属性。");
            }
            return getObjectBySql(sql,
                    idObj, type,  mapInfo);
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


    public <T> List<T> listObjectByProperties(Map<String, Object> properties, Class<T> type){

        return null;
    }

    public <T> List<T> queryObjectsBySql(String sql, Class<T> type){

        return null;
    }

    public <T> List<T> queryObjectsByParamsSql(String sql, Object[] params, Class<T> type){

        return null;
    }

    public <T> List<T> queryObjectsByNamedParamsSql(String sql,
                                              Map<String,Object> params, Class<T> type){

        return null;
    }

    public <T> T fetchObjectLazyColumn(T object,String columnName){
        return object;
    }

    public <T> T fetchObjectLazyColumns(T object){
        return object;
    }

    public <T> T fetchObjectReference(T object,String reference){
        return object;
    }

    /**
     * 加载对象所有的 关联 对象
     * @param object 主对象
     * @param <T> 类型
     * @return 对象
     */
    public <T> T fetchObjectReferences(T object){
        return object;
    }

    public <T> int replaceObjectsAsTabulation(Collection<T> dbObjects,Collection<T> newObjects){

        return 0;
    }


    public <T> int replaceObjectsAsTabulation(Collection<T> newObjects,
                                               final String propertyName,
                                               final Object propertyValue ){
        return replaceObjectsAsTabulation(newObjects,
                JSONOpt.createHashMap(propertyName,propertyValue));
    }

    public  <T> int replaceObjectsAsTabulation(Collection<T> newObjects,
                                               Map<String, Object> properties){

        return 0;
    }

}
