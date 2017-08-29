package com.centit.support.database.orm;

import com.centit.support.json.JSONOpt;

import java.sql.Connection;
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

    public <T> T saveNewObject(T object){
        return object;
    }

    public <T> boolean updateObject(T object){
        return true;
    }

    public <T> boolean mergeObject(T object){
        return true;
    }

    public <T> T getObjectById(Object id, Class<T> type){
        return null;
    }

    public <T> boolean deleteObject(T object){
        return deleteObjectById(object,object.getClass());
    }

    public <T> boolean deleteObjectById(Object id, Class<T> type){
        return true;
    }

    public <T> List<T> queryObjectsBySql(String sql, Class<T> type){

        return null;
    }

    public <T> List<T> queryObjectsBySql(String sql, Object[] params, Class<T> type){

        return null;
    }

    public <T> List<T> queryObjectsByNamedSql(String sql,
                                              Map<String,Object> params, Class<T> type){

        return null;
    }

    public <T> T fetchObjectReference(T object,String reference){
        return object;
    }

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
