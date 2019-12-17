package com.centit.support.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Fancy
 * modifyed by brmrk
 * modifyed by codefan
 * 这个类用来读取 ini配置文件，没有写的功能
 */
@SuppressWarnings("unused")
public class IniReader {

    protected Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();
    private transient String currentSecion = null;
    private transient Map<String, String> current = null;
    private IniReader() {
        throw new IllegalAccessError("Utility class");
    }

    public IniReader(String filename) throws IOException {
        //modifyed by brmrk
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(filename), "GBK"))) {
            read(reader);
        }
    }

    protected void read(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            parseLine(line);
        }

    }

    protected void parseLine(String line) {
        line = line.trim();
        if (line.startsWith("[") && line.endsWith("]")) {
            if (current != null && currentSecion != null) {
                sections.put(currentSecion, current);
            }
            currentSecion = line.substring(1, line.length() - 1);
            current = sections.get(currentSecion);
            if (current == null)
                current = new HashMap<String, String>();
        } else if (current != null && currentSecion != null &&
            !line.startsWith(";") && (line.indexOf('=') >= 0)) {
            int i = line.indexOf('=');
            String name = line.substring(0, i).trim();
            String value = line.substring(i + 1).trim();
            current.put(name, value);
            sections.put(currentSecion, current);
        }
    }

    public String getValue(String section, String name) {
        Map<String, String> p = sections.get(section);

        if (p == null) {
            return null;
        }

        String value = p.get(name);
        return value;
    }
    /*
    public  static void  main(String[] args)  throws  IOException  {
       IniReader ini=new IniReader("config.ini");
       String value = ini.getRight("product", "name");
       System.out.println(value);
   }*/
}



