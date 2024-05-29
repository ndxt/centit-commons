package com.centit.support.test.extend;

import com.centit.support.extend.CallSystemProcess;
import com.centit.support.file.FileIOOpt;

import java.io.IOException;
import java.util.List;

public class TestCallSystemProcess {

    public static void main(String[] args) throws IOException {
        CallSystemProcess cs = new CallSystemProcess();
        cs.setRunPath("/Users/codefan/temp");
        String pythonScript = "print(\"hello world from Python3!\");\n";
        cs.addExtendFile("test.py", FileIOOpt.castObjectToInputStream(pythonScript));
        List<String> lines = cs.callSystemProcess("python3", "test.py");
        System.out.println(lines.toString());
    }
}
