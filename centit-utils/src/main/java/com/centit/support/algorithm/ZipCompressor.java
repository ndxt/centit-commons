package com.centit.support.algorithm;

import com.centit.support.common.ObjectException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;

@SuppressWarnings("unused")
public abstract class ZipCompressor {
    static final int BUFFER = 102400;

    //private File zipFile;
    private ZipCompressor() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 将OutputStream 转换为 ZipArchiveOutputStream 并作为 compressFile 的输入参数
     * 这个可以用于 打包下载
     *
     * @param os os
     * @return ZipArchiveOutputStream
     */
    public static ZipArchiveOutputStream convertToZipOutputStream(OutputStream os) {
        ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(os);
        zipOut.setEncoding(StandardCharsets.UTF_8.name());
        return zipOut;
    }

    public static void compress(String zipFilePathName, String fileName, String srcPathName) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
            File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            /*CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());*/
            ZipArchiveOutputStream out = convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
            String basedir = "";

            compress(file, fileName, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void compress(String zipFilePathName, String srcPathName) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
            File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            /*CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());*/
            ZipArchiveOutputStream out = convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
            String basedir = "";

            compress(file, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将多个文件 压缩到一个 zip 文件中
     *
     * @param zipFilePathName 输出zip文件的路径名
     * @param srcPathNames    输入的文件路径列表
     */
    public static void compressFiles(String zipFilePathName, String[] srcPathNames) {
        try(FileOutputStream fileOutputStream = new FileOutputStream(zipFilePathName);
            ZipArchiveOutputStream out = convertToZipOutputStream(fileOutputStream)){
            String basedir = "";
            for (String srcPathName : srcPathNames) {
                File file = new File(srcPathName);
                if (file.exists()) {
                    compress(file, out, basedir);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将多个文件 压缩到一个 zip 文件中
     *
     * @param zipFilePathName 输出zip文件的路径名
     * @param srcPathNames    输入的文件路径列表
     */
    public static void compressFiles(String zipFilePathName, Collection<String> srcPathNames) {
        compressFiles(zipFilePathName, srcPathNames.toArray(new String[0]));
    }

    public static void compressFileInDirectory(String zipFilePathName, String srcPathName) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFilePathName);
            ZipArchiveOutputStream out = convertToZipOutputStream(fileOutputStream)){
            File[] files = file.listFiles();
            if(files != null) {
                for (File f : files) {
                    /* 递归 */
                    compress(f, out, "");
                }
            }
        } catch (Exception e) {
            throw new ObjectException(e.getMessage(), e);
        }
    }

    public static void compress(File file, String fileName, ZipArchiveOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            //System.out.println("压缩：" + basedir + file.getName());
            compressDirectory(file, out, basedir);
        } else {
            //System.out.println("压缩：" + basedir + file.getName());
            compressFile(file, fileName, out, basedir);
        }
    }

    public static void compress(File file, ZipArchiveOutputStream out, String basedir) {
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
    public static void compressDirectory(File dir, ZipArchiveOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        if(files == null)
            return;
        for (File file : files) {
            /* 递归 */
            compress(file, out, basedir + dir.getName() + "/");
        }
    }

    public static void compressFile(InputStream fis, String fileName, ZipArchiveOutputStream out, String basedir) {
        String filePath;
        if(StringUtils.isBlank(basedir)){
            filePath = fileName;
        }else {
            if(basedir.endsWith("/") || basedir.endsWith("\\")){
                filePath = basedir + fileName;
            } else {
                filePath = basedir + File.separatorChar + fileName;
            }
        }
        try (BufferedInputStream bis = new BufferedInputStream(fis)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(filePath);
            out.putArchiveEntry(entry);
            bis.transferTo(out);
            out.closeArchiveEntry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* 压缩一个文件  fileName =  file.getName() */

    public static void compressFile(File file, String fileName, ZipArchiveOutputStream out, String basedir) {
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            compressFile(fis, fileName, out, basedir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void compressFile(File file, ZipArchiveOutputStream out, String basedir) {
        compressFile(file, file.getName(), out, basedir);
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
        if (!descDir.endsWith("/"))
            descDir = dirPath + "/";

        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        try (ZipFile zip = ZipFile.builder().setFile(zipFile).setCharset(StandardCharsets.UTF_8).get()) {
            for (Enumeration<ZipArchiveEntry> entries = zip.getEntries(); entries.hasMoreElements();) {
                ZipArchiveEntry entry = entries.nextElement();
                String zipEntryName = entry.getName();

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
                try (InputStream in = zip.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outPath)) {
                    in.transferTo(out);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // System.out.println("******************解压完毕********************");
    }
}
