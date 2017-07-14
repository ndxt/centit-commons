package com.centit.support.database;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.centit.support.xml.IgnoreDTDEntityResolver;

/**
 * 数据源描述信息，这些信息和参数是创建连接池的参数
 * @author codefan
 *
 */
public final class DataSourceDescription implements  Serializable{
	protected static final Logger logger = LoggerFactory.getLogger(DataSourceDescription.class);
    private static final long serialVersionUID = 1L;
	private String connUrl ;
	private String username ;
	private String driver ;
	private String password ;
	private DBType dbType;
	private int    maxTotal ;
	private int    maxIdle ;
	private int    minIdle ;
	private int    maxWaitMillis;
	private int    initialSize ;
	private String databaseCode;
	
	public String getDatabaseCode() {
		return databaseCode;
	}

	public void setDatabaseCode(String databaseCode) {
		this.databaseCode = databaseCode;
	}
	
	public DataSourceDescription(){
		this.maxTotal  = 10;
		this.maxIdle  = 5;
		this.setMinIdle(1);
		this.initialSize = 3;
		this.maxWaitMillis = 10000;
	}
	
	public DataSourceDescription(String connectURI, String username){
		this();
		this.setConnUrl(connectURI);
		this.username = username;
	}
	
	public DataSourceDescription(String connectURI, String username, String pswd){
		this();
		this.setConnUrl(connectURI);
		this.username = username;
		this.password = pswd;
	}	

	public String getConnUrl() {
		return connUrl;
	}

	public void setConnUrl(String connUrl) {
		this.connUrl = connUrl;
		this.dbType = DBType.mapDBType(connUrl);
		this.driver = DBType.getDbDriver(this.dbType);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMaxWaitMillis() {
		return maxWaitMillis;
	}

	public void setMaxWaitMillis(int maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public String getDriver() {
		return driver;
	}

	public DBType getDbType() {
		return dbType;
	}

	@Override
	public boolean equals(Object dbco){
		if(this==dbco)
			return true;
		
		if( dbco instanceof DataSourceDescription ){
			DataSourceDescription dbc =(DataSourceDescription) dbco;
			return connUrl !=null && connUrl.equals(dbc.getConnUrl())
				&& username != null && username.equals(dbc.getUsername());
		}else
			return false;
	}
	
	
	@Override
	public int hashCode(){
		int result = 17;
		result = 37 * result +
		 	(this.connUrl == null ? 0 :this.connUrl.hashCode());
  
		result = 37 * result +
		 	(this.username == null ? 0 :this.username.hashCode());
	
		return result;
	}
	
	public void loadHibernateConfig(String sConfFile,String sDbBeanName){
		SAXReader  builder = new SAXReader(false);
		builder.setValidation(false);
		builder.setEntityResolver(new IgnoreDTDEntityResolver());
		
		Document doc = null;
		Element bean = null;

		try {			
			if(sConfFile.indexOf(':')>=0){
				if(sConfFile.startsWith("classpath:")){
					doc= builder.read(this.getClass().getResourceAsStream(sConfFile.substring(10)));
				}else
					doc= builder.read(new File(sConfFile));
			}else
				doc= builder.read(this.getClass().getResourceAsStream(sConfFile));
			Element root  = doc.getRootElement();//获取根元素 
			bean = (Element) root.selectSingleNode("bean[@id=\""+sDbBeanName+"\"]");
			if(bean != null){ // 
				Element property;			

				property = (Element)bean.selectSingleNode("property[@name=\"url\"]");
				if(property!=null)
					connUrl = property.attributeValue("value");
				property = (Element)bean.selectSingleNode("property[@name=\"driverClassName\"]");
				if(property!=null)
					driver = property.attributeValue("value");
				property = (Element)bean.selectSingleNode("property[@name=\"username\"]");
				if(property!=null)
					username = property.attributeValue("value");
				property = (Element)bean.selectSingleNode("property[@name=\"password\"]");
				if(property!=null)
					password = property.attributeValue("value");
			}
		} catch (DocumentException e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		}	
		
		dbType = DBType.mapDBType(connUrl);
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
	
    public static boolean testConntect(DataSourceDescription dsDesc){
		boolean connOk = false;
		try {
			Class.forName(dsDesc.getDriver());//.newInstance();
			Connection conn = DriverManager.getConnection(dsDesc.getConnUrl(),
					dsDesc.getUsername(),dsDesc.getPassword());
			connOk = true;
			conn.close();
		} catch (ReflectiveOperationException |SQLException e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		}
		return connOk;
	}
	
    public boolean testConntect(){
		return DataSourceDescription.testConntect(this);
	}
}
