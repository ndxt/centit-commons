package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSONArray;
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

public class SqlSvrJsonObjectDao extends GeneralJsonObjectDao {

    public SqlSvrJsonObjectDao(){

    }

    public SqlSvrJsonObjectDao(Connection conn) {
        super(conn);
    }

    public SqlSvrJsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public SqlSvrJsonObjectDao(Connection conn,TableInfo tableInfo) {
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
                    QueryUtils.buildSqlServerLimitQuerySQL(
                            q.getLeft(),
                            startPos, maxSize),
                 properties,
                 q.getRight());
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
    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        return getSimulateSequenceNextValue(sequenceName);
    }

    @Override
    public List<Object[]> findObjectsBySql(final String sSql, final Object[] values,
            final int pageNo, final int pageSize)
            throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsBySql(
                getConnect(),
                QueryUtils.buildSqlServerLimitQuerySQL(
                        sSql,
                        startPos, pageSize),
                values);
    }

    @Override
    public List<Object[]> findObjectsByNamedSql(final String sSql, final Map<String, Object> values,
            final int pageNo,final int pageSize) throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsByNamedSql(
                getConnect(),
                QueryUtils.buildSqlServerLimitQuerySQL(
                        sSql,
                        startPos, pageSize),
                values);
    }

    @Override
    public JSONArray findObjectsAsJSON(final String sSql, final Object[] values, final String[] fieldnames,
            final int pageNo, final int pageSize)
            throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsAsJSON(
                getConnect(),
                QueryUtils.buildSqlServerLimitQuerySQL(
                        sSql,
                        startPos, pageSize),
                values,fieldnames);
    }

    @Override
    public JSONArray findObjectsByNamedSqlAsJSON(final String sSql, final Map<String, Object> values,
            final String[] fieldnames, final int pageNo, final int pageSize) throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsByNamedSqlAsJSON(
                getConnect(),
                QueryUtils.buildSqlServerLimitQuerySQL(
                        sSql,
                        startPos, pageSize),
                values,fieldnames);
    }
}
