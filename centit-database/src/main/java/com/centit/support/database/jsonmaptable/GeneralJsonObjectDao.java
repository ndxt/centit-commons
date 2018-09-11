package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.*;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.DatabaseAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


public abstract class GeneralJsonObjectDao implements JsonObjectDao {

    private Connection conn;

    private TableInfo tableInfo;

    public GeneralJsonObjectDao(){

    }

    public static GeneralJsonObjectDao createJsonObjectDao(final Connection conn,final TableInfo tableInfo )
            throws SQLException {
        DBType dbtype = DBType.mapDBType(conn.getMetaData().getURL());
        switch (dbtype){
            case Oracle:
                return new OracleJsonObjectDao(conn,tableInfo);
            case DB2:
                return new DB2JsonObjectDao(conn,tableInfo);
            case SqlServer:
                return new SqlSvrJsonObjectDao(conn,tableInfo);
            case MySql:
                return new MySqlJsonObjectDao(conn,tableInfo);
            case H2:
                return new H2JsonObjectDao(conn,tableInfo);

            case Access:
            default:
                throw new  SQLException("不支持的数据库类型："+dbtype.toString());
        }
    }

    public static GeneralJsonObjectDao createJsonObjectDao(final Connection conn)
            throws SQLException {
        DBType dbtype = DBType.mapDBType(conn.getMetaData().getURL());
        switch (dbtype){
            case Oracle:
                return new OracleJsonObjectDao(conn);
            case DB2:
                return new DB2JsonObjectDao(conn);
            case SqlServer:
                return new SqlSvrJsonObjectDao(conn);
            case MySql:
                return new MySqlJsonObjectDao(conn);
            case H2:
                return new H2JsonObjectDao(conn);

            case Access:
            default:
                throw new  SQLException("不支持的数据库类型："+dbtype.toString());
        }
    }


    public GeneralJsonObjectDao(final TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public GeneralJsonObjectDao(final Connection conn) {
        this.conn = conn;
    }

    public GeneralJsonObjectDao(final Connection conn,final TableInfo tableInfo) {
        this.conn = conn;
        this.tableInfo = tableInfo;
    }

    public void setConnect(final Connection conn) {
        this.conn = conn;
    }

    public Connection getConnect() {
        return this.conn;
    }
    public void setTableInfo(final TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    @Override
    public TableInfo getTableInfo() {
        return this.tableInfo;
    }

    /**
     * 返回 sql 语句 和 属性名数组
     * @param ti TableInfo
     * @param alias String
     * @return Pair String String []
     */
    public static String buildFieldSql(TableInfo ti, String alias){
        StringBuilder sBuilder= new StringBuilder();
        List<? extends TableField> columns = ti.getColumns();
        boolean addAlias = StringUtils.isNotBlank(alias);
        int i=0;
        for(TableField col : columns){
            if(i>0)
                sBuilder.append(", ");
            else
                sBuilder.append(" ");
            if(addAlias)
                sBuilder.append(alias).append('.');
            sBuilder.append(col.getColumnName());
            i++;
        }
        return sBuilder.toString();
    }

    /**
     * 返回 sql 语句 和 属性名数组
     * @param ti TableInfo
     * @param alias String
     * @return Pair String String []
     */
    public static Pair<String,String[]> buildFieldSqlWithFieldName(TableInfo ti, String alias){
        StringBuilder sBuilder= new StringBuilder();
        List<? extends TableField> columns = ti.getColumns();
        String [] fieldNames = new String[columns.size()];
        boolean addAlias = StringUtils.isNotBlank(alias);
        int i=0;
        for(TableField col : columns){
            if(i>0)
                sBuilder.append(", ");
            else
                sBuilder.append(" ");
            if(addAlias)
                sBuilder.append(alias).append('.');
            sBuilder.append(col.getColumnName());
            fieldNames[i] = col.getPropertyName();
            i++;
        }
        return new ImmutablePair<>(sBuilder.toString(),fieldNames);
    }

    public static boolean isPkColumn(TableInfo ti, String propertyName){
        TableField field = ti.findFieldByName(propertyName);
        return ti.getPkColumns().contains(field.getColumnName());
    }

    public static boolean checkHasAllPkColumns(TableInfo tableInfo, Map<String, Object> properties){
        for(String pkc : tableInfo.getPkColumns() ){
            TableField field = tableInfo.findFieldByColumn(pkc);
            if( field != null &&
                    properties.get(field.getPropertyName()) == null)
                return false;
        }
        return true;
    }

    public boolean checkHasAllPkColumns(Map<String, Object> properties){
        return GeneralJsonObjectDao.checkHasAllPkColumns(tableInfo,properties) ;
    }

    public static String buildFilterSqlByPk(TableInfo ti,String alias){
        StringBuilder sBuilder= new StringBuilder();
        int i=0;
        for(String plCol : ti.getPkColumns()){
            if(i>0)
                sBuilder.append(" and ");
            TableField col = ti.findFieldByColumn(plCol);
            if(StringUtils.isNotBlank(alias))
                sBuilder.append(alias).append('.');
            sBuilder.append(col.getColumnName()).append(" = :").append(col.getPropertyName());
            i++;
        }
        return sBuilder.toString();
    }

    public static String buildFilterSql(TableInfo ti,String alias,Collection<String> properties){
        StringBuilder sBuilder= new StringBuilder();
        int i=0;
        for(String plCol : properties){
            TableField col = ti.findFieldByName(plCol);
            if( col != null ) {
                if (i > 0) {
                    sBuilder.append(" and ");
                }
                if (StringUtils.isNotBlank(alias))
                    sBuilder.append(alias).append('.');
                sBuilder.append(col.getColumnName()).append(" = :").append(col.getPropertyName());
                i++;
            }
        }
        return sBuilder.toString();
    }

    public static Pair<String,String[]> buildGetObjectSqlByPk(TableInfo ti){
        Pair<String,String[]> q = buildFieldSqlWithFieldName(ti,null);
        String filter = buildFilterSqlByPk(ti,null);
        return new ImmutablePair<>(
                "select " + q.getLeft() +" from " +ti.getTableName() + " where " + filter,
                q.getRight());
    }

    @Override
    public JSONObject getObjectById(final Object keyValue) throws SQLException, IOException {
        if(tableInfo.getPkColumns()==null || tableInfo.getPkColumns().size()!=1)
            throw new SQLException("表"+tableInfo.getTableName()+"不是单主键表，这个方法不适用。");

        Pair<String,String[]> q = buildGetObjectSqlByPk(tableInfo);
        JSONArray ja = DatabaseAccess.findObjectsByNamedSqlAsJSON(
                 conn, q.getLeft(),
                 CollectionsOpt.createHashMap( tableInfo.getPkColumns().get(0),keyValue),
                 q.getRight());
        if(ja.size()<1)
            return null;
        return (JSONObject) ja.get(0);
    }

    @Override
    public JSONObject getObjectById(final Map<String, Object> keyValues) throws SQLException, IOException {
        if(! checkHasAllPkColumns(keyValues)){
            throw new SQLException("缺少主键对应的属性。");
        }
        Pair<String,String[]> q = buildGetObjectSqlByPk(tableInfo);
        JSONArray ja = DatabaseAccess.findObjectsByNamedSqlAsJSON(
                 conn, q.getLeft(),
                 keyValues,
                 q.getRight());
        if(ja.size()<1)
            return null;
        return (JSONObject) ja.get(0);
    }

    @Override
    public JSONObject getObjectByProperties(final Map<String, Object> properties) throws SQLException, IOException {

        Pair<String,String[]> q = buildFieldSqlWithFieldName(tableInfo,null);
        String filter = buildFilterSql(tableInfo,null,properties.keySet());
        JSONArray ja = DatabaseAccess.findObjectsByNamedSqlAsJSON(
                 conn,
                 "select " + q.getLeft() +" from " +tableInfo.getTableName() + " where " + filter,
                 properties,
                 q.getRight());
        if(ja.size()<1)
            return null;
        return (JSONObject) ja.get(0);
    }

    @Override
    public JSONArray listObjectsByProperties(final Map<String, Object> properties) throws SQLException, IOException {
        Pair<String,String[]> q = buildFieldSqlWithFieldName(tableInfo,null);
        String filter = buildFilterSql(tableInfo,null,properties.keySet());
        String sql = "select " + q.getLeft() +" from " +tableInfo.getTableName();
        if(StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;

        if(StringUtils.isNotBlank(tableInfo.getOrderBy()))
            sql = sql + " order by " + tableInfo.getOrderBy();

        return DatabaseAccess.findObjectsByNamedSqlAsJSON(
                 conn,
                 sql,
                 properties,
                 q.getRight());
    }

    @Override
    public Long fetchObjectsCount(final Map<String, Object> properties)
            throws SQLException, IOException {
        String filter = buildFilterSql(tableInfo,null,properties.keySet());
        String sql = "select count(*) as rs from " +tableInfo.getTableName();
        if(StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;
        Object object = DatabaseAccess.getScalarObjectQuery(
                 conn,
                 sql,
                 properties);
        return NumberBaseOpt.castObjectToLong(object);
    }

    public static String buildInsertSql(TableInfo ti, final Collection<String> fields){
        StringBuilder sbInsert = new StringBuilder("insert into ");
        sbInsert.append(ti.getTableName()).append(" ( ");
        StringBuilder sbValues = new StringBuilder(" ) values ( ");
        int i=0;
        for(String f : fields){
            if(i>0){
                sbInsert.append(", ");
                sbValues.append(", ");
            }
            TableField col = ti.findFieldByName(f);
            sbInsert.append(col.getColumnName());
            sbValues.append(":").append(f);
            i++;
        }
        return sbInsert.append(sbValues).append(")").toString();
    }

    @Override
    public int saveNewObject(final Map<String, Object> object) throws SQLException {
        /*if(! checkHasAllPkColumns(object)){
            throw new SQLException("缺少主键");
        }*/
        String sql = buildInsertSql(tableInfo, object.keySet());
        return DatabaseAccess.doExecuteNamedSql(conn, sql, object);
    }

    public static String buildUpdateSql(TableInfo ti, final Collection<String> fields,final boolean exceptPk){
        StringBuilder sbUpdate = new StringBuilder("update ");
        sbUpdate.append(ti.getTableName()).append(" set ");
        int i=0;
        for(String f : fields){
            if(exceptPk && isPkColumn(ti, f))
                continue;

            if(i>0){
                sbUpdate.append(", ");
            }
            TableField col = ti.findFieldByName(f);
            sbUpdate.append(col.getColumnName());
            sbUpdate.append(" = :").append(f);
            i++;
        }
        return sbUpdate.toString();
    }

    /**
     * 更改部分属性
     * @param fields 更改部分属性 属性名 集合，应为有的Map 不允许 值为null，这样这些属性 用map就无法修改为 null
     * @param object Map
     * @return int
     */
    @Override
    public int updateObject(final Collection<String> fields,  final Map<String, Object> object) throws SQLException{
        if(! checkHasAllPkColumns(object)){
            throw new SQLException("缺少主键对应的属性。");
        }
        String sql =  buildUpdateSql(tableInfo, fields ,true) +
                " where " +  buildFilterSqlByPk(tableInfo,null);
        return DatabaseAccess.doExecuteNamedSql(conn, sql, object);
    }


    @Override
    public int updateObject(final Map<String, Object> object) throws SQLException {
        return updateObject(object.keySet(), object);
    }

    @Override
    public int mergeObject(final Collection<String> fields,
            final Map<String, Object> object) throws SQLException, IOException {
        if(! checkHasAllPkColumns(object)){
            throw new SQLException("缺少主键对应的属性。");
        }
        String sql =
                "select count(*) as checkExists from " + tableInfo.getTableName()
                + " where " +  buildFilterSqlByPk(tableInfo,null);
        Long checkExists = NumberBaseOpt.castObjectToLong(
                DatabaseAccess.getScalarObjectQuery(conn, sql, object));
        if(checkExists==null || checkExists.intValue() == 0){
            return saveNewObject(object);
        }else if(checkExists.intValue() == 1){
            return updateObject(fields, object);
        }else{
            throw new SQLException("主键属性有误，返回多个条记录。");
        }
    }

    @Override
    public int mergeObject(final Map<String, Object> object) throws SQLException, IOException {
        return mergeObject(object.keySet(), object);
    }

    @Override
    public int updateObjectsByProperties(final Collection<String> fields,
            final Map<String, Object> fieldValues,final Map<String, Object> properties)
            throws SQLException {
        String sql =  buildUpdateSql(tableInfo, fields,true) +
                " where " +  buildFilterSql(tableInfo,null,properties.keySet());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.putAll(fieldValues);
        paramMap.putAll(properties);
        return DatabaseAccess.doExecuteNamedSql(conn, sql, paramMap);
    }

    @Override
    public int updateObjectsByProperties(final Map<String, Object> fieldValues,
            final Map<String, Object> properties)
            throws SQLException {

        return updateObjectsByProperties(fieldValues.keySet(),fieldValues, properties);
    }
    @Override
    public int deleteObjectById(final Object keyValue) throws SQLException {
        if(tableInfo.getPkColumns()==null || tableInfo.getPkColumns().size()!=1)
            throw new SQLException("表"+tableInfo.getTableName()+"不是单主键表，这个方法不适用。");

        String sql =  "delete from " + tableInfo.getTableName()+
                " where " +  buildFilterSqlByPk(tableInfo,null);
        return DatabaseAccess.doExecuteNamedSql(conn, sql,
                 CollectionsOpt.createHashMap( tableInfo.getPkColumns().get(0),keyValue) );
    }

    @Override
    public int deleteObjectById(final Map<String, Object> keyValues) throws SQLException {
        if(! checkHasAllPkColumns(keyValues)){
            throw new SQLException("缺少主键对应的属性。");
        }
        String sql =  "delete from " + tableInfo.getTableName()+
                " where " +  buildFilterSqlByPk(tableInfo,null);
        return DatabaseAccess.doExecuteNamedSql(conn, sql, keyValues );
    }

    @Override
    public int deleteObjectsByProperties(final Map<String, Object> properties)
            throws SQLException {
        String sql =  "delete from " + tableInfo.getTableName()+
                " where " +  buildFilterSql(tableInfo,null,properties.keySet());
        return DatabaseAccess.doExecuteNamedSql(conn, sql, properties );

    }

    @Override
    public int insertObjectsAsTabulation(final JSONArray objects) throws SQLException {
        int resN = 0;
        for(Object object : objects){
            resN += saveNewObject((JSONObject)object);
        }
        return resN;
    }

    @Override
    public int deleteObjects(final JSONArray objects) throws SQLException {
        int resN = 0;
        for(Object object : objects){
            resN += deleteObjectById((JSONObject)object);
        }
        return resN;
    }

    @Override
    public int deleteObjectsAsTabulation(final String propertyName,final Object propertyValue) throws SQLException {
        return deleteObjectsByProperties(
                CollectionsOpt.createHashMap(propertyName,propertyValue));
    }

    @Override
    public int deleteObjectsAsTabulation(final Map<String, Object> properties) throws SQLException {
        return deleteObjectsByProperties(properties);
    }

    public class JSONObjectComparator implements Comparator<Object>{
        private TableInfo tableInfo;
        public JSONObjectComparator(TableInfo tableInfo){
            this.tableInfo = tableInfo;
        }
        @Override
        public int compare(Object o1, Object o2) {
            for(String pkc : tableInfo.getPkColumns() ){
                TableField field = tableInfo.findFieldByColumn(pkc);
                Object f1 = ((JSONObject) o1).get(field.getPropertyName());
                Object f2 = ((JSONObject) o2).get(field.getPropertyName());
                if(f1==null){
                    if(f2!=null)
                        return -1;
                }else{
                    if(f2==null)
                        return 1;
                    if( ReflectionOpt.isNumberType(f1.getClass()) &&
                            ReflectionOpt.isNumberType(f2.getClass())){
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

    @Override
    public int replaceObjectsAsTabulation(final JSONArray newObjects,final JSONArray dbObjects)
            throws SQLException {
        //insert<T> update(old,new)<T,T> delete<T>
        Triple<List<Object>, List<Pair<Object,Object>>, List<Object>>
        comRes=
            CollectionsOpt.compareTwoList(dbObjects, newObjects, new JSONObjectComparator(tableInfo));

        int resN = 0;
        if(comRes.getLeft() != null) {
            for (Object obj : comRes.getLeft()) {
                resN += saveNewObject((JSONObject) obj);
            }
        }
        if(comRes.getRight() != null) {
            for (Object obj : comRes.getRight()) {
                resN += deleteObjectById((JSONObject) obj);
            }
        }
        if(comRes.getMiddle() != null) {
            for (Pair<Object, Object> pobj : comRes.getMiddle()) {
                resN += updateObject((JSONObject) pobj.getRight());
            }
        }
        return resN;
    }

    @Override
    public int replaceObjectsAsTabulation(final JSONArray newObjects,
            final String propertyName,final Object propertyValue)
            throws SQLException, IOException {
        JSONArray dbObjects = listObjectsByProperties(
                CollectionsOpt.createHashMap(propertyName,propertyValue));
        return replaceObjectsAsTabulation(newObjects,dbObjects);
    }

    @Override
    public int replaceObjectsAsTabulation(final JSONArray newObjects,
            final Map<String, Object> properties)
            throws SQLException, IOException  {
        JSONArray dbObjects = listObjectsByProperties(properties);
        return replaceObjectsAsTabulation(newObjects,dbObjects);
    }

    /** 用表来模拟sequence
     * create table simulate_sequence (seqname varchar(100) not null primary key,
     * currvalue integer, increment integer);
     *
     * @param sequenceName sequenceName
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    public Long getSimulateSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
                 conn,
                 "SELECT count(*) hasValue from simulate_sequence "
                 + " where seqname = ?",
                new Object[]{sequenceName});
        Long l = NumberBaseOpt.castObjectToLong(object);
        if(l==0){
            DatabaseAccess.doExecuteSql(conn,
                    "insert into simulate_sequence(seqname,currvalue,increment)"
                    + " values(?,?,1)", new Object[]{sequenceName,1});
            return 1L;
        }else{
            DatabaseAccess.doExecuteSql(conn,
                    "update simulate_sequence currvalue = currvalue + increment "
                    + "where seqname= ?", new Object[]{sequenceName});
            object = DatabaseAccess.getScalarObjectQuery(
                     conn,
                     "SELECT currvalue from simulate_sequence "
                     + " where seqname = ?",
                     new Object[]{sequenceName});
        }
        return NumberBaseOpt.castObjectToLong(object);
    }


    @Override
    public List<Object[]> findObjectsBySql(String sSql, Object[] values) throws SQLException, IOException {
        return DatabaseAccess.findObjectsBySql(conn, sSql,values);
    }

    @Override
    public List<Object[]> findObjectsByNamedSql(final String sSql, final Map<String, Object> values)
            throws SQLException, IOException {
        return DatabaseAccess.findObjectsByNamedSql(conn, sSql,values);
    }

    @Override
    public JSONArray findObjectsAsJSON(final String sSql, final Object[] values, final String[] fieldnames)
            throws SQLException, IOException {
        return DatabaseAccess.findObjectsAsJSON(conn, sSql,values, fieldnames);
    }

    @Override
    public JSONArray findObjectsByNamedSqlAsJSON(final String sSql, final Map<String, Object> values,
            final String[] fieldnames) throws SQLException, IOException {
        return DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, sSql,values, fieldnames);
    }


    @Override
    public boolean doExecuteSql(final String sSql) throws SQLException {
        return DatabaseAccess.doExecuteSql(conn, sSql);
    }

    /**
     * 直接运行行带参数的 SQL,update delete insert
     *
     * @param sSql  String
     * @param values Object[]
     * @return int
     * @throws SQLException SQLException
     */
    @Override
    public int doExecuteSql(final String sSql,final Object[] values) throws SQLException {
        return DatabaseAccess.doExecuteSql(conn,sSql,values);
    }

    /**
     * 执行一个带命名参数的sql语句
     * @param sSql String
     * @param values values
     * @throws SQLException SQLException
     */
    @Override
    public int doExecuteNamedSql(final String sSql, final Map<String, Object> values)
            throws SQLException {
        return DatabaseAccess.doExecuteNamedSql(conn, sSql,values);
    }

}
