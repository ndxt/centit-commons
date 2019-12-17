package com.centit.support.database.jsonmaptable;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MySqlJsonObjectDao extends GeneralJsonObjectDao {

    public MySqlJsonObjectDao() {

    }

    public MySqlJsonObjectDao(Connection conn) {
        super(conn);
    }

    public MySqlJsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public MySqlJsonObjectDao(Connection conn, TableInfo tableInfo) {
        super(conn, tableInfo);
    }

    /**
     * 要使用这个函数首先需要在数据库中创建一下表和存储过程
     * <p>
     * DROP TABLE IF EXISTS f_mysql_sequence;
     * <p>
     * CREATE TABLE  f_mysql_sequence (
     * name varchar(50) NOT NULL,
     * currvalue int(11) NOT NULL,
     * increment int(11) NOT NULL DEFAULT '1',
     * primary key (name)
     * ) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='序列表，命名s_[table_name]';
     * <p>
     * DROP FUNCTION IF EXISTS sequence_currval;
     * <p>
     * DELIMITER //
     * <p>
     * CREATE  FUNCTION sequence_currval(seq_name VARCHAR(50)) RETURNS int(11)
     * READS SQL DATA
     * DETERMINISTIC
     * BEGIN
     * DECLARE cur_value INTEGER;
     * SET cur_value = 0;
     * SELECT currvalue INTO cur_value FROM f_mysql_sequence WHERE NAME = seq_name;
     * RETURN cur_value;
     * END//
     * <p>
     * DELIMITER ;
     * <p>
     * DROP FUNCTION IF EXISTS sequence_nextval;
     * <p>
     * DELIMITER //
     * <p>
     * CREATE  FUNCTION sequence_nextval(seq_name VARCHAR(50)) RETURNS int(11)
     * DETERMINISTIC
     * BEGIN
     * DECLARE cur_value INTEGER;
     * UPDATE f_mysql_sequence SET currvalue = currvalue + increment WHERE NAME = seq_name;
     * SELECT currvalue INTO cur_value FROM f_mysql_sequence WHERE NAME = seq_name;
     * RETURN cur_value;
     * END//
     * <p>
     * DELIMITER ;
     * <p>
     * DROP FUNCTION IF EXISTS sequence_setval;
     * <p>
     * DELIMITER //
     * <p>
     * CREATE  FUNCTION sequence_setval(seq_name VARCHAR(50),seq_value int(11)) RETURNS int(11)
     * DETERMINISTIC
     * BEGIN
     * UPDATE f_mysql_sequence SET currvalue = seq_value WHERE NAME = seq_name;
     * RETURN seq_value;
     * END//
     * <p>
     * DELIMITER ;
     *
     * @param sequenceName 序列名称
     * @return 返回当前序列
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        //return getSimulateSequenceNextValue(sequenceName);
        Object object = DatabaseAccess.getScalarObjectQuery(
            getConnect(),
            "SELECT sequence_nextval ('" + sequenceName + "')");
        return NumberBaseOpt.castObjectToLong(object);
    }

}
