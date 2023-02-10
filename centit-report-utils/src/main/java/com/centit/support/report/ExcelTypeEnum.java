package com.centit.support.report;

import com.centit.support.file.FileType;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by codefan on 17-9-29.
 */
public enum ExcelTypeEnum {
    HSSF, // excel 2003
    XSSF, // excel 2003+ oomxl
    NOTEXCEL;
    /*HSSF(0),XSSF(1);

    int value;
    ExcelTypeEnum(int value) {
        this.value = value;
    }*/
    protected static final Logger logger = LoggerFactory.getLogger(ExcelTypeEnum.class);

    public static ExcelTypeEnum checkFileExcelType(byte[] headerBytes) throws IOException {
        String fileHead =  String.valueOf(Hex.encodeHex(headerBytes,false));
        if(fileHead.startsWith("D0CF11E0"))//mFileTypes.get("office2003")))
            return HSSF;
        if(fileHead.startsWith("504B0304"))//mFileTypes.get("officeX")))
            return XSSF;

        return NOTEXCEL;
    }

    public static ExcelTypeEnum checkFileExcelType(InputStream inputStream) throws IOException {
        byte[] b = new byte[28];
        inputStream.read(b, 0, 28);
        return checkFileExcelType(b);
    }

    public static ExcelTypeEnum checkFileExcelType(String filePath){
        String suffix = FileType.getFileExtName(filePath);
        if("xls".equalsIgnoreCase(suffix)){
            return HSSF;
        }

        if("xlsx".equalsIgnoreCase(suffix)){
            return XSSF;
        }

        try(InputStream inputStream = new FileInputStream(new File(filePath))) {
           return checkFileExcelType(inputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }

        return NOTEXCEL;
    }
}
