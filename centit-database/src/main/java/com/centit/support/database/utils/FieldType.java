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
    public static final String BYTE_ARRAY = "byte[]";

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

    /**
     * @param dbObjectName 数据库中的名称（代码）
     * @return 小驼峰 名称
     */
    public static String mapPropName(String dbObjectName){
        String sTempName = dbObjectName.toLowerCase();
        String sTemp2Name = dbObjectName.toUpperCase();
        int nl = dbObjectName.length();
        if(nl<3)
            return sTempName;
        int i=0;
        String sPropName="";
        while(i<nl){
            if(sTempName.charAt(i) != '_' ){
                sPropName = sPropName + sTempName.charAt(i);
                i++;
            }else{
                i++;
                if(i==2)
                    sPropName = "";
                else if(i<nl){
                    sPropName = sPropName + sTemp2Name.charAt(i);
                    i++;
                }
            }
        }
        return sPropName;
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
        fts.put("string","string");
        fts.put("integer","integer");
        fts.put("float","float");
        fts.put("boolean","boolean");
        fts.put("date","date");
        fts.put("datetime","datetime");
        fts.put("timestamp","timestamp");
        fts.put("text","text");
        fts.put("blob","blob");
        fts.put("money","money");
        fts.put("file","file");
        return fts;
    }

    public static String mapToJavaType(String columnType, int scale){
        if("NUMBER".equalsIgnoreCase(columnType) ||
            "INTEGER".equalsIgnoreCase(columnType)||
            "DECIMAL".equalsIgnoreCase(columnType) ){
            if( scale > 0 ) {
                return FieldType.DOUBLE;
            } else {
                return FieldType.LONG;
            }
        }else if("FLOAT".equalsIgnoreCase(columnType)){
            return FieldType.FLOAT;
        }else if("CHAR".equalsIgnoreCase(columnType) ||
            "VARCHAR".equalsIgnoreCase(columnType)||
            "VARCHAR2".equalsIgnoreCase(columnType)||
            "STRING".equalsIgnoreCase(columnType) ){
            return FieldType.STRING;
        }else if("DATE".equalsIgnoreCase(columnType) ||
            "TIME".equalsIgnoreCase(columnType)||
            "DATETIME".equalsIgnoreCase(columnType) ){
            return FieldType.DATE;
        }else if("TIMESTAMP".equalsIgnoreCase(columnType) ){
            return FieldType.TIMESTAMP;
        }else if("CLOB".equalsIgnoreCase(columnType) /*||
                   "LOB".equalsIgnoreCase(columnType)||
                   "BLOB".equalsIgnoreCase(columnType)*/ ){
            return  FieldType.TEXT;
        }else if("BLOB".equalsIgnoreCase(columnType) ) {
            return FieldType.BYTE_ARRAY;
        }else if("BOOLEAN".equalsIgnoreCase(columnType) ){
            return FieldType.BOOLEAN;
        }else if("MONEY".equalsIgnoreCase(columnType) ){
            return "BigDecimal";//FieldType.MONEY;
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
