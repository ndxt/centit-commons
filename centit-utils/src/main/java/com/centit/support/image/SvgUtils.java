package com.centit.support.image;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SvgUtils {
    private static final Logger logger = LoggerFactory.getLogger(SvgUtils.class);

    public static boolean removeSvgJSAction(String pdfFilePath, String outputPath) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(pdfFilePath));
            List<Element> scriptNodes = document.getRootElement().elements("script");
            if(scriptNodes.isEmpty()) {
                return false;
            }
            for (Element node : scriptNodes) {
                node.detach();
            }
            Element root = document.getRootElement();
            root.attributes().removeIf(attr -> attr.getName().startsWith("on"));
            XMLWriter writer = new XMLWriter(new FileWriter(outputPath));

            writer.write(document);
            writer.close();
            return true;
        } catch (IOException | DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}
