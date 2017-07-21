package com.centit.support.office;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.centit.support.file.FileSystemOpt;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;
import jp.ne.so_net.ga2.no_ji.jcom.ReleaseManager;

public abstract class OfficeToPdf {
	private static Log logger = LogFactory.getLog(OfficeToPdf.class);
	//private static final int ppSaveAsPDF = 32;
	/**
	 * yang_h 2016-5-24
	 * 修改 excel多sheet 转换PDF 问题，一张表格 超大不分页，一个sheet = 一页pdf
	 * @param inputFile 输入excel文件
	 * @param pdfFile 临时pdf
	 * @return 是否成功
	 */
	public static boolean excel2PDF(String inputFile, String pdfFile) {
		ComThread.InitSTA();
		ActiveXComponent actcom = new ActiveXComponent("Excel.Application");
		try {
			actcom.setProperty("Visible", new Variant(false));
			Dispatch workbooks = actcom.getProperty("Workbooks").toDispatch();
			Dispatch excel = Dispatch.invoke(workbooks,"Open",Dispatch.Method,  
					new Object[]{inputFile,new Variant(false),new Variant(false)},  new int[9] ).toDispatch();
			Dispatch sheets= Dispatch.get(excel, "Sheets").toDispatch();
			int count = Dispatch.get(sheets, "Count").getInt();
			String outFile = pdfFile.substring(0,pdfFile.lastIndexOf("."));
			//將每一個sheet 分開轉換成 單獨的pdf
	        for (int i = 1; i <=count ; i++) {
	        	//獲得當前sheet
	           Dispatch sheet = Dispatch.invoke(sheets, "Item",
	                       Dispatch.Get, new Object[] { i }, new int[1]).toDispatch();
	           //設置當前sheet 內容在一頁展示
	           Dispatch page = Dispatch.call(sheet, "PageSetup").toDispatch();
	           Dispatch.put(page, "PrintArea", false);//false或"" 表示打印sheet页中的 整个区域， 可以使用 excel表达式指定 要打印的单元格范围 ，比如 "$A$1:$C$5" 表示打印 A1-C5的单元格区域
	           Dispatch.put(page, "Orientation", 2);// 打印方向 1横向  2纵向
	           /**
	            * 将所有内容 无论行，列 有多少 都在一页显示， 
	            * Zoom 必须为false ,FitToPagesTall、FitToPagesWide才有效！！
	            */
	           Dispatch.put(page, "Zoom", false);      //值为100=false， 缩放 10-400  %
	           Dispatch.put(page, "FitToPagesTall", 1);  //所有行为一页--   页高
	           Dispatch.put(page, "FitToPagesWide", 1);  //所有列为一页(1或false) --页宽
        		
	           //將當前sheet轉換成 一個pdf
        	   //String sheetname = Dispatch.get(sheet, "name").toString();
        		
        		Dispatch.call(sheet, "Activate");	
        		Dispatch.call(sheet, "Select");
        		Dispatch.invoke(excel,"SaveAs",Dispatch.Method,
        			 new Object[]{outFile+"-"+ i+".pdf",new Variant(57), new Variant(false),
           		     new Variant(57), new Variant(57),new Variant(false), 
           		     new Variant(true),new Variant(57), new Variant(false),
           		     new Variant(true), new Variant(false) },new int[1]);
        		//System.out.println("Excel sheet to pdf Success :"+outFile+"-"+ i+".pdf");
	        }
	        //將多個 pdf 合併到一個pdf,可能會有 頁面大小不一問題，需要合併之前 求出最大頁面pageSizes
	        if(count>0){
	        	try {
	        		File finalPdf = new File(pdfFile);//合并pdf 临时文件
	        		if(finalPdf.exists())
	        		finalPdf.delete();
	    			Document document = new Document();
	    			FileOutputStream out = new FileOutputStream(finalPdf);
	    			PdfCopy copy = new PdfCopy(document, out);
	    			document.open();
	    			for (int i = 1; i <= count; i++) {
	    				PdfReader reader = new PdfReader(outFile+"-"+ i+".pdf");
	    				int n = reader.getNumberOfPages();
	    				for (int j = 1; j <= n; j++) {
	    					document.newPage();
	    					PdfImportedPage page = copy.getImportedPage(reader, j);
	    					copy.addPage(page);
	    				}
	    				reader.close();
	    			}
	    			copy.close();
	    			document.close();
	    			out.close();
	    			out.flush();
	    			//System.out.println("合并sheet pdf 到 "+pdfFile+" 成功！");
	    		}catch (Exception e) {
	    			logger.error(e.getMessage(),e);//e.printStackTrace();

	    		}
	        }
	        //关闭
			Dispatch.call(excel, "Close", new Variant(false));
			//退出
			if (actcom != null) {
				actcom.invoke("Quit", new Variant[0]);
				actcom = null;
			}
			//释放jcom线程
			ComThread.Release();			
			//删除 sheet pdf
			for (int i = 1; i <= count; i++) {
				//删除sheet pdf文件
				File sheetPdf = new File(outFile+"-"+ i+".pdf");
				if(sheetPdf.exists()){
					boolean isDel = sheetPdf.delete();
					if(isDel==false){
						System.gc();
						isDel = sheetPdf.delete();
						//删除本地临时文件
					}
					//System.out.println("Remove Excel Sheet to pdf临时文件："+outFile+"-"+ i+".pdf" +"结果："+isDel);
				}
			}
			//System.out.println("excel 转换为 PDF 完成！");
			return true;
		} catch (Exception es) {
			es.printStackTrace();
		}
		return false;
	}
 

	public static boolean ppt2PDF(String inputFile, String pdfFile) {
		try {
			ActiveXComponent app = new ActiveXComponent("PowerPoint.Application");

			Dispatch ppts = app.getProperty("Presentations").toDispatch();
			Dispatch ppt = Dispatch
					.call(ppts, "Open", inputFile, Boolean.valueOf(true), Boolean.valueOf(true), Boolean.valueOf(false))
					.toDispatch();
			
			File f = new File(pdfFile);
			if(f.exists()){
				f.delete();
			}
			Dispatch.call(ppt, "SaveAs", pdfFile, Integer.valueOf(32));
			Dispatch.call(ppt, "Close");
			app.invoke("Quit");
			//System.out.println("ppt转换为PDF完成！");
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		}
		return false;
	}

	public static boolean word2PDF(String inputFile, String pdfFile) {
		ReleaseManager rm = null;
		IDispatch app = null;
		IDispatch doc;
		try {
			rm = new ReleaseManager();
			app = new IDispatch(rm, "Word.Application");
			app.put("Visible", Boolean.valueOf(false));
			IDispatch docs = (IDispatch) app.get("Documents");
			doc = (IDispatch) docs.method("Open",
					new Object[] { inputFile, Boolean.valueOf(false), Boolean.valueOf(true) });
			
			File f = new File(pdfFile);
			if(f.exists()){
				f.delete();
			}
			doc.method("SaveAs", new Object[] { pdfFile, Integer.valueOf(17) });
			doc.method("Close", new Object[] { Boolean.valueOf(false) });
			app.method("Quit", null);
			//System.out.println("word转换为PDF完成！");
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
			return false;
		} finally {
			try {
				app = null;
				if(rm!=null){
					rm.release();
					rm = null;
				}				
			} catch (Exception e) {
				logger.error(e.getMessage(),e);//e.printStackTrace();
			}
		}
	}

	public static boolean office2Pdf(String inputFile, String pdfFile) {
		String suffix = inputFile.substring(inputFile.lastIndexOf(".") + 1).toLowerCase();
		//System.out.println("文件后缀："+suffix);
		File file = new File(inputFile);
		if (!(file.exists())) {
			//System.err.println("文件不存在！");
			return false;
		}
		if (suffix.equalsIgnoreCase("pdf")) {
			//System.out.println("PDF文件无需转换为PDF!");
			try {
				FileSystemOpt.fileCopy(inputFile, pdfFile);
				return true;
			} catch (IOException e) {
			}
			return false;
		}
		if ((suffix.equalsIgnoreCase("doc")) || (suffix.equalsIgnoreCase("docx")))
			return word2PDF(inputFile, pdfFile);
		if ((suffix.equalsIgnoreCase("ppt")) || (suffix.equalsIgnoreCase("pptx")))
			return ppt2PDF(inputFile, pdfFile);
		if ((suffix.equalsIgnoreCase("xls")) || (suffix.equalsIgnoreCase("xlsx"))) {
			return excel2PDF(inputFile, pdfFile);
		}
		//System.out.println("文件格式不支持转换为PDF!");
		return false;
	}	
	
	public static void excelToPdf(String excelFileName, String pdfFileName, int Orientation) {
		ComThread.InitSTA();
		ActiveXComponent app = new ActiveXComponent("Excel.Application");
		try {
			app.setProperty("Visible", new Variant(false));
			Dispatch workbooks = app.getProperty("Workbooks").toDispatch();
			Dispatch workbook = Dispatch
					.invoke(workbooks, "Open", 1,
							new Object[] { excelFileName, new Variant(false), new Variant(false) }, new int[3])
					.toDispatch();
			Dispatch currentSheet = Dispatch.get(workbook, "ActiveSheet").toDispatch();
			Dispatch page = Dispatch.get(currentSheet, "PageSetup").toDispatch();
			Dispatch.put(page, "PrintArea", false);
			Dispatch.put(page, "Orientation", Orientation);
			Dispatch.put(page, "PaperSize", Integer.valueOf(9));
			Dispatch.put(page, "Zoom", false);
			Dispatch.put(page, "FitToPagesTall", false);
			Dispatch.put(page, "FitToPagesWide", 1);
			Variant f = new Variant(false);
			String tempFile = "E://ZJN//ZJN_FILES//";
			File tempDir = new File(tempFile);
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			tempFile = tempFile + "temp.pdf";
			Dispatch.invoke(workbook, "SaveAs", Dispatch.Method,
					new Object[] { tempFile, new Variant(57), new Variant(false), new Variant(57), new Variant(57),
							new Variant(false), new Variant(true), new Variant(57), new Variant(true),
							new Variant(true), new Variant(true) },
					new int[1]);
			File file = new File(tempFile);
			file.renameTo(new File(pdfFileName));
			file.delete();
			Dispatch.call(workbook, "Close", f);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		} finally {
			if (app != null) {
				app.invoke("Quit", new Variant[0]);
			}
			ComThread.Release();
			System.gc();
			System.runFinalization();
		}
	}
}