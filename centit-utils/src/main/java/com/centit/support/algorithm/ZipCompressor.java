package com.centit.support.algorithm;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipCompressor {
    static final int BUFFER = 8192;

    //private File zipFile;
    private ZipCompressor() {
        //zipFile = new File(pathName);
    }
    /**
     * 将OutputStream 转换为 ZipOutputStream 并作为 compressFile 的输入参数
     * 这个可以用于 打包下载
     * @param os os
     * @return ZipOutputStream
     */
    public static ZipOutputStream convertToZipOutputStream(OutputStream os){
         return new ZipOutputStream( 
        		 new CheckedOutputStream(os,
        				 new CRC32()));
    }
    
    public static void compress(String zipFilePathName,  String fileName, String srcPathName) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
        	File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            /*CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());*/
            ZipOutputStream out = convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
            String basedir = "";
            
            compress(file, fileName, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void compress(String zipFilePathName,  String srcPathName) {
    	File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
        	File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            /*CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());*/
            ZipOutputStream out = convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
            String basedir = "";
            
            compress(file,  out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void compressFileInDirectory(String zipFilePathName, String srcPathName) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
        	File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            /*CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());*/
            ZipOutputStream out = convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
           
            File[] files = file.listFiles();   
            for (int i = 0; i < files.length; i++) {   
                /* 递归 */  
                compress(files[i], out, "");   
            }   
            //compress(file, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void compress(File file,String fileName, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            //System.out.println("压缩：" + basedir + file.getName());
            compressDirectory(file, out, basedir);
        } else {
            //System.out.println("压缩：" + basedir + file.getName());
            compressFile(file, fileName, out, basedir);   
        }
    }

    public static void compress(File file, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            //System.out.println("压缩：" + basedir + file.getName());
            compressDirectory(file, out, basedir);
        } else {
            //System.out.println("压缩：" + basedir + file.getName());
            compressFile(file, file.getName(), out, basedir);   
        }
    }
    
    /* 压缩一个目录 */
    public static void compressDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();   
        for (int i = 0; i < files.length; i++) {   
            /* 递归 */  
            compress(files[i], out, basedir + dir.getName() + "/");   
        }   
    }
  
    /* 压缩一个文件  fileName =  file.getName() */
    
    public static void compressFile(File file, String fileName, ZipOutputStream out, String basedir) {   
        if (!file.exists()) {   
            return;   
        }
        try {   
            BufferedInputStream bis = new BufferedInputStream(   
                    new FileInputStream(file));   
            ZipEntry entry = new ZipEntry(basedir +fileName);   
            out.putNextEntry(entry);   
            int count;
            byte data[] = new byte[BUFFER];   
            while ((count = bis.read(data, 0, BUFFER)) != -1) {   
                out.write(data, 0, count);   
            }   
            bis.close();   
        } catch (Exception e) {
            throw new RuntimeException(e);   
        }   
    }   

    public static void compressFile(File file,  ZipOutputStream out, String basedir) {   
    	compressFile( file, file.getName(), out,  basedir);
    }
	/**
	 * 解压到指定目录
	 * 
	 * @param zipPath zipPath
	 * @param dirPath dirPath
	 * @author isea533
	 */
	public static void release(String zipPath, String dirPath) {
		release(new File(zipPath), dirPath);
	}

	/**
	 * 解压文件到指定目录
	 * 
	 * @param zipFile zipFile
	 * @param dirPath dirPath
	 * @author isea533
	 */
	public static void release(File zipFile, String dirPath) {
		String descDir = dirPath;
		if(!descDir.endsWith("/"))
			descDir = dirPath+"/";
			
		File pathFile = new File(descDir);
		if (!pathFile.exists()) {
			pathFile.mkdirs();
		}
		try{
			ZipFile zip = new ZipFile(zipFile);
			// zip.entries()
			for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String zipEntryName = entry.getName();
				InputStream in = zip.getInputStream(entry);
				String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
				
				// 判断路径是否存在,不存在则创建文件路径
				File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
				if (!file.exists()) {
					file.mkdirs();
				}
				// 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
				if (new File(outPath).isDirectory()) {
					continue;
				}
				// 输出文件路径信息
				//System.out.println(outPath);
	
				OutputStream out = new FileOutputStream(outPath);
				byte[] buf1 = new byte[1024];
				int len;
				while ((len = in.read(buf1)) > 0) {
					out.write(buf1, 0, len);
				}
				in.close();
				out.close();
			}
			zip.close();
		}catch (Exception e) {
            throw new RuntimeException(e);   
        }   
		// System.out.println("******************解压完毕********************");
	}
}
