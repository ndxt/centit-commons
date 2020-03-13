package com.centit.test;

import com.alibaba.fastjson.JSON;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.OrmDaoUtils;
import com.centit.support.database.orm.ValueGenerator;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.TransactionHandler;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class TestJsonOrm {

    public static void main(String[] args) {
        testOrm();
    }

    public static void testOrm() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserCode("u0001");
        userInfo.setUserName("先腾用户");
        DataSourceDescription dbc = new DataSourceDescription();
        dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
        dbc.setUsername("metaform");
        dbc.setPassword("metaform");
        try {
            List<UserInfo> users = TransactionHandler.executeQueryInTransaction(
                dbc, (conn) -> {
                    OrmDaoUtils.saveNewObject(conn, userInfo);
                    return OrmDaoUtils.listAllObjects(
                        conn, UserInfo.class);
                }
            );
            System.out.println(JSON.toJSONString(users));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }

    @Data
    @Table(name = "F_USERINFO")
    public static class UserInfo implements java.io.Serializable {
        private static final long serialVersionUID = -1753127177790732963L;
        @Id
        @Column(name = "USER_CODE")
        @ValueGenerator(strategy = GeneratorType.RANDOM_ID, value = "7:U")
        private String userCode; // 用户代码
        @Column(name = "USER_NAME")
        private String userName; // 用户姓名
        @Column(name = "CREATE_DATE")
        @ValueGenerator(strategy = GeneratorType.FUNCTION, value = "today()")
        protected Date createDate;
    }
}
