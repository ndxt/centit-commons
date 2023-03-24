package com.centit.support.report;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.compiler.EmbedFuncUtils;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.jxls.common.Context;
import org.jxls.expression.JexlExpressionEvaluator;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by codefan on 23-3-23.
*/

@SuppressWarnings("unused")
public abstract class ExcelReportUtil {

    private ExcelReportUtil() {
        throw new IllegalAccessError("Utility class");
    }

    protected static final Logger logger = LoggerFactory.getLogger(ExcelReportUtil.class);

    public static void exportExcel(InputStream is, OutputStream os, Map<String, Object> model, Map<String, Object> extendFuns) throws IOException {
        Context context = new Context(model);
        Map<String, Object> extFuns = CollectionsOpt.createHashMap("utils", EmbedFuncUtils.instance);
        if(extendFuns!=null)
            extFuns.putAll(extendFuns);

        JexlBuilder jb = new JexlBuilder();
        jb.namespaces(extFuns);
        JexlEngine je = jb.create();

        JxlsHelper jxlsHelper = JxlsHelper.getInstance();
        Transformer transformer = jxlsHelper.createTransformer(is, os);
        //TransformationConfig config = transformer.getTransformationConfig();
        JexlExpressionEvaluator evaluator = (JexlExpressionEvaluator) transformer.getTransformationConfig()
            .getExpressionEvaluator();
        evaluator.setJexlEngine(je);
        jxlsHelper.processTemplate(context, transformer);
    }

    public static void exportExcel(InputStream is, OutputStream os, Map<String, Object> model) throws IOException {
        exportExcel(is, os, model, null);
    }

}
