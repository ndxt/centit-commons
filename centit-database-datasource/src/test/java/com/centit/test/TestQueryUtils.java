package com.centit.test;

import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.LeftRightPair;
import com.centit.support.database.utils.QueryAndNamedParams;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestQueryUtils {


    public static void testGetTemplateParams() {


        String queryStatement = "select  eb.estimateDatOld"+
          " [ :(like,uppercase) xmqc,:(like,uppercase)newparames, user.Info.username : (like,uppercase) xmqc3 | and eb.PRONAME like:xmqc] ";
        for(String s : QueryUtils.getSqlTemplateParameters(queryStatement)){
            System.out.println(s);
        }
        System.out.println("\ndone!");
    }

    public static void main(String[] args) {
        testTranslateQuery2();
        System.out.println("\nDone!");
        //分析表别名， 格式为 TableNameOrClass:alias,TableNameOrClass:alias,.....
        /*String tablesDesc =  "TableNameOrClass:alias,  TableNameOrClass alias, addasf ";
        String [] tables = tablesDesc.split(",");
        for(String tableDesc:tables){
            Lexer tableLexer = new Lexer(tableDesc);
            String tableName = tableLexer.getAWord();
            String aliasName = tableLexer.getAWord();
            if(":".equals(aliasName)){
                aliasName = tableLexer.getAWord();
            }
            System.out.println(tableName +"-" + aliasName );
        }*/

/*
        System.out.println(GeneratorTime.UPDATE.matchTime(GeneratorTime.READ));
        System.out.println(GeneratorTime.READ.matchTime(GeneratorTime.NEW));
        System.out.println(GeneratorTime.UPDATE.matchTime(GeneratorTime.NEW));
        System.out.println(GeneratorTime.NEW_UPDATE.matchTime(GeneratorTime.UPDATE));
        System.out.println(GeneratorTime.NEW_UPDATE.matchTime(GeneratorTime.NEW));
        System.out.println(GeneratorTime.NEW_UPDATE.matchTime(GeneratorTime.READ));
        System.out.println(GeneratorTime.ALWAYS.matchTime(GeneratorTime.READ));
        System.out.println(GeneratorTime.ALWAYS.matchTime(GeneratorTime.NEW));*/

        //testSpiltFieldPiece();

        /*System.out.println(QueryUtils.trimSqlOrderByField(" , 1 2 nulls last, 2 desc ,"));
        System.out.println(QueryUtils.trimSqlOrderByField("%27+%2B+%27+%2B+%27createDate"));
        System.out.println(QueryUtils.trimSqlOrderByField("      reateDate nulls first, orderind desc ,"));
        System.out.println(QueryUtils.trimSqlOrderByField("  table.column aSc nulls last, orderind desc "));*/
        //testTranslateQuery2();
        //System.out.println(DatabaseAccess.mapColumnNameToField("Hello_World")+"!");
        //System.out.println(QueryUtils.getMatchString("hell%%'%'%wo'rd n__n"));
        //testTranslateQuery2();//testTemplate();
        //CodeRepositoryUtil.loadExtendedSqlMap("D:/Projects/framework2.1/framework-sys-module2.1/src/main/resources/ExtendedSqlMap.xml");
        //System.out.println(CodeRepositoryUtil.getExtendedSql("QUERY_ID_1"));
    }

    public  static void testSpiltFieldPiece() {
        String querySql = " select col_a, column col_b, col1+col2 ," +
                " col1 + col2 col_c , " +
                " col1 + col2 + col3 as col_d , " +
                " col1+(select count(1) from table_c c where c.a=b.col_a), " +
                " (select count(1) from table_c c where c.a=b.col_a) col_e, " +
                " b.fieldf ," +
                " b.field_name as field2," +
                " f.field_name field" +
                "  from table b";

        List<Pair<String,String>> p = QueryUtils.getSqlFieldNamePieceMap(querySql);
        for(Pair<String,String> f : p ){
            System.out.println(f.getKey()+" : "+f.getValue());
        }
        System.out.println("--------------------");
        for(String s : QueryUtils.getSqlFieldPieces(querySql) ){
            System.out.println(s);
        }
        System.out.println("--------------------");
        for(String s : QueryUtils.getSqlFiledNames(querySql) ){
            System.out.println(s);
        }
    }
    public static void testBuildGetCountSQL() {
        System.out.println(QueryUtils.trimSqlOrderByField(""));
        System.out.println(QueryUtils.buildGetCountSQL("From UserInfo"));
        System.out.println(QueryUtils.buildGetCountSQL("with(select * from table group by 1,2 order by ab) a "
                + "select distinct a,b,c,count(*) From (select * from UserInfo group by a, b order by a,b) "
                + " atable group cute by a.a,b.v,b.c order by 1,2"));
    }


    public static void printQueryAndNamedParams(QueryAndNamedParams qp) {
        System.out.println(qp.getQuery());
        for(Map.Entry<String, Object>ent : qp.getParams().entrySet()){
            System.out.print(ent.getKey());
            System.out.print("----");
            System.out.println(String.valueOf(ent.getValue()));
        }
    }

    public static void printDictionaryMap(Map<String, LeftRightPair<String,String>> m) {

        for(Map.Entry<String, LeftRightPair<String,String>>ent : m.entrySet()){
            System.out.print(ent.getKey());
            System.out.print("----");
            System.out.print(String.valueOf(ent.getValue().getLeft()));
            System.out.print("----");
            System.out.println(String.valueOf(ent.getValue().getRight()));
        }
    }

    public static void testGetParams() {

        String queryStatement = "select [(${p1.1}>2 && p2>2)|t1.a,] t2.b,t3.c "+
                "from [(${p1.1}>2  && p2>2)| table1 t1,] table2 t2,table3 t3 "+
                "where 1=1 [(${p1.1}>2  && p2>2)(p1.1:ps)| and t1.a=:ps]" +
            "[(isNotEmpty(${p1.1})&&isNotEmpty(p2)&&isNotEmpty(p3))(p2,p3:px)"
                + "| and (t2.b> :p2 or t3.c >:px)] order by 1,2";
        System.out.println(QueryUtils.transNamedParamSqlToParamSql(queryStatement).getLeft());
    }

    public static void testTranslateQuery() {

        /*String queryStatement = "select t1.a,t2.b,t3.c "+
            "from table1 t1,table2 t2,table3 t3 "+
            "where 1=1 {table1:t1} {不认识} [也不认识] order by 1,2";

        printQueryAndNamedParams(QueryUtils.translateQuery(
                 queryStatement, filters,
                  paramsMap, true));*/
        List<String> filters = new ArrayList<String> ();

        Map<String,Object> paramsMap = new HashMap<String,Object>();
        paramsMap.put("p1.1", "1212年5月6日 下午 5点 25分33秒");
        paramsMap.put("p2", "h w word hello");
        paramsMap.put("p3", "5");
        paramsMap.put("p4", "7");
        filters.add("[table1.c] like { p1.1: ( datetime ) ps}");
        filters.add("[table1.b] = {( like )p2}");
        filters.add("[table1.c] = {:(like )p2}");
        filters.add("[table4.b] = {p4}");
        filters.add("([table2.f]={p2} and [table3.f]={p3})");
        String queryStatement = "select t1.a,t2.b,t3.c "
                    + "from table1 t1,table2 t2,table3 t3 "
                    + "where 1=1 {table1:t1}{table9:t9}"
                    + "{table2:t2,table3:t3}"
                    + "[(count(p1.1,p2)>1)((like )p1.1,p2:(like )pw) " +
                        "| and tw.a=:p3] "
                    //+ " [ p1.1 :()  p4, : ( like )p2  | and tw.a=:p3 ]"
                    + " order by 1,2";

        printQueryAndNamedParams(QueryUtils.translateQuery(
                 queryStatement, filters,
                  paramsMap, true));
        /*
        queryStatement = "select [(${p1.1}>2 && p2>2)|t1.a,] t2.b,t3.c "+
                "from [(${p1.1}>2  && p2>2)| table1 t1,] table2 t2,table3 t3 "+
                "where 1=1 [(${p1.1}>2  && p2>2)(p1.1:ps)| and t1.a=:ps][(isNotEmpty(${p1.1})&&isNotEmpty(p2)&&isNotEmpty(p3))(()p2,p3:px)"
                + "| and (t2.b> :p2 or t3.c >:px)] order by 1,2";

        printQueryAndNamedParams(QueryUtils.translateQuery(
                 queryStatement, filters,
                  paramsMap, true));

        paramsMap.put("p1.1", "5");
        queryStatement = "select [(${p1.1}>2 && p2>2)|t1.a,] t2.b,t3.c "+
                "from [(${p1.1}>2 && p2>2)| table1 t1,] table2 t2,table3 t3 "+
                "where 1=1 [(${p1.1}>2 && p2>2)(p1.1:ps)| and t1.a=:ps][p1.1,:p2,p3:px| and (t2.b> :p2 or t3.c >:px)] order by 1,2";
        printQueryAndNamedParams(QueryUtils.translateQuery(
                 queryStatement, filters,
                  paramsMap, true));*/
    }

    public static void testTemplate() {
        String queryStatement = "select [(${我是中国人@SINA}>2 && p2>2 )|t1.a,] t2.b,t3.c "+
                "from [(${p1.1}>2 && p2>2)| table1 t1,] table2 t2,table3 t3 "+
                "where t2.usercode = :userName  [(${p1.1}>2 && p2>2)(p5,:p9)| and t1.a=:ps][p3:px| and (t2.b> :p2 or t3.c >:px)] order by 1,2";
        System.out.println(StringBaseOpt.objectToString(
                QueryUtils.getSqlTemplateFiledNames(queryStatement)));
    }

    public static void testTranslateQuery2() {

        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("punitCode", "null");
        paramsMap.put("createDate", "2010-12-01");
        paramsMap.put("array", "1,2,3,4,5");
        paramsMap.put("sort", "uu.unitType asc");
        Map<String,Object> userInfo = new HashMap<>();
        userInfo.put("username", "先腾 用户");
        userInfo.put("userCode", "xt code");
        paramsMap.put("userInfo", userInfo);
        String queryStatement =
                "select uu.unitCode,uu.unitName,uu.unitType,uu.isValid,uu.unitTag"
                        +"  from projectTable uu  "
                        +" where 1=1 "
                        //+ "[:(SPLITFORIN,LONG,CREEPFORIN)array| and uu.unitType in (:array)]"
                        //+ "[:(date)createDate | and uu.createDate >= :createDate ]"
                        + "[ * D(U+1, U3)||D(U--) :(like)mathName | and uu.unitName like :mathName ]"
                        +"[:(inplace)sort | order by :sort  ]";

        printQueryAndNamedParams(QueryUtils.translateQuery(
                 queryStatement, null,
                  paramsMap, true));

    }


}
