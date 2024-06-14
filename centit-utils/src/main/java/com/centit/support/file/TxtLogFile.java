package com.centit.support.file;

import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 这个类是用来对纯文本的文件追加文本内容
 * 可以用来写操作日志
 * <p>
 * 他有两组函数，一组静态函数打开、写入、关闭 一次完成
 * 一组是类成员函数，可以打开，重复写入，关闭，
 * 两组成员函数用于不同的场合
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public class TxtLogFile {
    protected static final Logger logger = LoggerFactory.getLogger(TxtLogFile.class);
    private BufferedWriter logWriter;
    private FileWriter fileWriter;

    private static void innerWriteLog(Writer bw, String slog, boolean bNewLine, boolean bShowTime) throws IOException {
        if (bShowTime) {
            bw.write(DatetimeOpt.convertDateToString(DatetimeOpt.currentUtilDate(),
                DatetimeOpt.getDateTimePattern()));
            bw.write(" -- ");
        }
        bw.write(slog);
        if (bNewLine) {
            bw.write("\r\n");
        }
    }

    public static void writeLog(String sLogFileName, String slog, boolean bNewLine, boolean bShowTime) {
        try (BufferedWriter bw = new BufferedWriter(
            new FileWriter(sLogFileName, true))) {
            innerWriteLog(bw, slog, bNewLine, bShowTime);
            bw.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
    }

    public static void writeLogEx(String sLogFileName, String slog, boolean bNewLine, boolean bShowTime) {
        try (BufferedWriter bw = new BufferedWriter(
            new FileWriter(sLogFileName, true))) {
            innerWriteLog(bw, slog, bNewLine, bShowTime);
            System.out.println(DatetimeOpt.currentDatetime() + " - " + slog);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
    }

    public static void writeLog(String sLogFileName, String slog) {
        try (BufferedWriter bw = new BufferedWriter(
            new FileWriter(sLogFileName, true))) {
            bw.write(slog);
            //bw.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
    }

    public boolean openLogFile(String sLogFileName) {
        closeLogFile();
        boolean bOpened = false;
        try {
            fileWriter = new FileWriter(sLogFileName, true);
            logWriter = new BufferedWriter(fileWriter);
            bOpened = true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return bOpened;
    }

    public boolean closeLogFile() {
        boolean bClosed = false;
        try {
            if (logWriter != null) {
                logWriter.close();
                fileWriter.close();
            }
            logWriter = null;
            bClosed = true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return bClosed;
    }

    public void writeLog(String slog, boolean bNewLine, boolean bShowTime) {
        try {
            innerWriteLog(logWriter, slog, bNewLine, bShowTime);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
    }

    public void writeLogEx(String slog, boolean bNewLine, boolean bShowTime) {
        try {
            innerWriteLog(logWriter, slog, bNewLine, bShowTime);
            System.out.println(DatetimeOpt.currentDatetime() + " - " + slog);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
    }
}
