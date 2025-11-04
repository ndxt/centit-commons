package com.centit.support.xml;

import com.alibaba.fastjson2.JSONObject;
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
            if(warningCount==0){
                errorJson.put("status", "success");
            } else {
                errorJson.put("status", "warning");
            }
        }else{
            errorJson.put("status", "error");
        }
        errorJson.put("errorCount", errorCount);
        errorJson.put("warningCount", warningCount);
        errorJson.put("fatalErrorCount", fatalErrorCount);
        errorJson.put("errorMessage", errorMessage);
        errorJson.put("warningMessage", warningMessage);
        errorJson.put("fatalErrorMessage", fatalErrorMessage);
        return errorJson;
    }

    public static JSONObject createFatalError(String fatalMessage) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("status", "error");
        errorJson.put("errorCount", 0);
        errorJson.put("warningCount", 0);
        errorJson.put("fatalErrorCount", 1);
        errorJson.put("fatalErrorMessage", Collections.singletonList(fatalMessage));
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
