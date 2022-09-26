package com.centit.support.database.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class DatabaseAccess {

    protected static final Logger logger = LoggerFactory.getLogger(DatabaseAccess.class);

    private DatabaseAccess() {
        throw new IllegalAccessError("Utility class");
    }

    public static SQLException createAccessException(String sql, SQLException e) {
        SQLException exception = new SQLException(sql + " raise " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e.getCause());
        exception.setNextException(e.getNextException());
        exception.setStackTrace(e.getStackTrace());
        return exception;
    }

    private static Object transObjectForSqlParam(Object param) {
        // 避免重复转换
        if (param instanceof java.sql.Date) {
            return param;
        } else if (param instanceof java.util.Date ||
            param instanceof java.time.LocalDate || param instanceof java.time.LocalDateTime) {
            return DatetimeOpt.convertToSqlTimestamp(
                DatetimeOpt.castObjectToDate(param));
        }

        Class<?> paramClass = param.getClass();
        if (Boolean.class.isAssignableFrom(paramClass)) {
            return BooleanBaseOpt.castObjectToBoolean(param, false) ?
                BooleanBaseOpt.ONE_CHAR_TRUE : BooleanBaseOpt.ONE_CHAR_FALSE;
        } else if (paramClass.isEnum()) {
            return EnumBaseOpt.enumToOrdinal(param);
        } else if (ReflectionOpt.isScalarType(paramClass) || param instanceof byte[]) {
            return param;
        } else {
            return JSON.toJSONString(param);
        }
    }

    /**
     * 调用数据库函数
     *
     * @param conn      数据库链接
     * @param procName  procName
     * @param sqlType   返回值类型
     * @param paramObjs paramObjs
     * @return 调用数据库函数
     * @throws SQLException SQLException
     */
    public static Object callFunction(Connection conn, String procName, int sqlType, Object... paramObjs)
        throws SQLException {
        int n = paramObjs.length;
        StringBuilder procDesc = new StringBuilder("{?=call ");
        procDesc.append(procName).append("(");
        for (int i = 0; i < n; i++) {
            if (i > 0)
                procDesc.append(",");
            procDesc.append("?");
        }
        procDesc.append(")}");
        String sqlCen = procDesc.toString();
        QueryLogUtils.printSql(logger, sqlCen);
        try (CallableStatement stmt = conn.prepareCall(sqlCen)) {
            stmt.registerOutParameter(1, sqlType);
            for (int i = 0; i < n; i++) {
                if (paramObjs[i] == null) {
                    stmt.setNull(i + 2, Types.NULL);
                } else {
                    stmt.setObject(i + 2,
                        transObjectForSqlParam(paramObjs[i]));
                }
            }
            stmt.execute();
            return stmt.getObject(1);
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sqlCen, e);
        }
    }

    /**
     * 执行一个存储过程
     *
     * @param conn      数据库链接
     * @param procName  procName
     * @param paramObjs procName
     * @return 返回是否成功
     * @throws SQLException SQLException
     */
    public static boolean callProcedure(Connection conn, String procName, Object... paramObjs)
        throws SQLException {
        int n = paramObjs.length;
        StringBuilder procDesc = new StringBuilder("{call ");
        procDesc.append(procName).append("(");
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                procDesc.append(",");
            }
            procDesc.append("?");
        }
        procDesc.append(")}");
        String sqlCen = procDesc.toString();
        QueryLogUtils.printSql(logger, sqlCen);
        try (CallableStatement stmt = conn.prepareCall(sqlCen)) {
            DatabaseAccess.setQueryStmtParameters(stmt, paramObjs);
            return stmt.execute();
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sqlCen, e);
        }
    }

    /**
     * 直接运行SQL,update delete insert
     *
     * @param conn conn
     * @param sSql sSql
     * @return 返回是否成功
     * @throws SQLException SQLException
     */
    public static boolean doExecuteSql(Connection conn, String sSql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            QueryLogUtils.printSql(logger, sSql);
            return stmt.execute();
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    public static void setQueryStmtParameters(PreparedStatement stmt, Object[] paramObjs) throws SQLException {
        //query.getParameterMetadata().isOrdinalParametersZeroBased()?0:1;
        if (paramObjs != null) {
            for (int i = 0; i < paramObjs.length; i++) {
                if (paramObjs[i] == null) {
                    stmt.setNull(i + 1, Types.NULL);
                } else {
                    stmt.setObject(i + 1,
                        transObjectForSqlParam(paramObjs[i]));
                }
            }
        }
    }

    public static void setQueryStmtParameters(PreparedStatement stmt, List<Object> paramObjs) throws SQLException {
        if (paramObjs != null) {
            for (int i = 0; i < paramObjs.size(); i++) {
                if (paramObjs.get(i) == null) {
                    stmt.setNull(i + 1, Types.NULL);
                } else {
                    stmt.setObject(i + 1,
                        transObjectForSqlParam(paramObjs.get(i)));
                }
            }
        }
    }

    public static void setQueryStmtParameters(PreparedStatement stmt, List<String> paramsName,
                                              Map<String, Object> paramObjs) throws SQLException {
        //query.getParameterMetadata().isOrdinalParametersZeroBased()?0:1;
        if (paramObjs != null) {
            for (int i = 0; i < paramsName.size(); i++) {
                Object pobj = paramObjs.get(paramsName.get(i));
                if (pobj == null) {
                    stmt.setNull(i + 1, Types.NULL);
                } else {
                    stmt.setObject(i + 1,
                        transObjectForSqlParam(pobj));
                }
            }
        }
    }

    /*
     * 直接运行行带参数的 SQL,update delete insert
     */
    public static int doExecuteSql(Connection conn, String sSql, Object[] values) throws SQLException {
        QueryLogUtils.printSql(logger, sSql, values);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            setQueryStmtParameters(stmt, values);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    /*
     * 执行一个带命名参数的sql语句
     */
    public static int doExecuteNamedSql(Connection conn, String sSql, Map<String, Object> values)
        throws SQLException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return doExecuteSql(conn, qap.getQuery(), qap.getParams());
    }

    private static JSONObject innerFetchResultSetRowToJSONObject(ResultSet rs, int cc, String[] fieldNames)
        throws SQLException, IOException {
        JSONObject jo = new JSONObject();
        for (int i = 0; i < cc; i++) {
            Object obj = rs.getObject(i + 1);
            if (obj != null) {
                if (obj instanceof Clob) {
                    jo.put(fieldNames[i], DatabaseAccess.fetchClobString((Clob) obj));
                } else if (obj instanceof Blob) {
                    jo.put(fieldNames[i], DatabaseAccess.fetchBlobBytes((Blob) obj));
                    //fetchBlobAsBase64((Blob) obj));
                } else {
                    jo.put(fieldNames[i], obj);
                }
            }
        }
        return jo;
    }

    public static JSONObject fetchResultSetRowToJSONObject(ResultSet rs, String[] fieldnames)
        throws SQLException, IOException {
        if (rs.next()) {
            int cc = rs.getMetaData().getColumnCount();
            String[] fieldNames = new String[cc];
            int asFn = 0;
            if (fieldnames != null) {
                asFn = fieldnames.length;
                System.arraycopy(fieldnames, 0, fieldNames, 0, asFn);
                /*for (int i = 0; i < asFn; i++) {
                    fieldNames[i] = fieldnames[i];
                }*/
            }
            for (int i = asFn; i < cc; i++) {
                fieldNames[i] = mapColumnNameToField(
                    rs.getMetaData().getColumnLabel(i + 1));
            }
            return innerFetchResultSetRowToJSONObject(rs, cc, fieldNames);
        }
        return null;
    }

    public static JSONArray fetchResultSetToJSONArray(ResultSet rs, String[] fieldnames)
        throws SQLException, IOException {
        JSONArray ja = new JSONArray();
        int cc = rs.getMetaData().getColumnCount();
        String[] fieldNames = new String[cc];
        int asFn = 0;
        if (fieldnames != null) {
            asFn = fieldnames.length;
            for (int i = 0; i < asFn; i++) {
                fieldNames[i] = StringUtils.isBlank(fieldnames[i]) ?
                    mapColumnNameToField(rs.getMetaData().getColumnLabel(i + 1)) :
                    fieldnames[i];
            }
        }
        for (int i = asFn; i < cc; i++) {
            fieldNames[i] = mapColumnNameToField(
                rs.getMetaData().getColumnLabel(i + 1));
        }
        while (rs.next()) {
            ja.add(innerFetchResultSetRowToJSONObject(rs, cc, fieldNames));
        }
        return ja;
    }

    @Deprecated
    public static String mapColumnNameToField(String colName) {
        return FieldType.mapToHumpName(colName, false);
        /*if(StringUtils.isBlank(colName)){
            return colName;
        }
        if(colName.indexOf('_')>=0){
            int nl = colName.length();
            char [] ch = colName.toCharArray();
            int i=0 , j=0;
            while(i<nl){
                if(ch[i]=='_'){
                    i++;
                    while(i<nl && ch[i]=='_'){
                        ch[j] = '_';
                        i++;
                        j++;
                    }
                    if(i<nl){
                        ch[j] = Character.toUpperCase(ch[i]);
                        i++;
                        j++;
                    }
                }else{
                    ch[j] = Character.toLowerCase(ch[i]);
                    i++;
                    j++;
                }
            }
            return String.valueOf(ch,0,j);
        }else if(colName.charAt(0)>='A' && colName.charAt(0)<='Z'){
            return colName.toLowerCase();
        }else {
            return colName;
        }*/
    }

    public static String[] mapColumnsNameToFields(List<String> colNames) {
        if (colNames == null || colNames.size() == 0)
            return null;
        String[] fns = new String[colNames.size()];
        for (int i = 0; i < colNames.size(); i++)
            fns[i] = FieldType.mapToHumpName(colNames.get(i), false);
        return fns;
    }

    /**
     * @param conn       数据库连接
     * @param sSql       sql语句，这个语句必须用命名参数
     * @param values     命名参数对应的变量
     * @param fieldnames 字段名称作为json中Map的key，没有这个参数的函数会自动从sql语句中解析字段名作为json中map的
     * @return JSONArray实现了List JSONObject 接口，JSONObject实现了Map String,
     * Object 接口。所以可以直接转换为List Map String,Object
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static JSONArray findObjectsAsJSON(Connection conn, String sSql, Object[] values, String[] fieldnames)
        throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql, values);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            setQueryStmtParameters(stmt, values);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs == null)
                    return new JSONArray();
                String[] fns = fieldnames;
                if (ArrayUtils.isEmpty(fns)) {
                    List<String> fields = QueryUtils.getSqlFiledNames(sSql);
                    fns = mapColumnsNameToFields(fields);
                }
                return fetchResultSetToJSONArray(rs, fns);
                //rs.close();
                //stmt.close();
                //return ja;
            }
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    public static JSONObject getObjectAsJSON(Connection conn, String sSql, Object[] values, String[] fieldnames)
        throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql, values);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            setQueryStmtParameters(stmt, values);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs == null) {
                    return null;
                }
                String[] fns = fieldnames;
                if (ArrayUtils.isEmpty(fns)) {
                    List<String> fields = QueryUtils.getSqlFiledNames(sSql);
                    fns = mapColumnsNameToFields(fields);
                }
                return fetchResultSetRowToJSONObject(rs, fns);
                //rs.close();
                //stmt.close();
                //return ja;
            }
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    public static JSONObject getObjectAsJSON(Connection conn, String sSql, Object[] values)
        throws SQLException, IOException {
        return getObjectAsJSON(conn, sSql, values, null);
    }

    public static JSONObject getObjectAsJSON(Connection conn, String sSql)
        throws SQLException, IOException {
        return getObjectAsJSON(conn, sSql, (Object[]) null, null);
    }

    public static JSONObject getObjectAsJSON(Connection conn, String sSql, Map<String, Object> values, String[]
        fieldnames)
        throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return getObjectAsJSON(conn, qap.getQuery(), qap.getParams(), fieldnames);
    }

    public static JSONObject getObjectAsJSON(Connection conn, String sSql, Map<String, Object> values)
        throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return getObjectAsJSON(conn, qap.getQuery(), qap.getParams(), null);
    }

    public static JSONArray findObjectsAsJSON(Connection conn, String sSql, Object[] values)
        throws SQLException, IOException {
        return findObjectsAsJSON(conn, sSql, values, null);
    }

    public static JSONArray findObjectsAsJSON(Connection conn, String sSql) throws SQLException, IOException {
        return findObjectsAsJSON(conn, sSql, null, null);
    }

    public static JSONArray findObjectsAsJSON(Connection conn, String sSql, Object value)
        throws SQLException, IOException {
        return findObjectsAsJSON(conn, sSql, new Object[]{value}, null);
    }

    public static JSONArray findObjectsAsJSON(Connection conn, String sSql, Object value, String[] fieldnames)
        throws SQLException, IOException {
        return findObjectsAsJSON(conn, sSql, new Object[]{value}, fieldnames);
    }

    /**
     * 执行一个带命名参数的查询并返回JSONArray
     *
     * @param conn       conn
     * @param sSql       sSql
     * @param values     values
     * @param fieldnames 对字段重命名
     * @return 执行一个带命名参数的查询并返回JSONArray
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static JSONArray findObjectsByNamedSqlAsJSON(Connection conn, String
        sSql, Map<String, Object> values,
                                                        String[] fieldnames) throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return findObjectsAsJSON(conn, qap.getQuery(), qap.getParams(), fieldnames);
    }

    /*
     * 执行一个带命名参数的查询并返回JSONArray
     */
    public static JSONArray findObjectsByNamedSqlAsJSON(Connection conn, String sSql, Map<String, Object> values)
        throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return findObjectsAsJSON(conn, qap.getQuery(), qap.getParams(), null);
    }

    public static String fetchClobString(Clob clob) throws IOException {
        try (Reader reader = clob.getCharacterStream()) {
            StringWriter writer = new StringWriter();
            char[] buf = new char[4096];
            int len;
            while ((len = reader.read(buf)) != -1) {
                writer.write(buf, 0, len);
            }
            //reader.close();
            return writer.toString();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            throw new IOException("write clob error", e);
        }
    }

    public static byte[] fetchBlobBytes(Blob blob) throws IOException {
        try (InputStream is = blob.getBinaryStream()) {
            if (is == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
            // out.writeString(new String(Base64.encodeBase64(readBytes)));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            throw new IOException("write clob error", e);
        }
    }

    public static String fetchBlobAsBase64(Blob blob) throws IOException {
        byte[] readBytes = DatabaseAccess.fetchBlobBytes(blob);
        if (readBytes == null) {
            return null;
        }
        return new String(Base64.encodeBase64(readBytes));
    }

    public static Object fetchLobField(Object fieldData, boolean blobAsBase64String) {
        if (fieldData == null)
            return null;
        try {
            if (fieldData instanceof Clob) {
                return DatabaseAccess.fetchClobString((Clob) fieldData);
            }

            if (fieldData instanceof Blob) {
                if (blobAsBase64String)
                    return DatabaseAccess.fetchBlobAsBase64((Blob) fieldData);
                else
                    return DatabaseAccess.fetchBlobBytes((Blob) fieldData);
            }

            return fieldData;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return null;
    }

    public static List<Object[]> fetchResultSetToObjectsList(ResultSet rs) throws SQLException, IOException {

        List<Object[]> datas;
        int col = rs.getMetaData().getColumnCount();
        if (rs.next()) {
            datas = new ArrayList<>();
            datas.add(innerFetchResultSetRowToObjects(rs, col));
        } else {
            return null;
        }
        while (rs.next()) {
            datas.add(innerFetchResultSetRowToObjects(rs, col));
        }
        return datas;
    }

    private static Object[] innerFetchResultSetRowToObjects(ResultSet rs, int col) throws SQLException, IOException {
        Object[] objs = new Object[col];
        for (int i = 1; i <= col; i++) {
            Object obj = rs.getObject(i);
            if (obj instanceof Clob) {
                objs[i - 1] = fetchClobString((Clob) obj);
            } else if (obj instanceof Blob) {
                objs[i - 1] = DatabaseAccess.fetchBlobBytes((Blob) obj);
                //fetchBlobAsBase64((Blob) obj);
            } else {
                objs[i - 1] = obj;
            }
        }
        return objs;
    }

    public static Object[] fetchResultSetRowToObjects(ResultSet rs) throws SQLException, IOException {
        if (rs.next()) {
            int col = rs.getMetaData().getColumnCount();
            return innerFetchResultSetRowToObjects(rs, col);
        }
        return null;
    }

    /*
     * 执行一个带参数的查询
     */
    public static List<Object[]> findObjectsBySql(Connection conn, String sSql, Object[] values)
        throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql, values);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            setQueryStmtParameters(stmt, values);
            // stmt.setMaxRows(max);
            try (ResultSet rs = stmt.executeQuery()) {
                // rs.absolute(row)
                return fetchResultSetToObjectsList(rs);
            }
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    /*
     * 执行一个带参数的查询
     *
     */
    public static List<Object[]> findObjectsBySql(Connection conn, String sSql, List<Object> values)
        throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql, values);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            setQueryStmtParameters(stmt, values);
            try (ResultSet rs = stmt.executeQuery()) {
                return fetchResultSetToObjectsList(rs);
            }
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    /*
     * 执行只带一个参数的查询
     *
     */
    public static List<Object[]> findObjectsBySql(Connection conn, String sSql, Object value)
        throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql, value);
        try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
            setQueryStmtParameters(stmt, new Object[]{value});

            try (ResultSet rs = stmt.executeQuery()) {
                return fetchResultSetToObjectsList(rs);
            }
            //rs.close();
            //stmt.close();
            //return datas;
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    /*
     * 执行只带一个参数的查询
     *
     */
    public static List<Object[]> findObjectsBySql(Connection conn, String sSql) throws SQLException, IOException {
        QueryLogUtils.printSql(logger, sSql);
        try (PreparedStatement stmt = conn.prepareStatement(sSql);
             ResultSet rs = stmt.executeQuery()) {
            return fetchResultSetToObjectsList(rs);
            //rs.close();
            //stmt.close();
            //return datas;
        } catch (SQLException e) {
            throw DatabaseAccess.createAccessException(sSql, e);
        }
    }

    /*
     * 执行一个带命名参数的查询
     *
     */
    public static List<Object[]> findObjectsByNamedSql(Connection conn,
                                                       String sSql, Map<String, Object> values)
        throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.
            createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return findObjectsBySql(conn, qap.getQuery(), qap.getParams());
    }

    /*
     * *执行一个标量查询
     */
    public static Object[] getSingleRow(Connection conn, String sSql, Map<String, Object> values)
        throws SQLException, IOException {

        List<Object[]> objList = DatabaseAccess.findObjectsByNamedSql(conn, sSql, values);
        if (objList != null && objList.size() > 0)
            return objList.get(0);
        return null;
    }

    /*
     * * 执行一个标量查询
     */
    public static Object[] getSingleRow(Connection conn, String sSql, Object[] values)
        throws SQLException, IOException {

        List<Object[]> objList = DatabaseAccess.findObjectsBySql(conn, sSql, values);
        if (objList != null && objList.size() > 0)
            return objList.get(0);
        return null;
    }

    /*
     * * 执行一个标量查询
     */
    public static Object[] getSingleRow(Connection conn, String sSql)
        throws SQLException, IOException {
        List<Object[]> objList = DatabaseAccess.findObjectsBySql(conn, sSql);
        if (objList != null && objList.size() > 0)
            return objList.get(0);
        return null;
    }

    /*
     * * 执行一个标量查询
     */
    public static Object[] getSingleRow(Connection conn, String sSql, Object value)
        throws SQLException, IOException {
        List<Object[]> objList = DatabaseAccess.findObjectsBySql(conn, sSql, value);
        if (objList != null && objList.size() > 0)
            return objList.get(0);
        return null;
    }

    /* 下面是分页查询相关的语句 */
    /*----------------------------------------------------------------------------- */
    public static Object fetchScalarObject(List<Object[]> rsDatas) {
        if (rsDatas == null || rsDatas.size() == 0)
            return null;
        Object[] firstRow = rsDatas.get(0);
        if (firstRow == null || firstRow.length == 0)
            return null;
        return firstRow[0];
    }

    public static List<Object> fetchSingleColumn(List<Object[]> rsDatas, int columnIndex) {
        if (rsDatas == null || rsDatas.size() == 0)
            return null;
        List<Object> columnData = new ArrayList<>(rsDatas.size());
        for (Object[] row : rsDatas) {
            columnData.add(row[columnIndex]);
        }
        return columnData;
    }

    public static List<Object> fetchSingleColumn(List<Object[]> rsDatas) {
        return fetchSingleColumn(rsDatas, 0);
    }

    public static List<String> fetchSingleColumnAsString(List<Object[]> rsDatas, int columnIndex) {
        if (rsDatas == null || rsDatas.size() == 0)
            return null;
        List<String> columnData = new ArrayList<>(rsDatas.size());
        for (Object[] row : rsDatas) {
            columnData.add(StringBaseOpt.castObjectToString(row[columnIndex]));
        }
        return columnData;
    }

    public static List<String> fetchSingleColumnAsString(List<Object[]> rsDatas) {
        return fetchSingleColumnAsString(rsDatas, 0);
    }

    /**
     * 执行一个标量查询
     *
     * @param conn   数据库链接
     * @param sSql   sql 语句
     * @param values 参数
     * @return 返回值 object
     * @throws SQLException SQLException
     * @throws IOException  SQLException
     */
    public static Object getScalarObjectQuery(Connection conn, String sSql, Map<String, Object> values)
        throws SQLException, IOException {
        List<Object[]> objList = DatabaseAccess.findObjectsByNamedSql(conn, sSql, values);
        return DatabaseAccess.fetchScalarObject(objList);
    }

    /*
     * * 执行一个标量查询
     */
    public static Object getScalarObjectQuery(Connection conn, String sSql, Object[] values)
        throws SQLException, IOException {
        List<Object[]> objList = DatabaseAccess.findObjectsBySql(conn, sSql, values);
        return DatabaseAccess.fetchScalarObject(objList);
    }

    /*
     * * 执行一个标量查询
     */
    public static Object getScalarObjectQuery(Connection conn, String sSql)
        throws SQLException, IOException {
        List<Object[]> objList = DatabaseAccess.findObjectsBySql(conn, sSql);
        return DatabaseAccess.fetchScalarObject(objList);
    }

    /*
     * * 执行一个标量查询
     */
    public static Object getScalarObjectQuery(Connection conn, String sSql, Object value)
        throws SQLException, IOException {
        List<Object[]> objList = DatabaseAccess.findObjectsBySql(conn, sSql, value);
        return DatabaseAccess.fetchScalarObject(objList);
    }

    /**
     * 将sql语句转换为查询总数的语句，
     * 输入的语句为不带分页查询的语句，对于oracle来说就是where中没有rowunm，Mysql语句中没有limitDB2中没有fetch等等
     *
     * @param conn   conn
     * @param sSql   sSql
     * @param values 参数应该都在条件语句中，如果标量子查询中也有参数可能会有SQLException异常
     * @return Long
     * @throws SQLException 如果标量子查询中也有参数可能会有SQLException异常
     * @throws IOException  这个查询应该不会有这个异常
     */
    public static Long queryTotalRows(Connection conn, String sSql, Object[] values)
        throws SQLException, IOException {
        Object scalarObj = DatabaseAccess.getScalarObjectQuery(conn, QueryUtils.buildGetCountSQL(sSql), values);
        return NumberBaseOpt.castObjectToLong(scalarObj);
    }

    /**
     * 将sql语句转换为查询总数的语句，
     * 输入的语句为不带分页查询的语句，对于oracle来说就是where中没有rowunm，Mysql语句中没有limitDB2中没有fetch等等
     *
     * @param conn   conn
     * @param sSql   sSql
     * @param values 命名参数
     * @return Long
     * @throws SQLException 这个查询应该不会有这个异常
     * @throws IOException  这个查询应该不会有这个异常
     */
    public static Long queryTotalRows(Connection conn, String sSql, Map<String, Object> values)
        throws SQLException, IOException {
        Object scalarObj = DatabaseAccess.getScalarObjectQuery(conn, QueryUtils.buildGetCountSQL(sSql), values);
        return NumberBaseOpt.castObjectToLong(scalarObj);
    }

    private static String makePageQuerySql(Connection conn, String sSql, int pageNo,
                                           int pageSize) throws PersistenceException {
        int offset = (pageNo > 1 && pageSize > 0) ? (pageNo - 1) * pageSize : 0;
        return QueryUtils.buildLimitQuerySQL(sSql, offset, pageSize, false, DBType.mapDBType(conn));
    }

    /** 下面是根据不同数据库生成不同的查询语句进行分页查询相关的语句 */
    /*----------------------------------------------------------------------------- */

    /**
     * 执行一个带参数的分页查询，这个是完全基于jdbc实现的，对不同的数据库来说效率差别可能很大
     *
     * @param conn     数据库连接
     * @param sSql     sql语句，这个语句必须用命名参数
     * @param values   命名参数对应的变量
     * @param pageNo   第几页
     * @param pageSize 每页大小 ，小于1 为不分页
     * @return JSONArray
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static List<Object[]> findObjectsBySql(Connection conn, String sSql, Object[] values, int pageNo,
                                                  int pageSize) throws SQLException, IOException {
        String query = makePageQuerySql(conn, sSql, pageNo, pageSize);
        if (query.equals(sSql)) { // always false
            try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
                setQueryStmtParameters(stmt, values);
                if (pageNo > 0 && pageSize > 0)
                    stmt.setMaxRows(pageNo * pageSize);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (pageNo > 1 && pageSize > 0)
                        rs.absolute((pageNo - 1) * pageSize);

                    return DatabaseAccess.fetchResultSetToObjectsList(rs);
                }
            } catch (SQLException e) {
                throw DatabaseAccess.createAccessException(sSql, e);
            }
        } else {
            return findObjectsBySql(conn, query, values);
        }
    }

    /**
     * @param conn       数据库连接
     * @param sSql       sql语句，这个语句必须用命名参数
     * @param values     命名参数对应的变量
     * @param fieldnames 对字段重命名
     * @param pageNo     第几页
     * @param pageSize   每页大小 ，小于1 为不分页
     *                   字段名称作为json中Map的key，没有这个参数的函数会自动从sql语句中解析字段名作为json中map的
     * @return JSONArray实现了List JSONObject 接口，JSONObject实现了Map String,
     * Object 接口。所以可以直接转换为List Map String,Object
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static JSONArray findObjectsAsJSON(Connection conn, String sSql, Object[] values, String[] fieldnames,
                                              int pageNo, int pageSize) throws SQLException, IOException {
        String query = makePageQuerySql(conn, sSql, pageNo, pageSize);
        if (query.equals(sSql)) { // always false
            try (PreparedStatement stmt = conn.prepareStatement(sSql)) {
                setQueryStmtParameters(stmt, values);
                if (pageNo > 0 && pageSize > 0)
                    stmt.setMaxRows(pageNo * pageSize);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs == null)
                        return new JSONArray();
                    if (pageNo > 1 && pageSize > 0)
                        rs.absolute((pageNo - 1) * pageSize);
                    String[] fns = fieldnames;
                    if (ArrayUtils.isEmpty(fns)) {
                        List<String> fields = QueryUtils.getSqlFiledNames(sSql);
                        fns = mapColumnsNameToFields(fields);
                    }
                    return fetchResultSetToJSONArray(rs, fns);
                }
            } catch (SQLException e) {
                throw DatabaseAccess.createAccessException(sSql, e);
            }
        } else {
            String[] fns = fieldnames;
            if (ArrayUtils.isEmpty(fns)) {
                List<String> fields = QueryUtils.getSqlFiledNames(sSql);
                fns = mapColumnsNameToFields(fields);
            }
            return findObjectsAsJSON(conn, query, values, fns);
        }
    }

    /**
     * @param conn     数据库连接
     * @param sSql     sql语句，这个语句必须用命名参数
     * @param values   命名参数对应的变量
     * @param pageNo   第几页
     * @param pageSize 每页大小 ，小于1 为不分页
     *                 字段名称作为json中Map的key，没有这个参数的函数会自动从sql语句中解析字段名作为json中map的
     * @return JSONArray实现了List JSONObject 接口，JSONObject实现了Map String,
     * Object 接口。所以可以直接转换为List Map String,Object
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static JSONArray findObjectsAsJSON(Connection conn, String sSql, Object[] values,
                                              int pageNo, int pageSize) throws SQLException, IOException {
        return findObjectsAsJSON(conn, sSql, values, null, pageNo, pageSize);
    }

    /**
     * 执行分页一个带命名参数的查询
     *
     * @param conn     数据库连接
     * @param sSql     sql语句，这个语句必须用命名参数
     * @param values   命名参数对应的变量
     * @param pageNo   第几页
     * @param pageSize 每页大小 ，小于1 为不分页
     * @return JSONArray
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static List<Object[]> findObjectsByNamedSql(
        Connection conn, String sSql, Map<String, Object> values,
        int pageNo, int pageSize)
        throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return findObjectsBySql(conn, qap.getQuery(), qap.getParams(), pageNo, pageSize);
    }

    /**
     * 执行一个带命名参数的查询并返回JSONArray
     *
     * @param conn       数据库连接
     * @param sSql       sql语句，这个语句必须用命名参数
     * @param values     命名参数对应的变量
     * @param fieldnames 对字段重命名
     *                   对字段重命名
     * @param pageNo     第几页
     * @param pageSize   每页大小 ，小于1 为不分页
     * @return JSONArray
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    public static JSONArray findObjectsByNamedSqlAsJSON(
        Connection conn, String sSql, Map<String, Object> values,
        String[] fieldnames, int pageNo, int pageSize) throws SQLException, IOException {
        QueryAndParams qap = QueryAndParams.createFromQueryAndNamedParams(new QueryAndNamedParams(sSql, values));
        return findObjectsAsJSON(conn, qap.getQuery(), qap.getParams(), fieldnames, pageNo, pageSize);
    }
}
