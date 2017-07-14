package com.centit.support.file;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.alibaba.fastjson.JSON;

public class FileIOOpt {

	public static int writeInputStreamToOutputStream(InputStream in,
			OutputStream out) throws IOException{
		int read = 0;
		int length=0;
		final byte[] bytes = new byte[1024 * 10];
		while ((read = in.read(bytes)) != -1){
			out.write(bytes, 0, read);
			length +=read;
		}
		return length;
	}
	
	public static int writeInputStreamToFile(InputStream in,
			File file) throws IOException{
		
		try(FileOutputStream out = new FileOutputStream(file, true)){
			return writeInputStreamToOutputStream(in,out);
		}
	}
	
	public static int writeInputStreamToFile(InputStream in,
			String filePath) throws IOException{
		return writeInputStreamToFile(in, new File(filePath));
	}
	
	public static void writeStringToOutputStream(String strData,OutputStream io) throws IOException{
		try(Writer writer = new OutputStreamWriter(io)){
			writer.write(strData);
		}
	}
	
	public static void writeStringToFile(String strData,File file) throws IOException{
		try(Writer writer = new FileWriter(file)){
			writer.write(strData);
		}
	}
	
	public static void writeStringToFile(String strData,String fileName) throws IOException{
		writeStringToFile(strData,new File(fileName));
	}
	
	public static String readStringFromRead(Reader reader) throws IOException{
        try(StringWriter writer = new StringWriter()){  
	        char[] buf = new char[1024];
	        int len = 0;
	        while ((len = reader.read(buf)) != -1) {
	            writer.write(buf, 0, len);
	        }    
	        return writer.toString();
        }
	}
	
	public static String readStringFromInputStream(InputStream is,String charsetName) throws IOException{
		return readStringFromRead(new InputStreamReader(is,charsetName));        
	}
	
	public static String readStringFromInputStream(InputStream is) throws IOException{
		return readStringFromRead(new InputStreamReader(is));        
	}
	
	public static String readStringFromFile(File file,String charsetName) throws IOException{
		return readStringFromRead(new InputStreamReader(new FileInputStream(file),charsetName));
	}
	
	public static String readStringFromFile(File file) throws IOException{
		return readStringFromRead(new InputStreamReader(new FileInputStream(file)));
	}
	
	public static String readStringFromFile(String fileName,String charsetName) throws IOException{
		return readStringFromFile(new File(fileName),charsetName);
	}
	
	public static String readStringFromFile(String fileName) throws IOException{
		return readStringFromFile(new File(fileName));
	}
	
	public static void writeObjectToFile(Object obj,String fileName) throws IOException{
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))){
			oos.writeObject(obj);
		}
	}
	
	public static Object readObjectFromFile(String fileName)
			throws IOException, ClassNotFoundException{
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))){
			Object obj = ois.readObject();
			return obj;
		}
	}
	
	public static void writeObjectAsJsonToFile(Object obj,String fileName) throws IOException{
		String sjson = JSON.toJSONString(obj);
		writeStringToFile(sjson,fileName);
	}
	
	public static <T> T readObjectAsJsonFromFile(String fileName, Class<T> clazz)
			throws IOException, ClassNotFoundException{
		String sjson = readStringFromFile(fileName);
		return JSON.parseObject(sjson,clazz);
	}

	/**
	 * close the IO stream.
	 * @param closeable closeable
	 */
	public static void close(Closeable closeable) {
		try {
			if (closeable != null)
				closeable.close();
		} catch (IOException e) {
		}
	}
}
