package com.centit.support;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Proxy;

@SuppressWarnings("unused")
public abstract class FtpExecutor {

    protected static final Logger logger = LoggerFactory.getLogger(FtpExecutor.class);

    private FtpExecutor() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param url      FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String url, int port, String username,
                                     String password, String path, String filename, InputStream input) {
        return uploadFile(url, port, username, null,
            password, path, filename, input);
    }

    public static boolean uploadFile(String url, int port, String username,
                                     Proxy proxy,
                                     String password, String path, String filename, InputStream input) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            if (proxy != null)
                ftp.setProxy(proxy);
            ftp.connect(url, port);//连接FTP服务器
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(filename, input);
            ftp.logout();
            success = true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);//ioe.printStackTrace();
                }
            }
        }
        return success;
    }

    /**
     * Description: 从FTP服务器下载文件
     *
     * @param url        FTP服务器hostname
     * @param port       FTP服务器端口
     * @param username   FTP登录账号
     * @param password   FTP登录密码
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param localPath  下载后保存到本地的路径
     * @return 是否成功
     */
    public static boolean downloadFile(String url, int port, String username, String password,
                                       String remotePath, String fileName, String localPath) {
        return downloadFile(url, port, username, password, null,
            remotePath, fileName, localPath);
    }

    public static boolean downloadFile(String url, int port, String username, String password,
                                       Proxy proxy,
                                       String remotePath, String fileName, String localPath) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            if (proxy != null)
                ftp.setProxy(proxy);
            ftp.connect(url, port);
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(remotePath);//转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    File localFile = new File(localPath + "/" + ff.getName());
                    try (OutputStream is = new FileOutputStream(localFile)) {
                        ftp.retrieveFile(ff.getName(), is);
                    }
                    //is.close();
                }
            }

            ftp.logout();
            success = true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);//ioe.printStackTrace();
                }
            }
        }
        return success;
    }
}
