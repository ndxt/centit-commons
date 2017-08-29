package com.centit.support.database.orm;

import java.sql.ResultSet;
import java.util.List;

/**
 * Created by codefan on 17-8-27.
 */
@SuppressWarnings("unused")
public class OrmUtils {

    public static <T> T prepareObjectForInsert(T object){
        return object;
    }

    public static <T> T prepareObjectForUpdate(T object){
        return object;
    }

    public static <T> T fetchObjectFormResultSet(ResultSet rs, Class<T> clazz) {

        return null;
    }

    public static <T> List<T> fetchObjectListFormResultSet(ResultSet rs, Class<T> clazz) {

        return null;
    }

}
