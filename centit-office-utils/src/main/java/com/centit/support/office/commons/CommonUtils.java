package com.centit.support.office.commons;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.File;

public abstract class CommonUtils {

    public static String mapWidowsPathIfNecessary(String filePath) {
        return File.separator.equals("\\") ?
            filePath.replace('/', '\\') :
            filePath;
    }

    public static Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "no");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        return serializer;
    }
}

