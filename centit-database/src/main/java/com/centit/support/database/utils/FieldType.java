package com.centit.support.database.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class FieldType {
    static final String STRING = "string";
    static final String INTEGER= "integer";
    static final String FLOAT= "float";
    static final String DOUBLE= "double";
    static final String LONG= "long";
    static final String BOOLEAN= "boolean";
    static final String DATE= "date";
    static final String DATETIME= "datetime";
    static final String TIMESTAMP= "timestamp";
    static final String TEXT= "text";
    static final String FILE = "file";
    static final String BYTE_ARRAY = "byte[]";

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
              return "number";
          case FLOAT:
              return "number";
          case BOOLEAN:
              return "varchar2(1)";
          case DATE:
          case DATETIME:
              return "Date";
          case TEXT:
              return "clob";//长文本
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
              return "decimal";
          case FLOAT:
              return "decimal";
          case BOOLEAN:
              return "varchar(1)";
          case DATE:
          case DATETIME:
              return "datetime";
          case TEXT:
              return "text";//长文本
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
              return "INTEGER";
          case FLOAT:
              return "DECIMAL";
          case BOOLEAN:
              return "varchar(1)";
          case DATE:
          case DATETIME:
              return "Date";
          case TEXT:
              return "clob(52428800)";//长文本
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
          case FLOAT:
              return "DECIMAL";
          case BOOLEAN:
              return "varchar(1)";
          case DATE:
              return "Date";
          case DATETIME:
              return "DATETIME";
          case TEXT:
              return "clob";//长文本
          case FILE:
              return "varchar(64)";//默认记录文件的ID号
          default:
              return ft;
      }
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
        switch(dt){
        case SqlServer:
            return mapToSqlServerColumnType(ft);
          case Oracle:
              return mapToOracleColumnType(ft);
          case DB2:
              return mapToDB2ColumnType(ft);
          case MySql:
              return mapToMySqlColumnType(ft);
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
        fts.put("text","text");
        fts.put("file","file");
        return fts;
    }

    public static String mapToFieldType(String columnType,int scale)
    {
        if("NUMBER".equalsIgnoreCase(columnType) ||
            "INTEGER".equalsIgnoreCase(columnType)||
            "DECIMAL".equalsIgnoreCase(columnType) ){
            if( scale > 0 )
                return FieldType.FLOAT;
            else
                return FieldType.INTEGER;
        }else if("FLOAT".equalsIgnoreCase(columnType)){
            return FieldType.FLOAT;
        }else if("CHAR".equalsIgnoreCase(columnType) ||
            "VARCHAR".equalsIgnoreCase(columnType)||
            "VARCHAR2".equalsIgnoreCase(columnType)||
            "STRING".equalsIgnoreCase(columnType) ){
            return FieldType.STRING;
        }else if("DATE".equalsIgnoreCase(columnType) ||
            "TIME".equalsIgnoreCase(columnType) ){
            return FieldType.DATE;
        }else if("TIMESTAMP".equalsIgnoreCase(columnType)||
            "DATETIME".equalsIgnoreCase(columnType) ){
            return FieldType.DATETIME;
        }else if("CLOB".equalsIgnoreCase(columnType) /*||
                   "LOB".equalsIgnoreCase(sDBType)||
                   "BLOB".equalsIgnoreCase(sDBType)*/ ){
            return FieldType.TEXT;
        }else if("BOOLEAN".equalsIgnoreCase(columnType) ){
            return FieldType.BOOLEAN;
        }else
            return columnType;
    }

    public static String mapToJavaType(String columnType,int scale)
    {
        if("NUMBER".equalsIgnoreCase(columnType) ||
            "INTEGER".equalsIgnoreCase(columnType)||
            "DECIMAL".equalsIgnoreCase(columnType) ){
            if( scale > 0 )
                return FieldType.DOUBLE;
            else
                return FieldType.LONG;
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
        }else
            return columnType;
    }
}
