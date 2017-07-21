package com.centit.support.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties 文件工具类
 * 
 * @author sx
 * 
 */
@SuppressWarnings("unused")
public abstract class PropertiesReader {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    /**
     * 读取classpath下文件
     * 
     * @param fileName
     *            文件名前需要加 "/"，如 "/system.properties"
     * @param key 建
     * @return 值
     */
    public static String getClassPathProperties(String fileName, String key) {
        try(InputStream in = PropertiesReader.class.getResourceAsStream(fileName)){   
            return getPropertyValue(in, key);
        } catch (IOException e) {
        	logger.error("读取系统配置文件出错", e);
        }

        return "";
    }
	/**
     * 读取classpath下文件
     * @param clazz 任意类型
     * @param fileName
     *            文件名前需要加 "/"，如 "/system.properties"
     * @param key 建
     * @return 值
     */
    public static String getClassPathProperty(Class<?> clazz, String fileName, String key) {
        try(InputStream in = clazz.getResourceAsStream(fileName)){  
            return getPropertyValue(in, key);
        } catch (IOException e) {
        	logger.error("读取系统配置文件出错", e);
        }

        return "";
    }
    
    /**
     * 读取非classpath下文件
     * 
     * @param fileName
     *            文件全路径及文件名，文件名前需要加 "/"，如 "/system.properties"
     * @param key 建
     * @return 值
     */
    public static String getFilePathProperties(String fileName, String key) {
        try(FileInputStream fis = new FileInputStream(new File(fileName))) {
            return getPropertyValue(fis, key);
        } catch (IOException e) {
        	logger.error("读取系统配置文件出错", e);
        }

        return "";
    }

    /**
     * 读取classpath下文件
     * 
     * @param fileName
     *            文件名，文件名前需要加 "/"，如 "/system.properties"
     * @return 键值对
     */
    public static Properties getClassPathProperties(String fileName) {
        try(InputStream in = PropertiesReader.class.getResourceAsStream(fileName)){
        	return loadProperties(in);
        } catch (IOException e) {
        	logger.error("读取系统配置文件出错", e);
        }
        return null;
    }
    
    public static Properties getClassPathProperties( Class<?> clazz, String fileName) {
        try(InputStream in = clazz.getResourceAsStream(fileName)){         	
            return loadProperties(in);
        } catch (IOException e) {
        	logger.error("读取系统配置文件出错", e);
        }
        return null;
    }
   
    
    /**
     * 读取非classpath下文件
     * 
     * @param fileName
     *            文件全路径及文件名
     * @return 键值对
     */
    public static Properties getFilePathProperties(String fileName) {
        try(FileInputStream fis =new FileInputStream(new File(fileName))){
            return loadProperties(fis);
        } catch (IOException e) {
        	logger.error("读取系统配置文件出错", e);
        }

        return null;
    }

    private static String getPropertyValue(InputStream resource, String key) throws IOException {
        Properties prop = new Properties();
        prop.load(resource);
        return prop.getProperty(key);
    }

    private static Properties loadProperties(InputStream resource) throws IOException {
        Properties prop = new Properties();
        prop.load(resource);
        return prop;
    }

}
