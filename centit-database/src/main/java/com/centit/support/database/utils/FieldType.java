package com.centit.support.database.utils;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public abstract class FieldType {
    public static final String STRING = "string";
    public static final String INTEGER= "integer";
    public static final String FLOAT= "float";
    /**
     * 要考虑 定点数，用于存储金钱
     */
    public static final String MONEY= "money";
    public static final String DOUBLE= "double";
    public static final String LONG= "long";
    public static final String BOOLEAN= "boolean";
    public static final String DATE= "date";
    public static final String DATETIME= "datetime";
    public static final String TIMESTAMP= "timestamp";
    public static final String TEXT= "text";
    public static final String FILE = "file";
    public static final String BYTE_ARRAY = "bytes";

    /**
     * @param st java类 名称
     * @return java类 短名
     */
    public static String trimType(String st ){
        int p = st.lastIndexOf('.');
        if(p>0)
            return  st.substring(p+1);
        return st;
    }

    public static String mapToHumpName(String dbObjectName, boolean firstUpCase){
        String sTempName = dbObjectName.toLowerCase();
        int nl = dbObjectName.length();
        int i=0;

        StringBuilder sClassName = new StringBuilder();
        boolean upCase = firstUpCase;
        while(i<nl){
            if(sTempName.charAt(i) == '_' ){
               if(sClassName.length()==1){
                    sClassName.delete(0,1);
                    upCase = firstUpCase;
                }else {
                    upCase = true;
                }
            }else{
                if(upCase) {
                    sClassName.append((char)(sTempName.charAt(i)-32));
                    upCase = false;
                }else {
                    sClassName.append(sTempName.charAt(i));
                }
            }
            i++;
        }
        return sClassName.toString();
    }

    /**
     * @param dbObjectName 数据库中的名称（代码）
     * @return 大驼峰 名称
     */
    public static String mapClassName(String dbObjectName){
        return mapToHumpName(dbObjectName, true);
    }


    /**
     * @param dbObjectName 数据库中的名称（代码）
     * @return 小驼峰 名称
     */
    public static String mapPropName(String dbObjectName){
        return mapToHumpName(dbObjectName, false);
    }
    /**
     * 转换到Oracle的字段
     * @param ft String
     * @return String
     */
    public static String mapToOracleColumnType(String ft){
        switch(ft){
            case STRING:
                return "varchar2";
            case INTEGER:
            case LONG:
                return "number";
            case FLOAT:
            case DOUBLE:
                return "number";
            case MONEY:
                return "number(30,4)";
            case BOOLEAN:
                return "varchar2(1)";
            case DATE:
            case DATETIME:
                return "Date";
            case TIMESTAMP:
                return "TimeStamp";
            case TEXT:
                return "clob";//长文本
            case BYTE_ARRAY:
                return "blob";//大字段
            case FILE:
                return "varchar2(64)";//默认记录文件的ID号
            default:
                return ft;
      }
    }

    /**
     * 转换到Oracle的字段
     * @param ft String
     * @return String
     */
    public static String mapToSqlServerColumnType(String ft){
        switch(ft){
            case STRING:
                return "varchar";
            case INTEGER:
            case LONG:
                return "decimal";
            case DOUBLE:
            case FLOAT:
                return "decimal";
            case MONEY:
                return "decimal(30,4)";
            case BOOLEAN:
                return "varchar(1)";
            case DATE:
            case DATETIME:
                return "datetime";
            case TIMESTAMP:
                return "TimeStamp";
            case TEXT:
                return "text";//长文本
            case BYTE_ARRAY:
                return "VarBinary(MAX)";
            case FILE:
                return "varchar(64)";//默认记录文件的ID号
            default:
                return ft;
      }
    }

    /**
     * 转换到Oracle的字段
     * @param ft String
     * @return String
     */
    public static String mapToDB2ColumnType(String ft){
        switch(ft){
        case STRING:
            return "varchar";
            case INTEGER:
            case LONG:
                 return "INTEGER";
            case DOUBLE:
            case FLOAT:
                return "DECIMAL";
            case MONEY:
                return "DECIMAL(30,4)";
            case BOOLEAN:
                return "varchar(1)";
            case DATE:
            case DATETIME:
                return "Date";
            case TIMESTAMP:
                return "TimeStamp";
            case TEXT:
                return "clob(52428800)";//长文本
            case BYTE_ARRAY:
                return "BLOB";
            case FILE:
                return "varchar(64)";//默认记录文件的ID号
            default:
                return ft;
      }
    }

    /**
     * 转换到Oracle的字段
     * @param ft String
     * @return String
     */
    public static String mapToMySqlColumnType(String ft){
        switch(ft){
            case STRING:
                return "varchar";
            case INTEGER:
                return "INT";
            case LONG:
                  return "BIG INT";
            case MONEY:
                return "DECIMAL(30,4)";
            case FLOAT:
                return "FLOAT";
            case DOUBLE:
                return "DOUBLE";
            case BOOLEAN:
                return "varchar(1)";
            case DATE:
                return "Date";
            case DATETIME:
                return "DATETIME";
            case TIMESTAMP:
                return "TimeStamp";
            case TEXT:
                return "clob";//长文本
            case FILE:
                return "varchar(64)";//默认记录文件的ID号
            case BYTE_ARRAY:
                return "VARBINARY";
            default:
                return ft;
        }
    }

    public static String mapToH2ColumnType(String ft){
        return mapToMySqlColumnType(ft);
    }

    public static String mapToPostgreSqlColumnType(String ft){
        return mapToMySqlColumnType(ft);
    }

    /**
     *
     * @param dt 数据库类别
     * @param ft 字段 java 类别
     * @return String
     */
    public static String mapToDatabaseType(String ft, DBType dt ){
        if(dt==null)
            return ft;
        switch(dt) {
            case SqlServer:
                return mapToSqlServerColumnType(ft);
            case Oracle:
                return mapToOracleColumnType(ft);
            case DB2:
                return mapToDB2ColumnType(ft);
            case MySql:
                return mapToMySqlColumnType(ft);
            case H2:
                return mapToH2ColumnType(ft);
            case PostgreSql:
                return mapToPostgreSqlColumnType(ft);
            default:
                return mapToOracleColumnType(ft);
        }

    }

    public static Map<String,String> getAllTypeMap(){
        Map<String,String> fts = new HashMap<>();
        fts.put(FieldType.STRING,FieldType.STRING);
        fts.put(FieldType.INTEGER,FieldType.INTEGER);
        fts.put(FieldType.FLOAT,FieldType.FLOAT);
        fts.put(FieldType.MONEY,FieldType.MONEY);
        fts.put(FieldType.DOUBLE,FieldType.DOUBLE);
        fts.put(FieldType.LONG,FieldType.LONG);
        fts.put(FieldType.BOOLEAN,FieldType.BOOLEAN);
        fts.put(FieldType.DATE,FieldType.DATE);
        fts.put(FieldType.DATETIME,FieldType.DATETIME);
        fts.put(FieldType.TIMESTAMP,FieldType.TIMESTAMP);
        fts.put(FieldType.TEXT,FieldType.TEXT);
        fts.put(FieldType.FILE,FieldType.FILE);
        fts.put(FieldType.BYTE_ARRAY,FieldType.BYTE_ARRAY);
        return fts;
    }

    public static String mapToJavaType(String columnType, int scale){
        if("NUMBER".equalsIgnoreCase(columnType) ||
            "INTEGER".equalsIgnoreCase(columnType)||
            "DECIMAL".equalsIgnoreCase(columnType) ){
            if( scale > 0 ) {
                return "double";
            } else {
                return "long";
            }
        }else if("CHAR".equalsIgnoreCase(columnType) ||
            "VARCHAR".equalsIgnoreCase(columnType)||
            "VARCHAR2".equalsIgnoreCase(columnType)||
            FieldType.STRING.equalsIgnoreCase(columnType) ||
            FieldType.FILE.equalsIgnoreCase(columnType) ||
            FieldType.BOOLEAN.equalsIgnoreCase(columnType)){
            return "String";
        }else if("DATE".equalsIgnoreCase(columnType) ||
            "TIME".equalsIgnoreCase(columnType)||
            "DATETIME".equalsIgnoreCase(columnType) ){
            return "Date";
        }else if("TIMESTAMP".equalsIgnoreCase(columnType) ){
            return "Timestamp";
        }else if("CLOB".equalsIgnoreCase(columnType) ||
                "TEXT".equalsIgnoreCase(columnType) ){
            return  "String";
        }else if("BLOB".equalsIgnoreCase(columnType) ||
                 FieldType.BYTE_ARRAY.equalsIgnoreCase(columnType)) {
            return "byte[]";
        }else if(FieldType.MONEY.equalsIgnoreCase(columnType) ){
            return "BigDecimal";//FieldType.MONEY;
        }else if(FieldType.FLOAT.equalsIgnoreCase(columnType)){
            return "float";
        }else if(FieldType.INTEGER.equalsIgnoreCase(columnType)){
            return "int";
        } else if(FieldType.DOUBLE.equalsIgnoreCase(columnType)){
            return "double";
        }else if(FieldType.LONG.equalsIgnoreCase(columnType)){
            return "long";
        }else {
            return columnType;
        }
    }

    /**
     * map java.sql.Type to javaType
     * @param dbType java.sql.Type
     * @return java type
     * @see Types
     */
    public static String mapToJavaType(int dbType){
        switch(dbType) {
            case -6:
            case -5:
            case 5:
            case 4:
            case 2:
                return "int";
            case 6:
            case 7:
                return "float";
            case 8:
                return "double";
            case 3:
                return "long";
            case -1:
            case 1:
            case 12:
                return "String";
            case 91:
            case 92:
                return "Date";
            case 93:
            case 2013:
            case 2014:
                return "Timestamp";
            case -2:
            case -3:
            case -4:
            case 2004:
                return "byte[]";
            /*case 2005:
                return "String";
            case 16:
                return "String";*/
            default:
                return "String";
        }
    }

    public static String mapToFieldType(int dbType){
        switch(dbType) {
            case -6:
            case -5:
            case 5:
            case 4:
            case 2:
                return FieldType.INTEGER;
            case 6:
            case 7:
                return FieldType.FLOAT;
            case 8:
                return FieldType.DOUBLE;
            case 3:
                return FieldType.LONG;
            case -1:
            case 1:
            case 12:
                return FieldType.STRING;
            case 91:
                return FieldType.DATE;
            case 92:
                return FieldType.DATETIME;
            case 93:
            case 2013:
            case 2014:
                return FieldType.TIMESTAMP;
            case -2:
            case -3:
            case -4:
            case 2004:
                return FieldType.BYTE_ARRAY;
            case 2005:
                return FieldType.TEXT;
            case 16:
                return FieldType.BOOLEAN;
            default:
                return FieldType.STRING;
        }
    }
}
