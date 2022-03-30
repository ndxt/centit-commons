package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.*;
import com.centit.support.compiler.Lexer;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;


public abstract class GeneralJsonObjectDao implements JsonObjectDao {

    /**
     * 用户自定义排序描述，  放到 filterDesc 中
     */
    public static final String SELF_ORDER_BY = "ORDER_BY";
    /**
     * 用户自定义排序字段 ， 放到 filterDesc 中
     */
    public static final String TABLE_SORT_FIELD = "sort";
    /**
     * 用户自定义排序字段的排序顺序 ， 放到 filterDesc 中
     */
    public static final String TABLE_SORT_ORDER = "order";
    protected static final Logger logger = LoggerFactory.getLogger(JsonObjectDao.class);
    private Connection conn;

    private TableInfo tableInfo;

    GeneralJsonObjectDao() {

    }

    GeneralJsonObjectDao(final TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    GeneralJsonObjectDao(final Connection conn) {
        this.conn = conn;
    }

    GeneralJsonObjectDao(final Connection conn, final TableInfo tableInfo) {
        this.conn = conn;
        this.tableInfo = tableInfo;
    }

    public static Map<String, Object> mapObjectProperties(TableInfo tableInfo, Map<String, Object> properties) {
        for (TableField field : tableInfo.getColumns()) {
            if (properties.containsKey(field.getColumnName()) &&
                !properties.containsKey(field.getPropertyName())) {
                properties.put(field.getPropertyName(),
                    properties.get(field.getColumnName()));
            }
        }
        return properties;
    }

    public static GeneralJsonObjectDao createJsonObjectDao(final Connection conn, final TableInfo tableInfo)
        throws SQLException {
        DBType dbtype = DBType.mapDBType(conn.getMetaData().getURL());
        switch (dbtype) {
            case Oracle:
            case DM:
            case KingBase:
            case GBase:
            case Oscar:
                return new OracleJsonObjectDao(conn, tableInfo);
            case DB2:
                return new DB2JsonObjectDao(conn, tableInfo);
            case SqlServer:
                return new SqlSvrJsonObjectDao(conn, tableInfo);
            case MySql:
                return new MySqlJsonObjectDao(conn, tableInfo);
            case H2:
                return new H2JsonObjectDao(conn, tableInfo);
            case PostgreSql:
                return new PostgreSqlJsonObjectDao(conn, tableInfo);
            case Access:
            default:
                throw new SQLException("不支持的数据库类型：" + dbtype.toString());
        }
    }

    public static GeneralJsonObjectDao createJsonObjectDao(DBType dbtype, Connection conn)
        throws SQLException {
        switch (dbtype) {
            case Oracle:
            case DM:
            case KingBase:
            case GBase:
            case Oscar:
                return new OracleJsonObjectDao(conn);
            case DB2:
                return new DB2JsonObjectDao(conn);
            case SqlServer:
                return new SqlSvrJsonObjectDao(conn);
            case MySql:
                return new MySqlJsonObjectDao(conn);
            case H2:
                return new H2JsonObjectDao(conn);
            case PostgreSql:
                return new PostgreSqlJsonObjectDao(conn);
            case Access:
            default:
                throw new SQLException("不支持的数据库类型：" + dbtype.toString());
        }
    }

    public static GeneralJsonObjectDao createJsonObjectDao(final Connection conn)
        throws SQLException {
        DBType dbtype = DBType.mapDBType(conn.getMetaData().getURL());
        return createJsonObjectDao(dbtype, conn);
    }

    /**
     * 返回 sql 语句 和 属性名数组
     *
     * @param ti          TableInfo
     * @param alias       String
     * @param builderType 1 非lazy字段 2 lazy字段 3 all所有字段
     * @return Pair String String []
     */
    public static String buildFieldSql(TableInfo ti, String alias, int builderType) {
        StringBuilder sBuilder = new StringBuilder();
        List<? extends TableField> columns = ti.getColumns();
        boolean addAlias = StringUtils.isNotBlank(alias);
        int i = 0;
        for (TableField col : columns) {
            if (builderType == 3 ||
                (builderType == 1 && !col.isLazyFetch()) ||
                (builderType == 2 && col.isLazyFetch())) {

                sBuilder.append(i > 0 ? ", " : " ");
                if (addAlias)
                    sBuilder.append(alias).append('.');
                sBuilder.append(col.getColumnName());
                i++;
            }
        }
        return sBuilder.toString();
    }

    /**
     * 返回 sql 语句 和 属性名数组
     *
     * @param ti     TableInfo
     * @param fields String 值返回对应的字段
     * @param alias  String
     * @param withPk 是否添加主键
     * @return Pair String String []
     */
    public static String buildPartFieldSql(TableInfo ti, Collection<String> fields, String alias, boolean withPk) {
        StringBuilder sBuilder = new StringBuilder();
        boolean addAlias = StringUtils.isNotBlank(alias);
        String aliasName = alias + ".";
        int i = 0;
        if(withPk){
            for(TableField col : ti.getPkFields()){
                sBuilder.append(i > 0 ? ", " : " ");
                if (addAlias) sBuilder.append(aliasName);
                sBuilder.append(col.getColumnName());
                i++;
            }
        }

        for (String colName : fields) {
            TableField col = ti.findFieldByName(colName);
            if (col != null && (!withPk || !col.isPrimaryKey()) ) {
                sBuilder.append(i > 0 ? ", " : " ");
                if (addAlias) sBuilder.append(aliasName);
                sBuilder.append(col.getColumnName());
                i++;
            }
        }
        return sBuilder.toString();
    }

    /**
     * 返回 sql 语句 和 属性名数组
     *
     * @param mapInfo     TableInfo 表信息
     * @param alias       String 别名
     * @param excludeLazy String 是否剔除Lazy字段
     * @param filterSql   String 过滤语句
     * @param withOrderBy String 是否排序
     * @param orderSql    String 排序语句
     * @return Pair String String []
     */
    public static Pair<String, TableField[]> buildSelectSqlWithFields(
        TableInfo mapInfo, String alias, boolean excludeLazy, String filterSql,
        boolean withOrderBy, String orderSql) {
        StringBuilder sBuilder = new StringBuilder("select");

        Pair<String, TableField[]> fieldsAndSql =
            buildFieldSqlWithFields(mapInfo, alias, excludeLazy);

        sBuilder.append(fieldsAndSql.getLeft());
        sBuilder.append(" from ").append(mapInfo.getTableName());
        if (StringUtils.isNotBlank(filterSql)) {
            sBuilder.append(" where ").append(filterSql);
        }
        if (withOrderBy) {
            if (StringUtils.isNotBlank(orderSql)) {
                sBuilder.append(" order by ").append(orderSql);
            } else if (StringUtils.isNotBlank(mapInfo.getOrderBy())) {
                sBuilder.append(" order by ").append(mapInfo.getOrderBy());
            }
        }
        return new ImmutablePair<>(sBuilder.toString(), fieldsAndSql.getRight());
    }

    public static Pair<String, TableField[]> buildFieldSqlWithFields(
        TableInfo mapInfo, String alias, boolean excludeLazy) {
        StringBuilder sBuilder = new StringBuilder();
        List<? extends TableField> columns = mapInfo.getColumns();
        TableField[] fields = new TableField[columns.size()];
        boolean addAlias = StringUtils.isNotBlank(alias);
        int i = 0;
        for (TableField col : columns) {
            if (excludeLazy && col.isLazyFetch()) {
                continue;
            }
            sBuilder.append(i > 0 ? ", " : " ");
            fields[i] = col;
            i++;
            if (addAlias) {
                sBuilder.append(alias).append('.');
            }
            sBuilder.append(col.getColumnName());
        }
        return new ImmutablePair<>(sBuilder.toString(), fields);
    }

    /**
     * 返回 sql 语句 和 属性名数组
     *
     * @param ti     TableInfo
     * @param fields String 值返回对应的字段
     * @param alias  String
     * @param withPk 是否添加主键
     * @return Pair String String []
     */
    public static Pair<String, TableField[]> buildPartFieldSqlWithFields(
        TableInfo ti, Collection<String> fields, String alias, boolean withPk) {
        StringBuilder sBuilder = new StringBuilder();
        boolean addAlias = StringUtils.isNotBlank(alias);
        String aliasName = alias + ".";
        TableField[] selectFields = new TableField[fields.size()];
        int i = 0;
        if(withPk){
            for(TableField col : ti.getPkFields()){
                sBuilder.append(i > 0 ? ", " : " ");
                if (addAlias) sBuilder.append(aliasName);
                sBuilder.append(col.getColumnName());
                selectFields[i] = col;
                i++;
            }
        }
        for (String colName : fields) {
            TableField col = ti.findFieldByName(colName);
            if (col != null && (!withPk || !col.isPrimaryKey())) {
                sBuilder.append(i > 0 ? ", " : " ");
                if (addAlias) sBuilder.append(aliasName);
                sBuilder.append(col.getColumnName());
                selectFields[i] = col;//.getPropertyName();
                i++;
            }
        }
        return new ImmutablePair<>(sBuilder.toString(), selectFields);
    }

    public static boolean checkHasAllPkColumns(TableInfo tableInfo, Map<String, Object> properties) {
        for (TableField field : tableInfo.getPkFields()) {
            if (field != null
                && properties.get(field.getPropertyName()) == null
                && properties.get(field.getColumnName()) == null) {
                return false;
            }
        }
        return true;
    }

   /*
     * 返回 sql 语句 和 包括所有属性名数组
     * @param ti TableInfo
     * @param alias String
     * @return Pair String String []
    public static String buildFieldSql(TableInfo ti, String alias){
        return buildFieldSql(ti, alias, 3);
    }
   */

    /**
     * 在sql语句中找到属性对应的字段语句
     *
     * @param querySql  sql语句
     * @param fieldName 属性
     * @return 返回的对应这个属性的语句，如果找不到返回 null
     */
    public static String mapFieldToColumnPiece(String querySql, String fieldName) {
        List<Pair<String, String>> fields = QueryUtils.getSqlFieldNamePieceMap(querySql);
        for (Pair<String, String> field : fields) {
            if (fieldName.equalsIgnoreCase(field.getLeft()) ||
                fieldName.equals(DatabaseAccess.mapColumnNameToField(field.getLeft())) ||
                fieldName.equalsIgnoreCase(field.getRight())) {
                return field.getRight();
            }
        }
        return null;
    }

    /**
     * querySql 用户检查order by 中的字段属性 对应的查询标识 比如，
     * select a+b as ab from table
     * 在 filterMap 中的 CodeBook.TABLE_SORT_FIELD (sort) 为 ab 字段 返回的排序语句为 a+b
     *
     * @param querySql  SQL语句 用来检查字段对应的查询语句 片段
     * @param filterMap 查询条件map其中包含排序属性
     * @return order by 字句
     */
    public static String fetchSelfOrderSql(String querySql, Map<String, Object> filterMap) {
        String selfOrderBy = StringBaseOpt.objectToString(filterMap.get(GeneralJsonObjectDao.SELF_ORDER_BY));
        if (StringUtils.isNotBlank(selfOrderBy)) {
            Lexer lexer = new Lexer(selfOrderBy, Lexer.LANG_TYPE_SQL);
            StringBuilder orderBuilder = new StringBuilder();
            String aWord = lexer.getAWord();
            while (StringUtils.isNotBlank(aWord)) {
                if (StringUtils.equalsAnyIgnoreCase(aWord,
                    ",", "(", ")", "order", "by", "desc", "asc", "nulls", "first", "last")) {
                    orderBuilder.append(aWord);
                } else {
                    String orderField = GeneralJsonObjectDao.mapFieldToColumnPiece(querySql, aWord);
                    if (orderField != null) {
                        orderBuilder.append(orderField);
                    } else {
                        orderBuilder.append(aWord);
                    }
                }
                orderBuilder.append(" ");
                aWord = lexer.getAWord();
            }
            return orderBuilder.toString();
        }

        String sortField = StringBaseOpt.objectToString(filterMap.get(GeneralJsonObjectDao.TABLE_SORT_FIELD));
        if (StringUtils.isNotBlank(sortField)) {
            sortField = GeneralJsonObjectDao.mapFieldToColumnPiece(querySql, sortField);
            if (sortField != null) {
                String sOrder = StringBaseOpt.objectToString(filterMap.get(GeneralJsonObjectDao.TABLE_SORT_ORDER));
                if (/*"asc".equalsIgnoreCase(sOrder) ||*/ "desc".equalsIgnoreCase(sOrder)) {
                    selfOrderBy = sortField + " desc";
                } else {
                    selfOrderBy = sortField;
                }
            }
        }
        return selfOrderBy;
    }

    public static String fetchSelfOrderSql(TableInfo ti, Map<String, Object> filterMap) {
        String selfOrderBy = StringBaseOpt.objectToString(filterMap.get(GeneralJsonObjectDao.SELF_ORDER_BY));
        if (StringUtils.isNotBlank(selfOrderBy)) {
            Lexer lexer = new Lexer(selfOrderBy, Lexer.LANG_TYPE_SQL);
            StringBuilder orderBuilder = new StringBuilder();
            String aWord = lexer.getAWord();
            while (StringUtils.isNotBlank(aWord)) {
                if (StringUtils.equalsAnyIgnoreCase(aWord,
                    ",", "(", ")", "order", "by", "desc", "asc", "nulls", "first", "last")) {
                    orderBuilder.append(aWord);
                } else {
                    TableField field = ti.findFieldByName(aWord);
                    if (field != null) {
                        orderBuilder.append(field.getColumnName());
                    } else {
                        orderBuilder.append(aWord);
                        //throw new RuntimeException("表"+ti.getTableName()
                        //+"应用排序语句"+selfOrderBy+"出错，找不到对应的排序字段");
                    }
                }
                orderBuilder.append(" ");
                aWord = lexer.getAWord();
            }
            return orderBuilder.toString();
        }

        String sortField = StringBaseOpt.objectToString(filterMap.get(GeneralJsonObjectDao.TABLE_SORT_FIELD));
        if (StringUtils.isNotBlank(sortField)) {
            TableField field = ti.findFieldByName(sortField);
            if (field != null) {
                String sOrder = StringBaseOpt.objectToString(filterMap.get(GeneralJsonObjectDao.TABLE_SORT_ORDER));
                if (/*"asc".equalsIgnoreCase(sOrder) ||*/ "desc".equalsIgnoreCase(sOrder)) {
                    selfOrderBy = field.getColumnName() + " desc";
                } else {
                    selfOrderBy = field.getColumnName();
                }
                return selfOrderBy;
            }
        }
        return ti.getOrderBy();
    }

    public static String buildFilterSqlByPk(TableInfo ti, String alias) {
        StringBuilder sBuilder = new StringBuilder();
        int i = 0;
        List<? extends TableField> pkColumns = ti.getPkFields();
        if (pkColumns == null || pkColumns.size() == 0) {
            throw new RuntimeException("表或者视图 " + ti.getTableName() + " 缺少对应主键。");
        }
        for (TableField col : pkColumns) {
            if (i > 0)
                sBuilder.append(" and ");
            if (StringUtils.isNotBlank(alias))
                sBuilder.append(alias).append('.');
            sBuilder.append(col.getColumnName()).append(" = :").append(col.getPropertyName());
            i++;
        }
        return sBuilder.toString();
    }

    public static String buildFilterSql(TableInfo ti, String alias, Collection<String> properties) {
        StringBuilder sBuilder = new StringBuilder();
        boolean needAppendAndSign = false;
        Map<String, StringBuilder> filterGroup = null;
        for (String plCol : properties) {
            int plColLength = plCol.length();
            boolean beGroup = false;
            String groupName="nog";
            String optSuffix = "none";
            int pos = 3;
            if(plColLength>3) {
                boolean haveGroupSign=(plCol.charAt(0) == 'g' || plCol.charAt(0) == 'G') &&
                    plCol.charAt(1) >= '0' && plCol.charAt(1) <= '9' &&
                    (plCol.charAt(2) == '_' || plCol.charAt(3) == '_');
                if (haveGroupSign) {
                    groupName = "g" + plCol.charAt(1);
                    beGroup = true;
                    if (plCol.charAt(3) == '_') {
                        pos = 4;
                    }
                }
                optSuffix = plCol.substring(plColLength - 3).toLowerCase();
            }
            String propName = beGroup ?
                (optSuffix.charAt(0)=='_'? plCol.substring(pos,plColLength- 3) : plCol.substring(pos)) :
                (optSuffix.charAt(0)=='_'? plCol.substring(0,plColLength- 3) : plCol);
            if(ti!=null) {
                TableField col = ti.findFieldByName(propName);
                if(col==null){
                    propName=null;
                }else {
                    propName = col.getColumnName();
                }
            }
            if(propName != null){
                StringBuilder currentBuild = null;
                if(beGroup){
                    if(filterGroup == null){
                        filterGroup = new HashMap<>(4);
                    } else {
                        currentBuild = filterGroup.get(groupName);
                    }

                    if(currentBuild==null){
                        currentBuild = new StringBuilder();
                        filterGroup.put(groupName, currentBuild);
                    } else {
                        currentBuild.append(" or ");
                    }
                } else {
                    currentBuild = sBuilder;
                    if (needAppendAndSign) {
                        sBuilder.append(" and ");
                    }
                    needAppendAndSign=true;
                }
                if (StringUtils.isNotBlank(alias)) {
                    currentBuild.append(alias).append('.');
                }
                currentBuild.append(propName);
                // opt ==  0:eq 1:gt 2:ge 3:lt 4:le 5: lk 6:in 7:ne 8:ni
                dealSuffixSql(plCol, optSuffix, currentBuild);
            }
        }
        if(filterGroup != null){
            for(Map.Entry<String, StringBuilder> ent : filterGroup.entrySet()){
                if (needAppendAndSign) {
                    sBuilder.append(" and ");
                }
                needAppendAndSign = true;
                sBuilder.append(" ( ").append(ent.getValue()).append(" )");
            }

        }
        return sBuilder.toString();
    }

    private static void dealSuffixSql(String plCol, String optSuffix, StringBuilder currentBuild) {
        switch (optSuffix) {
            case "_gt":
                currentBuild.append(" > :").append(plCol);
                break;
            case "_ge":
                currentBuild.append(" >= :").append(plCol);
                break;
            case "_lt":
                currentBuild.append(" < :").append(plCol);
                break;
            case "_le":
                currentBuild.append(" <= :").append(plCol);
                break;
            case "_lk":
                currentBuild.append(" like :").append(plCol);
                break;
            case "_in":
                currentBuild.append(" in (:").append(plCol).append(")");
                break;
            case "_ni":
                currentBuild.append(" not in (:").append(plCol).append(")");
                break;
            case "_ne":
                currentBuild.append(" <> :").append(plCol);
                break;
            case "_nv":
                currentBuild.append(" is null");
                break;
            case "_nn":
                currentBuild.append(" is not null");
                break;
            default:
                currentBuild.append(" = :").append(plCol);
                break;
        }
    }

    public static JSONArray findObjectsByNamedSql(Connection conn, String sSql, Map<String, Object> values,
                                                  TableField[] fields) throws SQLException, IOException {
        QueryAndParams sqlQuery = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return findObjectsBySql(conn, sqlQuery.getQuery(), sqlQuery.getParams(), fields);
    }

    public static JSONArray findObjectsBySql(Connection conn, String sSql, Object[] params,
                                             TableField[] fields) throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql, params);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            DatabaseAccess.setQueryStmtParameters(stmt, params);
            JSONArray objects = new JSONArray();
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs == null) {
                    return objects;
                }
                int colCount = rs.getMetaData().getColumnCount();
                //在oracle分页查询中可能会多一个 rownum 字段
                if (colCount > fields.length) {
                    colCount = fields.length;
                }
                while (rs.next()) {
                    JSONObject jo = new JSONObject();
                    for (int i = 0; i < colCount; i++) {
                        Object obj = rs.getObject(i + 1);
                        if (obj != null && fields[i]!=null) {
                            if (obj instanceof Clob) {
                                String fieldValue = DatabaseAccess.fetchClobString((Clob) obj);
                                if (FieldType.JSON_OBJECT.equals(fields[i].getFieldType())) {
                                    jo.put(fields[i].getPropertyName(), JSON.parse(fieldValue));
                                } else {
                                    jo.put(fields[i].getPropertyName(), fieldValue);
                                }
                            } else if (obj instanceof Blob) {
                                jo.put(fields[i].getPropertyName(), DatabaseAccess.fetchBlobBytes((Blob) obj));
                            } else {
                                if (FieldType.BOOLEAN.equals(fields[i].getFieldType())) {
                                    jo.put(fields[i].getPropertyName(),
                                        BooleanBaseOpt.castObjectToBoolean(obj, false));
                                }
                                if (FieldType.JSON_OBJECT.equals(fields[i].getFieldType())) {
                                    jo.put(fields[i].getPropertyName(), JSON.parse(
                                        StringBaseOpt.castObjectToString(obj)));
                                } else {
                                    jo.put(fields[i].getPropertyName(), obj);
                                }
                            }
                        }
                    }
                    objects.add(jo);
                }
                return objects;
            }
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    public static String buildCountSqlByProperties(TableInfo tableInfo, final Map<String, Object> properties) {
        String filter = GeneralJsonObjectDao.buildFilterSql(tableInfo, null, properties.keySet());
        String sql = "select count(*) as row_sum from " + tableInfo.getTableName();
        if (StringUtils.isNotBlank(filter)) {
            sql = sql + " where " + filter;
        }
        return sql;
    }

    public static String buildInsertSql(TableInfo ti, final Collection<String> fields) {
        StringBuilder sbInsert = new StringBuilder("insert into ");
        sbInsert.append(ti.getTableName()).append(" ( ");
        StringBuilder sbValues = new StringBuilder(" ) values ( ");
        int i = 0;
        for (String f : fields) {
            TableField col = ti.findFieldByName(f);
            if (col != null) {
                if (i > 0) {
                    sbInsert.append(", ");
                    sbValues.append(", ");
                }
                sbInsert.append(col.getColumnName());
                sbValues.append(":").append(f);
                i++;
            }
        }
        return sbInsert.append(sbValues).append(")").toString();
    }

    /**
     * 返回更新语句 update ，如果返回null表示 没有更新的内容
     *
     * @param ti     表信息
     * @param fields 需要更新的字段
     *               param exceptPk 是否可剔除主键
     * @return null 没有字段需要更新，
     */
    public static String buildUpdateSql(TableInfo ti, final Collection<String> fields) {
        StringBuilder sbUpdate = new StringBuilder("update ");
        sbUpdate.append(ti.getTableName()).append(" set ");
        int updateColCount = 0;
        for (String f : fields) {
            if (/*exceptPk && */ti.isParmaryKey(f))
                continue;
            TableField col = ti.findFieldByName(f);
            if (col != null) {
                if (updateColCount > 0) {
                    sbUpdate.append(", ");
                }
                sbUpdate.append(col.getColumnName());
                sbUpdate.append(" = :").append(f);
                updateColCount++;
            }
        }
        if (updateColCount == 0) {
            return null;// throw exception
        }
        return sbUpdate.toString();
    }

    public Map<String, Object> mapObjectProperties(Map<String, Object> properties) {
        return GeneralJsonObjectDao.mapObjectProperties(this.tableInfo, properties);
    }

    public Connection getConnect() {
        return this.conn;
    }

    public void setConnect(final Connection conn) {
        this.conn = conn;
    }

    @Override
    public TableInfo getTableInfo() {
        return this.tableInfo;
    }

    public void setTableInfo(final TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public boolean checkHasAllPkColumns(Map<String, Object> properties) {
        return GeneralJsonObjectDao.checkHasAllPkColumns(tableInfo, properties);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> makePkFieldMap(final Object keyValue) throws SQLException {
        if (keyValue instanceof Map) {
            //return tableInfo.fetchObjectPk((Map<String, Object>) keyValue);
            Map<String, Object> objectValues = (Map<String, Object>) keyValue;
            Map<String, Object> pkMap = new HashMap<>(4);
            for (TableField field : tableInfo.getPkFields()) {
                Object pkValue = objectValues.get(field.getPropertyName());
                if (pkValue == null) {
                    pkValue = objectValues.get(field.getColumnName());
                }
                if (pkValue == null) {
                    throw new SQLException("缺少主键对应的属性, 表：" + tableInfo.getTableName() + " ,参数：" + JSON.toJSONString(keyValue));
                }
                pkMap.put(field.getPropertyName(), pkValue);
            }
            return pkMap;
        } else {
            if (tableInfo.countPkColumn() != 1)
                throw new SQLException("表" + tableInfo.getTableName() + "不是单主键表，这个方法不适用。");
            return CollectionsOpt.createHashMap(tableInfo.getPkFields().get(0).getPropertyName(), keyValue);
        }
    }

    @Override
    public JSONObject getObjectById(final Object keyValue) throws SQLException, IOException {
        Map<String, Object> keyValues = makePkFieldMap(keyValue);
        Pair<String, TableField[]> q = buildSelectSqlWithFields(tableInfo, null, false,
            buildFilterSqlByPk(tableInfo, null), false, null);

        JSONArray ja = findObjectsByNamedSql(
            conn, q.getLeft(),
            keyValues,
            q.getRight());
        return (JSONObject) CollectionsOpt.fetchFirstItem(ja);
    }

    @Override
    public JSONObject getObjectByProperties(final Map<String, Object> properties) throws SQLException, IOException {

        Pair<String, TableField[]> q = buildSelectSqlWithFields(tableInfo, null, false,
            GeneralJsonObjectDao.buildFilterSql(tableInfo, null, properties.keySet()),
            false, null);
        JSONArray ja = findObjectsByNamedSql(
            conn, q.getLeft(),
            properties,
            q.getRight());
        return (JSONObject) CollectionsOpt.fetchFirstItem(ja);
    }

    /**
     * 查询语句默认 不包括 lazy 字段
     *
     * @param properties 查询字段属性
     * @return 语句和字段名称
     */
    @Override
    public JSONArray listObjectsByProperties(final Map<String, Object> properties) throws SQLException, IOException {
        Pair<String, TableField[]> q = buildSelectSqlWithFields(tableInfo, null, true,
            GeneralJsonObjectDao.buildFilterSql(tableInfo, null, properties.keySet()),
            true, GeneralJsonObjectDao.fetchSelfOrderSql(tableInfo, properties));
        return findObjectsByNamedSql(
            conn,
            q.getLeft(),
            properties,
            q.getRight());
    }

    @Override
    public Long fetchObjectsCount(final Map<String, Object> properties)
        throws SQLException, IOException {
        String filter = buildFilterSql(tableInfo, null, properties.keySet());
        String sql = "select count(*) as rs from " + tableInfo.getTableName();
        if (StringUtils.isNotBlank(filter))
            sql = sql + " where " + filter;
        Object object = DatabaseAccess.getScalarObjectQuery(
            conn,
            sql,
            properties);
        return NumberBaseOpt.castObjectToLong(object);
    }

    @Override
    public int saveNewObject(final Map<String, Object> object) throws SQLException {
        /*if(! checkHasAllPkColumns(object)){
            throw new SQLException("缺少主键");
        }*/
        String sql = buildInsertSql(tableInfo, object.keySet());
        return DatabaseAccess.doExecuteNamedSql(conn, sql, object);
    }

    /**
     * 更改部分属性
     *
     * @param fields 更改部分属性 属性名 集合，应为有的Map 不允许 值为null，这样这些属性 用map就无法修改为 null
     * @param object Map
     * @return int
     */
    @Override
    public int updateObject(final Collection<String> fields, final Map<String, Object> object) throws SQLException {
        if (!checkHasAllPkColumns(object)) {
            throw new SQLException("缺少主键对应的属性。");
        }
        String sql = buildUpdateSql(tableInfo, fields);
        if (sql == null) {
            return 0;
        }
        sql = sql + (" where " + buildFilterSqlByPk(tableInfo, null));
        return DatabaseAccess.doExecuteNamedSql(conn, sql, object);
    }

    @Override
    public int updateObject(final Map<String, Object> object) throws SQLException {
        return updateObject(object.keySet(), object);
    }

    @Override
    public int mergeObject(final Collection<String> fields,
                           final Map<String, Object> object) throws SQLException, IOException {
        if (!checkHasAllPkColumns(object)) {
            throw new SQLException("缺少主键对应的属性。");
        }
        String sql =
            "select count(*) as checkExists from " + tableInfo.getTableName()
                + " where " + buildFilterSqlByPk(tableInfo, null);
        Long checkExists = NumberBaseOpt.castObjectToLong(
            DatabaseAccess.getScalarObjectQuery(conn, sql, object));
        if (checkExists == null || checkExists.intValue() == 0) {
            return saveNewObject(object);
        } else if (checkExists.intValue() == 1) {
            return updateObject(fields, object);
        } else {
            throw new SQLException("主键属性有误，返回多个条记录。");
        }
    }

    @Override
    public int mergeObject(final Map<String, Object> object) throws SQLException, IOException {
        return mergeObject(object.keySet(), object);
    }

    @Override
    public int updateObjectsByProperties(final Collection<String> fields,
                                         final Map<String, Object> fieldValues, final Map<String, Object> properties)
        throws SQLException {

        String sql = buildUpdateSql(tableInfo, fields);
        if (sql == null) {
            return 0;
        }
        sql = sql + " where " + buildFilterSql(tableInfo, null, properties.keySet());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.putAll(fieldValues);
        paramMap.putAll(properties);
        return DatabaseAccess.doExecuteNamedSql(conn, sql, paramMap);
    }

    @Override
    public int updateObjectsByProperties(final Map<String, Object> fieldValues,
                                         final Map<String, Object> properties)
        throws SQLException {

        return updateObjectsByProperties(fieldValues.keySet(), fieldValues, properties);
    }

    @Override
    public int deleteObjectById(final Object keyValue) throws SQLException {
        Map<String, Object> keyValues = makePkFieldMap(keyValue);
        String sql = "delete from " + tableInfo.getTableName() +
            " where " + buildFilterSqlByPk(tableInfo, null);
        return DatabaseAccess.doExecuteNamedSql(conn, sql, keyValues);
    }

    @Override
    public int deleteObjectsByProperties(final Map<String, Object> properties)
        throws SQLException {
        String sql = "delete from " + tableInfo.getTableName() +
            " where " + buildFilterSql(tableInfo, null, properties.keySet());
        return DatabaseAccess.doExecuteNamedSql(conn, sql, properties);

    }

    @Override
    public int insertObjectsAsTabulation(final List<Map<String, Object>> objects) throws SQLException {
        int resN = 0;
        for (Map<String, Object> object : objects) {
            resN += saveNewObject(object);
        }
        return resN;
    }

    @Override
    public int deleteObjects(final List<Object> objects) throws SQLException {
        int resN = 0;
        for (Object object : objects) {
            resN += deleteObjectById(object);
        }
        return resN;
    }

    @Override
    public int deleteObjectsAsTabulation(final String propertyName, final Object propertyValue) throws SQLException {
        return deleteObjectsByProperties(
            CollectionsOpt.createHashMap(propertyName, propertyValue));
    }

    @Override
    public int deleteObjectsAsTabulation(final Map<String, Object> properties) throws SQLException {
        return deleteObjectsByProperties(properties);
    }

    /**
     * 比较两个对象，判断是否需要更新，这个仅用于只更新有值的字段
     * @param oldObject 就对象，一般指数据库中的对象
     * @param newObject 新对象，一般指待更新的对象
     * @return 是否需要更新
     */
    public static boolean checkNeedUpdate(Map<String, Object> oldObject, Map<String, Object> newObject){
        for(Map.Entry<String, Object> ent : newObject.entrySet()){
            Object oldValue = oldObject.get(ent.getKey());
            Object newValue = ent.getValue();
            if(newValue!=null && !newValue.equals(oldValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int replaceObjectsAsTabulation(final List<Map<String, Object>> newObjects, final List<Map<String, Object>> dbObjects)
        throws SQLException {
        //insert<T> update(old,new)<T,T> delete<T>
        Triple<List<Map<String, Object>>, List<Pair<Map<String, Object>, Map<String, Object>>>, List<Map<String, Object>>>
            comRes =
            CollectionsOpt.compareTwoList(dbObjects, newObjects, new JSONObjectComparator(tableInfo));

        int resN = 0;
        if (comRes.getLeft() != null) {
            for (Object obj : comRes.getLeft()) {
                resN += saveNewObject((JSONObject) obj);
            }
        }
        if (comRes.getRight() != null) {
            for (Object obj : comRes.getRight()) {
                resN += deleteObjectById(obj);
            }
        }
        if (comRes.getMiddle() != null) {
            for (Pair<Map<String, Object>, Map<String, Object>> pobj : comRes.getMiddle()) {
                //对比减少不必要的更新
                if(checkNeedUpdate(pobj.getLeft(), pobj.getRight())) {
                    resN += updateObject(pobj.getRight());
                }
            }
        }
        return resN;
    }

    @Override
    public int replaceObjectsAsTabulation(final List<Map<String, Object>> newObjects,
                                          final String propertyName, final Object propertyValue)
        throws SQLException, IOException {
        JSONArray dbObjects = listObjectsByProperties(
            CollectionsOpt.createHashMap(propertyName, propertyValue));
        return replaceObjectsAsTabulation(newObjects, (List) dbObjects);
    }

    @Override
    public int replaceObjectsAsTabulation(final List<Map<String, Object>> newObjects,
                                          final Map<String, Object> properties)
        throws SQLException, IOException {
        JSONArray dbObjects = listObjectsByProperties(properties);
        return replaceObjectsAsTabulation(newObjects, (List) dbObjects);
    }

    /**
     * 用表来模拟sequence
     * create table simulate_sequence (seqname varchar(100) not null primary key,
     * currvalue integer, increment integer);
     *
     * @param sequenceName sequenceName
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public Long getSimulateSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
            conn,
            "SELECT count(*) hasValue from simulate_sequence "
                + " where seqname = ?",
            new Object[]{sequenceName});
        Long l = NumberBaseOpt.castObjectToLong(object);
        if (l == 0) {
            DatabaseAccess.doExecuteSql(conn,
                "insert into simulate_sequence(seqname,currvalue,increment)"
                    + " values(?,?,1)", new Object[]{sequenceName, 1});
            return 1L;
        } else {
            DatabaseAccess.doExecuteSql(conn,
                "update simulate_sequence set currvalue = currvalue + increment "
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
        return DatabaseAccess.findObjectsBySql(conn, sSql, values);
    }

    @Override
    public List<Object[]> findObjectsByNamedSql(final String sSql, final Map<String, Object> values)
        throws SQLException, IOException {
        return DatabaseAccess.findObjectsByNamedSql(conn, sSql, values);
    }

    @Override
    public JSONArray findObjectsAsJSON(final String sSql, final Object[] values, final String[] fieldnames)
        throws SQLException, IOException {
        return DatabaseAccess.findObjectsAsJSON(conn, sSql, values, fieldnames);
    }

    @Override
    public JSONArray findObjectsByNamedSqlAsJSON(final String sSql, final Map<String, Object> values,
                                                 final String[] fieldnames) throws SQLException, IOException {
        return DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, sSql, values, fieldnames);
    }

    @Override
    public boolean doExecuteSql(final String sSql) throws SQLException {
        return DatabaseAccess.doExecuteSql(conn, sSql);
    }

    /**
     * 直接运行行带参数的 SQL,update delete insert
     *
     * @param sSql   String
     * @param values Object[]
     * @return int
     * @throws SQLException SQLException
     */
    @Override
    public int doExecuteSql(final String sSql, final Object[] values) throws SQLException {
        return DatabaseAccess.doExecuteSql(conn, sSql, values);
    }

    /**
     * 执行一个带命名参数的sql语句
     *
     * @param sSql   String
     * @param values values
     * @throws SQLException SQLException
     */
    @Override
    public int doExecuteNamedSql(final String sSql, final Map<String, Object> values)
        throws SQLException {
        return DatabaseAccess.doExecuteNamedSql(conn, sSql, values);
    }

    @Override
    public JSONArray listObjectsByProperties(final Map<String, Object> properties,
                                             final int startPos, final int maxSize)
        throws SQLException, IOException {
        TableInfo tableInfo = this.getTableInfo();
        Pair<String, TableField[]> q = buildSelectSqlWithFields(tableInfo, null, true,
            GeneralJsonObjectDao.buildFilterSql(tableInfo, null, properties.keySet()),
            true, GeneralJsonObjectDao.fetchSelfOrderSql(tableInfo, properties));
        return GeneralJsonObjectDao.findObjectsByNamedSql(
            conn,
            QueryUtils.buildLimitQuerySQL(
                q.getLeft(),
                startPos, maxSize, false, DBType.mapDBType(conn)),
            properties,
            q.getRight());
    }

    @Override
    public List<Object[]> findObjectsBySql(final String sSql, final Object[] values,
                                           final int pageNo, final int pageSize)
        throws SQLException, IOException {
        int startPos = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        return DatabaseAccess.findObjectsBySql(
            conn,
            QueryUtils.buildLimitQuerySQL(
                sSql,
                startPos, pageSize, false, DBType.mapDBType(conn)),
            values);
    }

    @Override
    public List<Object[]> findObjectsByNamedSql(final String sSql, final Map<String, Object> values,
                                                final int pageNo, final int pageSize) throws SQLException, IOException {
        int startPos = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        return DatabaseAccess.findObjectsByNamedSql(
            conn,
            QueryUtils.buildLimitQuerySQL(
                sSql,
                startPos, pageSize, false, DBType.mapDBType(conn)),
            values);
    }

    private JSONArray transObjectList(JSONArray ja, String[] fieldnames) {
        if (ja == null || ja.size() == 0 ) {
            return ja;
        }
        List<TableField> fields = new ArrayList<>(5);
        if(fieldnames==null) {
            for (TableField field  : tableInfo.getColumns()) {
                if (FieldType.BOOLEAN.equals(field.getFieldType())
                        || FieldType.JSON_OBJECT.equals(field.getFieldType())) {
                    fields.add(field);
                }
            }
        } else {
            for (String fieldName : fieldnames) {
                TableField field = tableInfo.findFieldByName(fieldName);
                if (field != null && (
                    FieldType.BOOLEAN.equals(field.getFieldType())
                        || FieldType.JSON_OBJECT.equals(field.getFieldType()))) {
                    fields.add(field);
                }
            }
        }

        if (fields.size() > 0) {
            for (Object jo : ja) {
                Map<String, Object> json = (Map<String, Object>) jo;
                for (TableField field : fields) {
                    if (FieldType.BOOLEAN.equals(field.getFieldType())) {
                        Object v = json.get(field.getPropertyName());
                        if (v != null) {
                            json.put(field.getPropertyName(),
                                BooleanBaseOpt.castObjectToBoolean(v, false));
                        }
                    } else if (FieldType.JSON_OBJECT.equals(field.getFieldType())) {
                        Object v = json.get(field.getPropertyName());
                        if (v != null) {
                            json.put(field.getPropertyName(),
                                JSON.parse(StringBaseOpt.castObjectToString(v)));
                        }
                    }
                }
            }
        }
        return ja;
    }

    @Override
    public JSONArray findObjectsAsJSON(final String sSql, final Object[] values, final String[] fieldnames,
                                       final int pageNo, final int pageSize)
        throws SQLException, IOException {
        int startPos = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        return transObjectList(DatabaseAccess.findObjectsAsJSON(
            conn,
            QueryUtils.buildLimitQuerySQL(
                sSql,
                startPos, pageSize, false, DBType.mapDBType(conn)),
            values, fieldnames), fieldnames);
    }

    @Override
    public JSONArray findObjectsByNamedSqlAsJSON(final String sSql, final Map<String, Object> values,
                                                 final String[] fieldnames, final int pageNo, final int pageSize) throws SQLException, IOException {
        int startPos = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        return transObjectList(DatabaseAccess.findObjectsByNamedSqlAsJSON(
            conn,
            QueryUtils.buildLimitQuerySQL(
                sSql,
                startPos, pageSize, false, DBType.mapDBType(conn)),
            values, fieldnames), fieldnames);
    }

    public class JSONObjectComparator implements Comparator<Map<String, Object>> {
        private TableInfo tableInfo;

        public JSONObjectComparator(TableInfo tableInfo) {
            this.tableInfo = tableInfo;
        }

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            for (TableField field : tableInfo.getPkFields()) {
                Object f1 = o1.get(field.getPropertyName());
                Object f2 = o2.get(field.getPropertyName());
                int compareRes = GeneralAlgorithm.compareTwoObject(f1, f2);
                if (compareRes != 0) {
                    return compareRes;
                }
            }
            return 0;
        }
    }
}
