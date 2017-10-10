package com.centit.support.office;

import com.centit.support.file.FileSystemOpt;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by codefan on 2017/10/9.
 */
@SuppressWarnings("unused")
public abstract class OfficeToHtml {
    private OfficeToHtml() {
        throw new IllegalAccessError("Utility class");
    }
    private static Log logger = LogFactory.getLog(OfficeToHtml.class);

    /**
     * word文档转化成html字符串
     *
     * @param inWordFile word文件路径
     * @param outHtmlFile 输出的Html文件路径
     * @return 是否成功
     * @throws Exception 文件读取异常
     */
    public static boolean word2Pdf(String inWordFile, String outHtmlFile) throws Exception {
        String inputFile = inWordFile.replace('/','\\');
        String htmlFile   = outHtmlFile.replace('/','\\');
        ComThread.InitSTA();
        //long start = System.currentTimeMillis();
        try {
            ActiveXComponent app = new ActiveXComponent("Word.Application");
            // 设置word不可见
            app.setProperty("Visible", new Variant(false));
            // 打开word文件
            Dispatch docs = app.getProperty("Documents").toDispatch();
            Dispatch doc = Dispatch.invoke(docs,"Open",Dispatch.Method,new Object[] {
                    inputFile, new Variant(false),new Variant(true) }, new int[1]).toDispatch();
            FileSystemOpt.deleteFile(htmlFile);
            // 另存为.html
            Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] { htmlFile, new Variant(8) // 7为txt格式，
                    // 8保存为html格式
                    // 17为pdf格式
            }, new int[1]);
            Dispatch.call(doc, "Close", new Variant(false));
            app.invoke("Quit", new Variant[] {});
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e.getMessage(),e);//e.printStackTrace();
            //System.out.println("========Error:文档转换失败：" + e.getMessage());
        } finally {
            ComThread.Release();
        }
        //如果没有这句话,winword.exe进程将不会关闭

        return true;
    }
}
