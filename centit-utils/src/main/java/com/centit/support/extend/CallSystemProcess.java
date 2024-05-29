package com.centit.support.extend;

import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallSystemProcess {
    private String pwd;
    private Map<String, InputStream> extendFiles;
    public CallSystemProcess(){
        pwd =".";
        extendFiles = new HashMap<>();
    }

    public void addExtendFile(String fileName, InputStream inputStream){
        extendFiles.put(fileName, inputStream);
    }

    public void setRunPath(String workingDirectory){ //Working directory
        if(workingDirectory.endsWith("/") || workingDirectory.endsWith("\\")) {
            this.pwd = workingDirectory.substring(0, workingDirectory.length() - 1);
        } else {
            this.pwd = workingDirectory;
        }
    }

    private String mapFilePath(String fileName){
        if(fileName.startsWith("/") || fileName.indexOf(":")>0)
            return fileName;
        return pwd + File.separatorChar +fileName;
    }
    public InputStream getExtendFile(String fileName){
        // load extend file
        try {
            return new FileInputStream(mapFilePath(fileName));
        } catch (FileNotFoundException e) {
            throw new ObjectException(ObjectException.DATA_NOT_FOUND_EXCEPTION,
                "Open file "+fileName+" error: " + e.getMessage(), e);
        }
    }

    public List<String> runCommand(String ... command){
        try {
            for(Map.Entry<String, InputStream> entry : extendFiles.entrySet()){
                FileIOOpt.writeInputStreamToFile(entry.getValue(), mapFilePath(entry.getKey()));
            }
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(pwd));
            processBuilder.command(command);

            Process process = processBuilder.start();

            // 读取脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> output = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            // 等待脚本执行完成
            process.waitFor();
            return output;
        } catch (IOException | InterruptedException e) {
            throw new ObjectException(ObjectException.SYSTEM_CALL_NOT_CORRECT,
                "System call not correct: " + e.getMessage(), e);
        }
    }
}
