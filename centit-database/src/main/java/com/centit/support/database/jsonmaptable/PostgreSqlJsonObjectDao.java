package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSONArray;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PostgreSqlJsonObjectDao extends GeneralJsonObjectDao {

    public PostgreSqlJsonObjectDao(){

    }

    public PostgreSqlJsonObjectDao(Connection conn) {
        super(conn);
    }

    public PostgreSqlJsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public PostgreSqlJsonObjectDao(Connection conn, TableInfo tableInfo) {
        super(conn,tableInfo);
    }

    @Override
    public JSONArray listObjectsByProperties(final Map<String, Object> properties,
            final int startPos,final int maxSize)
    throws SQLException, IOException {
        TableInfo tableInfo = this.getTableInfo();
        Pair<String, TableField[]> q = buildFieldSqlWithFields(tableInfo, null, true,
            GeneralJsonObjectDao.buildFilterSql(tableInfo,null, properties.keySet()),
            true, GeneralJsonObjectDao.fetchSelfOrderSql(tableInfo, properties));
        return GeneralJsonObjectDao.findObjectsByNamedSql(
                    getConnect(),
                    QueryUtils.buildPostgreSqlLimitQuerySQL(
                            q.getLeft(),
                            startPos, maxSize, false),
                 properties,
                 q.getRight());
    }

    // nextval currval
    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
                 getConnect(),
                 "SELECT nextval('" + sequenceName + "')");
        return NumberBaseOpt.castObjectToLong(object);
    }

    @Override
    public List<Object[]> findObjectsBySql(final String sSql, final Object[] values,
            final int pageNo, final int pageSize)
            throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsBySql(
                getConnect(),
                QueryUtils.buildPostgreSqlLimitQuerySQL(
                        sSql,
                        startPos, pageSize,false),
                values);
    }

    @Override
    public List<Object[]> findObjectsByNamedSql(final String sSql, final Map<String, Object> values,
            final int pageNo,final int pageSize) throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsByNamedSql(
                getConnect(),
                QueryUtils.buildPostgreSqlLimitQuerySQL(
                        sSql,
                        startPos, pageSize,false),
                values);
    }

    @Override
    public JSONArray findObjectsAsJSON(final String sSql, final Object[] values, final String[] fieldnames,
            final int pageNo, final int pageSize)
            throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsAsJSON(
                getConnect(),
                QueryUtils.buildPostgreSqlLimitQuerySQL(
                        sSql,
                        startPos, pageSize,false),
                values,fieldnames);
    }

    @Override
    public JSONArray findObjectsByNamedSqlAsJSON(final String sSql, final Map<String, Object> values,
            final String[] fieldnames, final int pageNo, final int pageSize) throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsByNamedSqlAsJSON(
                getConnect(),
                QueryUtils.buildPostgreSqlLimitQuerySQL(
                        sSql,
                        startPos, pageSize,false),
                values,fieldnames);
    }
}
