package com.centit.test.transaction;

import com.centit.support.database.utils.DBConnect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author codefan@sina.com
 *
 */
public class TransactionInvocationHandler implements InvocationHandler{
	
    private Object obj;
    
    public TransactionInvocationHandler(Object obj){
    	/*if(obj instanceof TransactionSupport)
    		throw new Exception("not support TransactionSupport");*/
        this.obj = obj;
    }
    
    /**
     * 生成代理类工厂
     * @param realObj Object
     * @return 返回生成的代理类
     */
    public static Object getProxyInstanceFactory(Object realObj)/* throws Exception*/{
        Class<?> classType = realObj.getClass();
        return Proxy.newProxyInstance(classType.getClassLoader(), 
                classType.getInterfaces(), new TransactionInvocationHandler(realObj));
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
    	Object retObj=null;
    	boolean inTransaction = false;
    	List<DBConnect> needCommitConn = new ArrayList<>(3);
        try{
        	inTransaction = method.isAnnotationPresent(Transactional.class);
        	//method.getParameterTypes()
        	//method.getParameterAnnotations()
        	if(inTransaction && args!=null){
        		for(Object arg:args){
        			if(arg instanceof DBConnect){
        				DBConnect conn = (DBConnect) arg;
        				if(conn.startTransaction()){
        					needCommitConn.add(conn);
        				}
        			}
        		}
        	}
         	retObj = method.invoke(obj, args);
        	if(inTransaction && needCommitConn.size()>0){
        		for(DBConnect conn : needCommitConn){
        			conn.commit();
        		}
        	}
        }catch(Exception e){
        	if(inTransaction && needCommitConn.size()>0){
        		for(DBConnect conn : needCommitConn){
        			conn.rollback();
        		}
        	}
        	throw e;
        }        
        return retObj;
    }

}
