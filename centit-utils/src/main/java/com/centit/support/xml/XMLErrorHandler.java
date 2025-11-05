package com.centit.support.xml;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMLErrorHandler implements ErrorHandler {

    private final List<String> errorMessage;
    private final List<String> warningMessage;
    private final List<String> fatalErrorMessage;

    public XMLErrorHandler() {
        this.errorMessage = new ArrayList<>();
        this.warningMessage = new ArrayList<>();
        this.fatalErrorMessage = new ArrayList<>();
    }

    public JSONObject toJSONObject() {
        JSONObject errorJson = new JSONObject();
        int errorCount = errorMessage.size();
        int fatalErrorCount = fatalErrorMessage.size();
        int warningCount = warningMessage.size();
        if(errorCount == 0){
            errorJson.put("code", "0");

            if(warningCount>0){
                errorJson.put("message", "XML文件格式验证通过，但是有" + warningCount + "个警告");
            }else{
                errorJson.put("message", "OK");
            }
        }else{
            errorJson.put("code", 701);
            errorJson.put("message", "XML文件格式验证未通过，有" + errorCount + "个错误，和"+ fatalErrorCount + "个致命错误");
        }

        errorJson.put("data", CollectionsOpt.createHashMap("errorCount", errorCount,
            "warningCount", warningCount,"fatalErrorCount",  fatalErrorCount,
            "errorMessage", errorMessage,"warningMessage", warningMessage,
            "fatalErrorMessage", fatalErrorMessage) );
        return errorJson;
    }

    public static JSONObject createFatalError(String fatalMessage) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("code", "0");
        errorJson.put("message", "XML文件格式验证未通过，有 1 个致命错误");
        errorJson.put("data", CollectionsOpt.createHashMap("errorCount", 0,
            "warningCount", 0,"fatalErrorCount", 1,
            "fatalErrorMessage",  Collections.singletonList(fatalMessage)) );
        return errorJson;
    }
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        warningMessage.add(exception.getMessage() + " at " + exception.getLineNumber() + ":" + exception.getColumnNumber());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        errorMessage.add(exception.getMessage() + " at " + exception.getLineNumber() + ":" + exception.getColumnNumber());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        fatalErrorMessage.add(exception.getMessage() + " at " + exception.getLineNumber() + ":" + exception.getColumnNumber());
    }
}
