package com.centit.support.database.metadata;

import com.centit.support.database.utils.FieldType;
import com.centit.support.file.FileSystemOpt;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
@SuppressWarnings("unused")
public class SimpleTableInfo implements TableInfo{

    protected static final Logger logger = LoggerFactory.getLogger(SimpleTableInfo.class);
    /**
     * 包括主键
     */
    private List<SimpleTableField> columns=null;

    private String schema;
    private String orderBy;
    private String tableType;
    private String tableName;// 其实是table 代码 code
    //private String sClassName;//表对应的类名 同时作为业务模块名
    private String tableLabelName;// 表的 描述，中文名称
    private String tableComment;// 表的备注信息
    private String pkName;
    private List<SimpleTableReference> references=null;
    private String packageName;

    /**
     * @return 数据库表名，对应pdm中的code，对应元数据中的 tabcode
     */
    public String getTableName() {
        return tableName;

    }

    public void setTableName(String tabName) {
        tableName = tabName;

    }

    /**
     * @return 数据库表中文名，对应pdm中的name,对应元数据中的 tabname
     */
    public String getTableLabelName() {
        return tableLabelName;
    }

    public void setTableLabelName(String tabDesc) {
        this.tableLabelName = tabDesc;
    }

    /**
     * @return 数据库表备注信息，对应pdm中的Comment,对应元数据中的 tabdesc
     */
    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tabComment) {
        this.tableComment = tabComment;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    protected static void writerXMLFile(Document doc, String xmlFile){
        XMLWriter output;
        try {
            output = new XMLWriter(
                      new FileWriter( new File(xmlFile) ));
            output.write( doc );
            output.close();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
    }

    /**
     * 根据字段名查找 字段信息
     * @param colname 字段名
     * @return 字段信息
     */
    public SimpleTableField findField(String colname){
        for(SimpleTableField col : columns){
            if(col.getColumnName().equals(colname))
                return col;
        }
        return null;
    }

    /**
     * 根据属性名查找 字段信息
     * @param name 字段属性名
     * @return 字段信息
     */
    @Override
    public SimpleTableField findFieldByName(String name){
        for(SimpleTableField col : columns){
            if(col.getPropertyName().equals(name))
                return col;
        }
        for(SimpleTableField col : columns){
            if(col.getColumnName().equals(name))
                return col;
        }
        return null;
    }

    /**
     * 根据属性名查找 字段信息
     * @param name 属性名
     * @return 字段信息
     */
    @Override
    public SimpleTableField findFieldByColumn(String name){
        for(SimpleTableField col : columns){
            if(col.getColumnName().equals(name))
                return col;
        }
        for(SimpleTableField col : columns){
            if(col.getPropertyName().equals(name))
                return col;
        }
        return null;
    }


    public SimpleTableInfo()
    {

    }

    public SimpleTableInfo(String tabname)
    {
        setTableName(tabname);
    }

    private void saveProperty(SimpleTableField field,Element propElt,boolean keyProp){
        propElt.addAttribute("name", field.getPropertyName());
        propElt.addAttribute("type", field.getJavaType().getName());
        Element colElt = propElt.addElement("column");
        saveColumn(field,colElt,keyProp);
    }

    private void saveColumn(SimpleTableField field,Element colElt,boolean keyProp){
        colElt.addAttribute("name", field.getColumnName().toUpperCase());
        if(FieldType.LONG.equals(field.getFieldType()) || FieldType.DOUBLE.equals(field.getFieldType())
            || FieldType.INTEGER.equals(field.getFieldType()) || FieldType.FLOAT.equals(field.getFieldType())) {
            colElt.addAttribute("precision", String.valueOf(field.getPrecision()));
            colElt.addAttribute("scale", String.valueOf(field.getScale()));
        }else if(field.getMaxLength()>0)
            colElt.addAttribute("length", String.valueOf(field.getMaxLength()));

        if(!keyProp && field.isMandatory())
            colElt.addAttribute("not-null", "true");
    }

    private void setAppPropertiesValue(Properties prop,String key,String value )
    {
        String sKey = /*sModuleName +'.'+ */ FieldType.mapPropName(tableName) +'.'+key;
        if(! prop.containsKey(sKey))
            prop.setProperty(sKey,  value);
    }

    public void addResource(String filename)
    {

        try {
            Properties prop = new Properties();
            if( FileSystemOpt.existFile(filename+"_zh_CN.properties")){
                try(FileInputStream fis  = new FileInputStream(filename+"_zh_CN.properties")){
                        prop.load(fis);
                }
            }

            setAppPropertiesValue(prop,"list.title",tableLabelName+"列表");
            setAppPropertiesValue(prop,"edit.title","编辑"+tableLabelName);
            setAppPropertiesValue(prop,"view.title","查看"+tableLabelName);
            for(SimpleTableField col : columns )
                setAppPropertiesValue(prop,FieldType.mapPropName(col.getColumnName()),col.getFieldLabelName());

            try(FileOutputStream outputFile = new FileOutputStream(filename+"_zh_CN.properties")){
                prop.store(outputFile, "create by centit B/S framework!");
                //outputFile.close();
            }
            //prop.list(System.out);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }

        try {
            Properties prop = new Properties();
            if( FileSystemOpt.existFile(filename+".properties")){
                try(FileInputStream fis  = new FileInputStream(filename+".properties")){
                    prop.load(fis);
                }
            }

            setAppPropertiesValue(prop,"list.title",FieldType.mapPropName(tableName)+" list");
            setAppPropertiesValue(prop,"edit.title","new or edit "+FieldType.mapPropName(tableName)+" piece");
            setAppPropertiesValue(prop,"view.title","view "+FieldType.mapPropName(tableName)+" piece");
            for(SimpleTableField col : columns )
                setAppPropertiesValue(prop,FieldType.mapPropName(col.getColumnName()),col.getPropertyName());

            try(FileOutputStream outputFile = new FileOutputStream(filename+".properties")){
                prop.store(outputFile, "create by centit B/S framework!");
                //outputFile.close();
            }
            //prop.list(System.out);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
    }

    public void saveHibernateMappingFile(String filename ){
        Document doc = null;

        if(FileSystemOpt.existFile(filename )){
            System.out.println("文件："+filename+" 已存在！");
            return;
        }

        doc = DocumentHelper.createDocument();
        //doc.addProcessingInstruction("xml", "version=\"1.0\" encoding=\"utf\"");
        doc.addDocType("hibernate-mapping", "-//Hibernate/Hibernate Mapping DTD 3.0//EN",
                "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd");
        doc.addComment("Mapping file autogenerated by codefan@centit.com");
        Element root  = doc.addElement("hibernate-mapping");//首先建立根元素
        //create class
        Element classElt = root.addElement("class");
        classElt.addAttribute("name", packageName+'.'+getClassName());
        classElt.addAttribute("table", tableName.toUpperCase());
        classElt.addAttribute("schema", schema);
        //save primary key
        List<String> pkColumns = getPkColumns();
        if(pkColumns.size()>1){
            Element idElt = classElt.addElement("composite-id");
            idElt.addAttribute("name", "cid");
            idElt.addAttribute("class", packageName+'.'+getClassName()+"Id");
            for(String pkcol : pkColumns){
                SimpleTableField field = findField(pkcol);
                if(field!=null){
                    Element keyElt = idElt.addElement("key-property");
                    saveProperty(field,keyElt,true);
                    //colElt.addAttribute("not-null", "true");
                }
            }
        }else if(pkColumns.size()==1){
            Element idElt = classElt.addElement("id");
            SimpleTableField field = findField(pkColumns.get(0));
            saveProperty(field,idElt,true);
            Element genElt = idElt.addElement("generator");
            genElt.addAttribute("class", "assigned");
        }
        //save property
        if(columns !=null){
            for(Iterator<SimpleTableField> it = columns.iterator();it.hasNext();){
                SimpleTableField col = it.next();
                if(isParmaryKey(col.getColumnName()))
                    continue;
                Element propElt = classElt.addElement("property");
                saveProperty(col,propElt,false);
            }
        }
        if(references !=null){
            for(Iterator<SimpleTableReference> it = references.iterator();it.hasNext();){
                SimpleTableReference ref = it.next();
                Element setElt = classElt.addElement("set");
                setElt.addAttribute("name", FieldType.mapPropName(ref.getTableName())+'s');
                setElt.addAttribute("cascade", "all-delete-orphan");//"all-delete-orphan")//save-update,delete;
                setElt.addAttribute("inverse", "true");
                Element keyElt = setElt.addElement("key");
                for(Iterator<SimpleTableField> it2 = ref.getFkColumns().iterator();it2.hasNext();){
                    SimpleTableField col = it2.next();
                    Element colElt = keyElt.addElement("column");
                    saveColumn(col,colElt,false);
                }
                Element maptypeElt = setElt.addElement("one-to-many");
                maptypeElt.addAttribute("class", packageName+'.'+ ref.getClassName());
            }
        }
        writerXMLFile(doc,filename);
    }

    public List<SimpleTableField> getColumns() {
        if(columns==null)
            columns = new ArrayList<>(20);
        return columns;
    }

    public void addColumn(SimpleTableField column) {
        getColumns().add(column);
    }

    public void setColumns(List<SimpleTableField> columns) {
        this.columns = columns;
    }

    public String getClassName() {
        String sClassName = FieldType.mapPropName(tableName);
        return sClassName.substring(0,1).toUpperCase() +
                sClassName.substring(1);
    }

    public List<SimpleTableReference> getReferences() {
        if(references==null)
            references = new ArrayList<>(4);
        return references;
    }

    public boolean hasReferences(){
        return references!=null && references.size()>0;
    }

    public void addReference(SimpleTableReference reference) {
        getReferences().add(reference);
    }

    public SimpleTableReference findReference(String reference){
        if(references==null)
            return null;

        for(SimpleTableReference ref : references){
            if(ref.getReferenceName().equals(reference))
                return ref;
        }
        return null;
    }

    public void setReferences(List<SimpleTableReference> references) {
        this.references = references;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    /**
     * 将一个字段设为主键
     * @param colname 字段
     */
    public void setColumnAsPrimaryKey(String colname){
        SimpleTableField field = findFieldByName(colname);
        if(field != null){
            field.setPrimaryKey(true);
        }
    }
}
