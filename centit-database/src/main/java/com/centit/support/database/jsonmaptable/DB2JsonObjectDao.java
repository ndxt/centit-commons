package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSONArray;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DB2JsonObjectDao extends GeneralJsonObjectDao {

    public DB2JsonObjectDao(){

    }

    public DB2JsonObjectDao(Connection conn) {
        super(conn);
    }

    public DB2JsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public DB2JsonObjectDao(Connection conn,TableInfo tableInfo) {
        super(conn,tableInfo);
    }

    @Override
    public JSONArray listObjectsByProperties(final Map<String, Object> properties,
            final int startPos,final int maxSize)
    throws SQLException, IOException {
        TableInfo tableInfo = this.getTableInfo();
        Pair<String,String[]> q = GeneralJsonObjectDao.buildQuerySqlByProperties(tableInfo,properties);
        return DatabaseAccess.findObjectsByNamedSqlAsJSON(
                    getConnect(),
                    QueryUtils.buildDB2LimitQuerySQL(
                            q.getLeft(),
                            startPos, maxSize),
                 properties,
                 q.getRight());
    }

    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
                 getConnect(),
                 "SELECT nextval for "
                         + sequenceName + " from sysibm.sysdummy1");
        return NumberBaseOpt.castObjectToLong(object);
    }


    @Override
    public List<Object[]> findObjectsBySql(final String sSql, final Object[] values,
            final int pageNo, final int pageSize)
            throws SQLException, IOException {
        int startPos=pageNo>1?(pageNo-1)*pageSize:0;
        return DatabaseAccess.findObjectsBySql(
                getConnect(),
                QueryUtils.buildDB2LimitQuerySQL(
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
                QueryUtils.buildDB2LimitQuerySQL(
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
                QueryUtils.buildDB2LimitQuerySQL(
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
                QueryUtils.buildDB2LimitQuerySQL(
                        sSql,
                        startPos, pageSize),
                values,fieldnames);
    }
}
