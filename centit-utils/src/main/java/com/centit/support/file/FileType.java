package com.centit.support.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author 朱晓文 杨淮生 codefan@sina.com
 *
 */
@SuppressWarnings("unused")
public abstract class FileType {

	private FileType() {
		throw new IllegalAccessError("Utility class");
	}

	protected static final Logger logger = LoggerFactory.getLogger(FileIOOpt.class);

	protected static final HashMap<String, String> mFileTypes = new HashMap<String, String>(42);
	protected static final HashMap<String, String> extMimeTypeMap 
				= new HashMap<String, String>(1280);
	
	static {
		// images
		mFileTypes.put("FFD8FF", "jpg");
		mFileTypes.put("89504E47", "png");
		mFileTypes.put("47494638", "gif");
		mFileTypes.put("49492A00", "tif");
		mFileTypes.put("424D", "bmp");
		//
		mFileTypes.put("41433130", "dwg"); // CAD
		mFileTypes.put("38425053", "psd");
		mFileTypes.put("7B5C727466", "rtf"); // 日记本
		mFileTypes.put("3C3F786D6C", "xml");
		mFileTypes.put("68746D6C3E", "html");
		mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
		mFileTypes.put("D0CF11E0", "office2003");
		mFileTypes.put("5374616E64617264204A", "mdb");
		mFileTypes.put("252150532D41646F6265", "ps");
		mFileTypes.put("255044462D312E", "pdf");
		mFileTypes.put("504B0304", "officeX");
		mFileTypes.put("52617221", "rar");
		mFileTypes.put("57415645", "wav");
		mFileTypes.put("41564920", "avi");
		mFileTypes.put("2E524D46", "rm");
		mFileTypes.put("000001BA", "mpg");
		mFileTypes.put("000001B3", "mpg");
		mFileTypes.put("6D6F6F76", "mov");
		mFileTypes.put("3026B2758E66CF11", "asf");
		mFileTypes.put("4D546864", "mid");
		mFileTypes.put("1F8B08", "gz");
		
		extMimeTypeMap.put("123", "application/vnd.lotus-1-2-3");
		extMimeTypeMap.put("323", "text/h323");
		extMimeTypeMap.put("3dml", "text/vnd.in3d.3dml");
		extMimeTypeMap.put("3g2", "video/3gpp2");
		extMimeTypeMap.put("3gp", "video/3gpp");
		extMimeTypeMap.put("7z", "application/x-7z-compressed");
		extMimeTypeMap.put("aab", "application/x-authorware-bin");
		extMimeTypeMap.put("aac", "audio/x-aac");
		extMimeTypeMap.put("aam", "application/x-authorware-map");
		extMimeTypeMap.put("aas", "application/x-authorware-seg");
		extMimeTypeMap.put("abw", "application/x-abiword");
		extMimeTypeMap.put("ac", "application/pkix-attr-cert");
		extMimeTypeMap.put("acc", "application/vnd.americandynamics.acc");
		extMimeTypeMap.put("ace", "application/x-ace-compressed");
		extMimeTypeMap.put("acu", "application/vnd.acucobol");
		extMimeTypeMap.put("acx", "application/internet-property-stream");
		extMimeTypeMap.put("adp", "audio/adpcm");
		extMimeTypeMap.put("aep", "application/vnd.audiograph");
		extMimeTypeMap.put("afp", "application/vnd.ibm.modcap");
		extMimeTypeMap.put("ahead", "application/vnd.ahead.space");
		extMimeTypeMap.put("ai", "application/postscript");
		extMimeTypeMap.put("aif", "audio/x-aiff");
		extMimeTypeMap.put("aifc", "audio/x-aiff");
		extMimeTypeMap.put("aiff", "audio/x-aiff");
		extMimeTypeMap.put("air", "application/vnd.adobe.air-application-installer-package+zip");
		extMimeTypeMap.put("ait", "application/vnd.dvb.ait");
		extMimeTypeMap.put("ami", "application/vnd.amiga.ami");
		extMimeTypeMap.put("apk", "application/vnd.android.package-archive");
		extMimeTypeMap.put("application", "application/x-ms-application");
		extMimeTypeMap.put("apr", "application/vnd.lotus-approach");
		extMimeTypeMap.put("asf", "video/x-ms-asf");
		extMimeTypeMap.put("aso", "application/vnd.accpac.simply.aso");
		extMimeTypeMap.put("asr", "video/x-ms-asf");
		extMimeTypeMap.put("asx", "video/x-ms-asf");
		extMimeTypeMap.put("atc", "application/vnd.acucorp");
		extMimeTypeMap.put("atom", "application/atom+xml");
		extMimeTypeMap.put("atomcat", "application/atomcat+xml");
		extMimeTypeMap.put("atomsvc", "application/atomsvc+xml");
		extMimeTypeMap.put("atx", "application/vnd.antix.game-component");
		extMimeTypeMap.put("au", "audio/basic");
		extMimeTypeMap.put("avi", "video/x-msvideo");
		extMimeTypeMap.put("aw", "application/applixware");
		extMimeTypeMap.put("axs", "application/olescript");
		extMimeTypeMap.put("azf", "application/vnd.airzip.filesecure.azf");
		extMimeTypeMap.put("azs", "application/vnd.airzip.filesecure.azs");
		extMimeTypeMap.put("azw", "application/vnd.amazon.ebook");
		extMimeTypeMap.put("bas", "text/plain");
		extMimeTypeMap.put("bcpio", "application/x-bcpio");
		extMimeTypeMap.put("bdf", "application/x-font-bdf");
		extMimeTypeMap.put("bdm", "application/vnd.syncml.dm+wbxml");
		extMimeTypeMap.put("bed", "application/vnd.realvnc.bed");
		extMimeTypeMap.put("bh2", "application/vnd.fujitsu.oasysprs");
		extMimeTypeMap.put("bin", "application/octet-stream");
		extMimeTypeMap.put("bmi", "application/vnd.bmi");
		extMimeTypeMap.put("bmp", "image/bmp");
		extMimeTypeMap.put("box", "application/vnd.previewsystems.box");
		extMimeTypeMap.put("btif", "image/prs.btif");
		extMimeTypeMap.put("bz", "application/x-bzip");
		extMimeTypeMap.put("bz2", "application/x-bzip2");
		extMimeTypeMap.put("c", "text/plain");
		extMimeTypeMap.put("c11amc", "application/vnd.cluetrust.cartomobile-config");
		extMimeTypeMap.put("c11amz", "application/vnd.cluetrust.cartomobile-config-pkg");
		extMimeTypeMap.put("c4g", "application/vnd.clonk.c4group");
		extMimeTypeMap.put("cab", "application/vnd.ms-cab-compressed");
		extMimeTypeMap.put("car", "application/vnd.curl.car");
		extMimeTypeMap.put("cat", "application/vnd.ms-pkiseccat");
		extMimeTypeMap.put("ccxml", "application/ccxml+xml,");
		extMimeTypeMap.put("cdbcmsg", "application/vnd.contact.cmsg");
		extMimeTypeMap.put("cdf", "application/x-cdf");
		extMimeTypeMap.put("cdkey", "application/vnd.mediastation.cdkey");
		extMimeTypeMap.put("cdmia", "application/cdmi-capability");
		extMimeTypeMap.put("cdmic", "application/cdmi-container");
		extMimeTypeMap.put("cdmid", "application/cdmi-domain");
		extMimeTypeMap.put("cdmio", "application/cdmi-object");
		extMimeTypeMap.put("cdmiq", "application/cdmi-queue");
		extMimeTypeMap.put("cdx", "chemical/x-cdx");
		extMimeTypeMap.put("cdxml", "application/vnd.chemdraw+xml");
		extMimeTypeMap.put("cdy", "application/vnd.cinderella");
		extMimeTypeMap.put("cer", "application/x-x509-ca-cert");
		extMimeTypeMap.put("cgm", "image/cgm");
		extMimeTypeMap.put("chat", "application/x-chat");
		extMimeTypeMap.put("chm", "application/vnd.ms-htmlhelp");
		extMimeTypeMap.put("chrt", "application/vnd.kde.kchart");
		extMimeTypeMap.put("cif", "chemical/x-cif");
		extMimeTypeMap.put("cii", "application/vnd.anser-web-certificate-issue-initiation");
		extMimeTypeMap.put("cil", "application/vnd.ms-artgalry");
		extMimeTypeMap.put("cla", "application/vnd.claymore");
		extMimeTypeMap.put("class", "application/java-vm");
		extMimeTypeMap.put("clkk", "application/vnd.crick.clicker.keyboard");
		extMimeTypeMap.put("clkp", "application/vnd.crick.clicker.palette");
		extMimeTypeMap.put("clkt", "application/vnd.crick.clicker.template");
		extMimeTypeMap.put("clkw", "application/vnd.crick.clicker.wordbank");
		extMimeTypeMap.put("clkx", "application/vnd.crick.clicker");
		extMimeTypeMap.put("clp", "application/x-msclip");
		extMimeTypeMap.put("cmc", "application/vnd.cosmocaller");
		extMimeTypeMap.put("cmdf", "chemical/x-cmdf");
		extMimeTypeMap.put("cml", "chemical/x-cml");
		extMimeTypeMap.put("cmp", "application/vnd.yellowriver-custom-menu");
		extMimeTypeMap.put("cmx", "image/x-cmx");
		extMimeTypeMap.put("cod", "image/cis-cod");
		//extMimeTypeMap.put("cod", "application/vnd.rim.cod");
		extMimeTypeMap.put("cpio", "application/x-cpio");
		extMimeTypeMap.put("cpt", "application/mac-compactpro");
		extMimeTypeMap.put("crd", "application/x-mscardfile");
		extMimeTypeMap.put("crl", "application/pkix-crl");
		extMimeTypeMap.put("crt", "application/x-x509-ca-cert");
		extMimeTypeMap.put("cryptonote", "application/vnd.rig.cryptonote");
		extMimeTypeMap.put("csh", "application/x-csh");
		extMimeTypeMap.put("csml", "chemical/x-csml");
		extMimeTypeMap.put("csp", "application/vnd.commonspace");
		extMimeTypeMap.put("css", "text/css");
		extMimeTypeMap.put("csv", "text/csv");
		extMimeTypeMap.put("cu", "application/cu-seeme");
		extMimeTypeMap.put("curl", "text/vnd.curl");
		extMimeTypeMap.put("cww", "application/prs.cww");
		extMimeTypeMap.put("dae", "model/vnd.collada+xml");
		extMimeTypeMap.put("daf", "application/vnd.mobius.daf");
		extMimeTypeMap.put("davmount", "application/davmount+xml");
		extMimeTypeMap.put("dcr", "application/x-director");
		extMimeTypeMap.put("dcurl", "text/vnd.curl.dcurl");
		extMimeTypeMap.put("dd2", "application/vnd.oma.dd2+xml");
		extMimeTypeMap.put("ddd", "application/vnd.fujixerox.ddd");
		extMimeTypeMap.put("deb", "application/x-debian-package");
		extMimeTypeMap.put("der", "application/x-x509-ca-cert");
		extMimeTypeMap.put("dfac", "application/vnd.dreamfactory");
		extMimeTypeMap.put("dir", "application/x-director");
		extMimeTypeMap.put("dis", "application/vnd.mobius.dis");
		extMimeTypeMap.put("djvu", "image/vnd.djvu");
		extMimeTypeMap.put("dll", "application/x-msdownload");
		extMimeTypeMap.put("dms", "application/octet-stream");
		extMimeTypeMap.put("dna", "application/vnd.dna");
		extMimeTypeMap.put("doc", "application/msword");
		extMimeTypeMap.put("docm", "application/vnd.ms-word.document.macroenabled.12");
		extMimeTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		extMimeTypeMap.put("dot", "application/msword");
		extMimeTypeMap.put("dotm", "application/vnd.ms-word.template.macroenabled.12");
		extMimeTypeMap.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		extMimeTypeMap.put("dp", "application/vnd.osgi.dp");
		extMimeTypeMap.put("dpg", "application/vnd.dpgraph");
		extMimeTypeMap.put("dra", "audio/vnd.dra");
		extMimeTypeMap.put("dsc", "text/prs.lines.tag");
		extMimeTypeMap.put("dssc", "application/dssc+der");
		extMimeTypeMap.put("dtb", "application/x-dtbook+xml");
		extMimeTypeMap.put("dtd", "application/xml-dtd");
		extMimeTypeMap.put("dts", "audio/vnd.dts");
		extMimeTypeMap.put("dtshd", "audio/vnd.dts.hd");
		extMimeTypeMap.put("dvi", "application/x-dvi");
		extMimeTypeMap.put("dwf", "model/vnd.dwf");
		extMimeTypeMap.put("dwg", "image/vnd.dwg");
		extMimeTypeMap.put("dxf", "image/vnd.dxf");
		extMimeTypeMap.put("dxp", "application/vnd.spotfire.dxp");
		extMimeTypeMap.put("dxr", "application/x-director");
		extMimeTypeMap.put("ecelp4800", "audio/vnd.nuera.ecelp4800");
		extMimeTypeMap.put("ecelp7470", "audio/vnd.nuera.ecelp7470");
		extMimeTypeMap.put("ecelp9600", "audio/vnd.nuera.ecelp9600");
		extMimeTypeMap.put("edm", "application/vnd.novadigm.edm");
		extMimeTypeMap.put("edx", "application/vnd.novadigm.edx");
		extMimeTypeMap.put("efif", "application/vnd.picsel");
		extMimeTypeMap.put("ei6", "application/vnd.pg.osasli");
		extMimeTypeMap.put("eml", "message/rfc822");
		extMimeTypeMap.put("emma", "application/emma+xml");
		extMimeTypeMap.put("eol", "audio/vnd.digital-winds");
		extMimeTypeMap.put("eot", "application/vnd.ms-fontobject");
		extMimeTypeMap.put("eps", "application/postscript");
		extMimeTypeMap.put("epub", "application/epub+zip");
		extMimeTypeMap.put("es", "application/ecmascript");
		extMimeTypeMap.put("es3", "application/vnd.eszigno3+xml");
		extMimeTypeMap.put("esf", "application/vnd.epson.esf");
		extMimeTypeMap.put("etx", "text/x-setext");
		extMimeTypeMap.put("evy", "application/envoy");
		//extMimeTypeMap.put("exe", "application/octet-stream");
		extMimeTypeMap.put("exe", "application/x-msdownload");
		extMimeTypeMap.put("exi", "application/exi");
		extMimeTypeMap.put("ext", "application/vnd.novadigm.ext");
		extMimeTypeMap.put("ez2", "application/vnd.ezpix-album");
		extMimeTypeMap.put("ez3", "application/vnd.ezpix-package");
		extMimeTypeMap.put("f", "text/x-fortran");
		extMimeTypeMap.put("f4v", "video/x-f4v");
		extMimeTypeMap.put("fbs", "image/vnd.fastbidsheet");
		extMimeTypeMap.put("fcs", "application/vnd.isac.fcs");
		extMimeTypeMap.put("fdf", "application/vnd.fdf");
		extMimeTypeMap.put("fe_launch", "application/vnd.denovo.fcselayout-link");
		extMimeTypeMap.put("fg5", "application/vnd.fujitsu.oasysgp");
		extMimeTypeMap.put("fh", "image/x-freehand");
		extMimeTypeMap.put("fif", "application/fractals");
		extMimeTypeMap.put("fig", "application/x-xfig");
		extMimeTypeMap.put("fli", "video/x-fli");
		extMimeTypeMap.put("flo", "application/vnd.micrografx.flo");
		extMimeTypeMap.put("flr", "x-world/x-vrml");
		extMimeTypeMap.put("flv", "video/x-flv");
		extMimeTypeMap.put("flw", "application/vnd.kde.kivio");
		extMimeTypeMap.put("flx", "text/vnd.fmi.flexstor");
		extMimeTypeMap.put("fly", "text/vnd.fly");
		extMimeTypeMap.put("fm", "application/vnd.framemaker");
		extMimeTypeMap.put("fnc", "application/vnd.frogans.fnc");
		extMimeTypeMap.put("fpx", "image/vnd.fpx");
		extMimeTypeMap.put("fsc", "application/vnd.fsc.weblaunch");
		extMimeTypeMap.put("fst", "image/vnd.fst");
		extMimeTypeMap.put("ftc", "application/vnd.fluxtime.clip");
		extMimeTypeMap.put("fti", "application/vnd.anser-web-funds-transfer-initiation");
		extMimeTypeMap.put("fvt", "video/vnd.fvt");
		extMimeTypeMap.put("fxp", "application/vnd.adobe.fxp");
		extMimeTypeMap.put("fzs", "application/vnd.fuzzysheet");
		extMimeTypeMap.put("g2w", "application/vnd.geoplan");
		extMimeTypeMap.put("g3", "image/g3fax");
		extMimeTypeMap.put("g3w", "application/vnd.geospace");
		extMimeTypeMap.put("gac", "application/vnd.groove-account");
		extMimeTypeMap.put("gdl", "model/vnd.gdl");
		extMimeTypeMap.put("geo", "application/vnd.dynageo");
		extMimeTypeMap.put("gex", "application/vnd.geometry-explorer");
		extMimeTypeMap.put("ggb", "application/vnd.geogebra.file");
		extMimeTypeMap.put("ggt", "application/vnd.geogebra.tool");
		extMimeTypeMap.put("ghf", "application/vnd.groove-help");
		extMimeTypeMap.put("gif", "image/gif");
		extMimeTypeMap.put("gim", "application/vnd.groove-identity-message");
		extMimeTypeMap.put("gmx", "application/vnd.gmx");
		extMimeTypeMap.put("gnumeric", "application/x-gnumeric");
		extMimeTypeMap.put("gph", "application/vnd.flographit");
		extMimeTypeMap.put("gqf", "application/vnd.grafeq");
		extMimeTypeMap.put("gram", "application/srgs");
		extMimeTypeMap.put("grv", "application/vnd.groove-injector");
		extMimeTypeMap.put("grxml", "application/srgs+xml");
		extMimeTypeMap.put("gsf", "application/x-font-ghostscript");
		extMimeTypeMap.put("gtar", "application/x-gtar");
		extMimeTypeMap.put("gtm", "application/vnd.groove-tool-message");
		extMimeTypeMap.put("gtw", "model/vnd.gtw");
		extMimeTypeMap.put("gv", "text/vnd.graphviz");
		extMimeTypeMap.put("gxt", "application/vnd.geonext");
		extMimeTypeMap.put("gz", "application/x-gzip");
		extMimeTypeMap.put("h", "text/plain");
		extMimeTypeMap.put("h261", "video/h261");
		extMimeTypeMap.put("h263", "video/h263");
		extMimeTypeMap.put("h264", "video/h264");
		extMimeTypeMap.put("hal", "application/vnd.hal+xml");
		extMimeTypeMap.put("hbci", "application/vnd.hbci");
		extMimeTypeMap.put("hdf", "application/x-hdf");
		extMimeTypeMap.put("hlp", "application/winhlp");
		extMimeTypeMap.put("hpgl", "application/vnd.hp-hpgl");
		extMimeTypeMap.put("hpid", "application/vnd.hp-hpid");
		extMimeTypeMap.put("hps", "application/vnd.hp-hps");
		extMimeTypeMap.put("hqx", "application/mac-binhex40");
		extMimeTypeMap.put("hta", "application/hta");
		extMimeTypeMap.put("htc", "text/x-component");
		extMimeTypeMap.put("htke", "application/vnd.kenameaapp");
		extMimeTypeMap.put("htm", "text/html");
		extMimeTypeMap.put("html", "text/html");
		extMimeTypeMap.put("htt", "text/webviewhtml");
		extMimeTypeMap.put("hvd", "application/vnd.yamaha.hv-dic");
		extMimeTypeMap.put("hvp", "application/vnd.yamaha.hv-voice");
		extMimeTypeMap.put("hvs", "application/vnd.yamaha.hv-script");
		extMimeTypeMap.put("i2g", "application/vnd.intergeo");
		extMimeTypeMap.put("icc", "application/vnd.iccprofile");
		extMimeTypeMap.put("ice", "x-conference/x-cooltalk");
		extMimeTypeMap.put("ico", "image/x-icon");
		extMimeTypeMap.put("ics", "text/calendar");
		extMimeTypeMap.put("ief", "image/ief");
		extMimeTypeMap.put("ifm", "application/vnd.shana.informed.formdata");
		extMimeTypeMap.put("igl", "application/vnd.igloader");
		extMimeTypeMap.put("igm", "application/vnd.insors.igm");
		extMimeTypeMap.put("igs", "model/iges");
		extMimeTypeMap.put("igx", "application/vnd.micrografx.igx");
		extMimeTypeMap.put("iif", "application/vnd.shana.informed.interchange");
		extMimeTypeMap.put("iii", "application/x-iphone");
		extMimeTypeMap.put("imp", "application/vnd.accpac.simply.imp");
		extMimeTypeMap.put("ims", "application/vnd.ms-ims");
		extMimeTypeMap.put("ins", "application/x-internet-signup");
		extMimeTypeMap.put("ipfix", "application/ipfix");
		extMimeTypeMap.put("ipk", "application/vnd.shana.informed.package");
		extMimeTypeMap.put("irm", "application/vnd.ibm.rights-management");
		extMimeTypeMap.put("irp", "application/vnd.irepository.package+xml");
		extMimeTypeMap.put("isp", "application/x-internet-signup");
		extMimeTypeMap.put("itp", "application/vnd.shana.informed.formtemplate");
		extMimeTypeMap.put("ivp", "application/vnd.immervision-ivp");
		extMimeTypeMap.put("ivu", "application/vnd.immervision-ivu");
		extMimeTypeMap.put("jad", "text/vnd.sun.j2me.app-descriptor");
		extMimeTypeMap.put("jam", "application/vnd.jam");
		extMimeTypeMap.put("jar", "application/java-archive");
		extMimeTypeMap.put("java", "text/x-java-source,java");
		extMimeTypeMap.put("jfif", "image/pipeg");
		extMimeTypeMap.put("jisp", "application/vnd.jisp");
		extMimeTypeMap.put("jlt", "application/vnd.hp-jlyt");
		extMimeTypeMap.put("jnlp", "application/x-java-jnlp-file");
		extMimeTypeMap.put("joda", "application/vnd.joost.joda-archive");
		extMimeTypeMap.put("jpe", "image/jpeg");
		extMimeTypeMap.put("jpeg", "image/jpeg");
		extMimeTypeMap.put("jpg", "image/jpeg");
		extMimeTypeMap.put("jpgv", "video/jpeg");
		extMimeTypeMap.put("jpm", "video/jpm");
		extMimeTypeMap.put("js", "application/x-javascript");
		extMimeTypeMap.put("js", "application/javascript");
		extMimeTypeMap.put("json", "application/json");
		extMimeTypeMap.put("karbon", "application/vnd.kde.karbon");
		extMimeTypeMap.put("kfo", "application/vnd.kde.kformula");
		extMimeTypeMap.put("kia", "application/vnd.kidspiration");
		extMimeTypeMap.put("kml", "application/vnd.google-earth.kml+xml");
		extMimeTypeMap.put("kmz", "application/vnd.google-earth.kmz");
		extMimeTypeMap.put("kne", "application/vnd.kinar");
		extMimeTypeMap.put("kon", "application/vnd.kde.kontour");
		extMimeTypeMap.put("kpr", "application/vnd.kde.kpresenter");
		extMimeTypeMap.put("ksp", "application/vnd.kde.kspread");
		extMimeTypeMap.put("ktx", "image/ktx");
		extMimeTypeMap.put("ktz", "application/vnd.kahootz");
		extMimeTypeMap.put("kwd", "application/vnd.kde.kword");
		extMimeTypeMap.put("lasxml", "application/vnd.las.las+xml");
		extMimeTypeMap.put("latex", "application/x-latex");
		extMimeTypeMap.put("lbd", "application/vnd.llamagraphics.life-balance.desktop");
		extMimeTypeMap.put("lbe", "application/vnd.llamagraphics.life-balance.exchange+xml");
		extMimeTypeMap.put("les", "application/vnd.hhe.lesson-player");
		extMimeTypeMap.put("lha", "application/octet-stream");
		extMimeTypeMap.put("link66", "application/vnd.route66.link66+xml");
		extMimeTypeMap.put("lrm", "application/vnd.ms-lrm");
		extMimeTypeMap.put("lsf", "video/x-la-asf");
		extMimeTypeMap.put("lsx", "video/x-la-asf");
		extMimeTypeMap.put("ltf", "application/vnd.frogans.ltf");
		extMimeTypeMap.put("lvp", "audio/vnd.lucent.voice");
		extMimeTypeMap.put("lwp", "application/vnd.lotus-wordpro");
		extMimeTypeMap.put("lzh", "application/octet-stream");
		extMimeTypeMap.put("m13", "application/x-msmediaview");
		extMimeTypeMap.put("m14", "application/x-msmediaview");
		extMimeTypeMap.put("m21", "application/mp21");
		extMimeTypeMap.put("m3u", "audio/x-mpegurl");
		extMimeTypeMap.put("m3u8", "application/vnd.apple.mpegurl");
		extMimeTypeMap.put("m4v", "video/x-m4v");
		extMimeTypeMap.put("ma", "application/mathematica");
		extMimeTypeMap.put("mads", "application/mads+xml");
		extMimeTypeMap.put("mag", "application/vnd.ecowin.chart");
		extMimeTypeMap.put("man", "application/x-troff-man");
		extMimeTypeMap.put("mathml", "application/mathml+xml");
		extMimeTypeMap.put("mbk", "application/vnd.mobius.mbk");
		extMimeTypeMap.put("mbox", "application/mbox");
		extMimeTypeMap.put("mc1", "application/vnd.medcalcdata");
		extMimeTypeMap.put("mcd", "application/vnd.mcd");
		extMimeTypeMap.put("mcurl", "text/vnd.curl.mcurl");
		extMimeTypeMap.put("mdb", "application/x-msaccess");
		extMimeTypeMap.put("mdi", "image/vnd.ms-modi");
		extMimeTypeMap.put("me", "application/x-troff-me");
		extMimeTypeMap.put("meta4", "application/metalink4+xml");
		extMimeTypeMap.put("mets", "application/mets+xml");
		extMimeTypeMap.put("mfm", "application/vnd.mfmp");
		extMimeTypeMap.put("mgp", "application/vnd.osgeo.mapguide.package");
		extMimeTypeMap.put("mgz", "application/vnd.proteus.magazine");
		extMimeTypeMap.put("mht", "message/rfc822");
		extMimeTypeMap.put("mhtml", "message/rfc822");
		extMimeTypeMap.put("mid", "audio/mid");
		//extMimeTypeMap.put("mid", "audio/midi");
		extMimeTypeMap.put("mif", "application/vnd.mif");
		extMimeTypeMap.put("mj2", "video/mj2");
		extMimeTypeMap.put("mlp", "application/vnd.dolby.mlp");
		extMimeTypeMap.put("mmd", "application/vnd.chipnuts.karaoke-mmd");
		extMimeTypeMap.put("mmf", "application/vnd.smaf");
		extMimeTypeMap.put("mmr", "image/vnd.fujixerox.edmics-mmr");
		extMimeTypeMap.put("mny", "application/x-msmoney");
		extMimeTypeMap.put("mods", "application/mods+xml");
		extMimeTypeMap.put("mov", "video/quicktime");
		extMimeTypeMap.put("movie", "video/x-sgi-movie");
		extMimeTypeMap.put("mp2", "video/mpeg");
		extMimeTypeMap.put("mp3", "audio/mpeg");
		extMimeTypeMap.put("mp4", "video/mp4");
		//extMimeTypeMap.put("mp4", "application/mp4");
		extMimeTypeMap.put("mp4a", "audio/mp4");
		extMimeTypeMap.put("mpa", "video/mpeg");
		extMimeTypeMap.put("mpc", "application/vnd.mophun.certificate");
		extMimeTypeMap.put("mpe", "video/mpeg");
		extMimeTypeMap.put("mpeg", "video/mpeg");
		extMimeTypeMap.put("mpg", "video/mpeg");
		extMimeTypeMap.put("mpga", "audio/mpeg");
		extMimeTypeMap.put("mpkg", "application/vnd.apple.installer+xml");
		extMimeTypeMap.put("mpm", "application/vnd.blueice.multipass");
		extMimeTypeMap.put("mpn", "application/vnd.mophun.application");
		extMimeTypeMap.put("mpp", "application/vnd.ms-project");
		extMimeTypeMap.put("mpv2", "video/mpeg");
		extMimeTypeMap.put("mpy", "application/vnd.ibm.minipay");
		extMimeTypeMap.put("mqy", "application/vnd.mobius.mqy");
		extMimeTypeMap.put("mrc", "application/marc");
		extMimeTypeMap.put("mrcx", "application/marcxml+xml");
		extMimeTypeMap.put("ms", "application/x-troff-ms");
		extMimeTypeMap.put("mscml", "application/mediaservercontrol+xml");
		extMimeTypeMap.put("mseq", "application/vnd.mseq");
		extMimeTypeMap.put("msf", "application/vnd.epson.msf");
		extMimeTypeMap.put("msh", "model/mesh");
		extMimeTypeMap.put("msl", "application/vnd.mobius.msl");
		extMimeTypeMap.put("msty", "application/vnd.muvee.style");
		extMimeTypeMap.put("mts", "model/vnd.mts");
		extMimeTypeMap.put("mus", "application/vnd.musician");
		extMimeTypeMap.put("musicxml", "application/vnd.recordare.musicxml+xml");
		extMimeTypeMap.put("mvb", "application/x-msmediaview");
		extMimeTypeMap.put("mwf", "application/vnd.mfer");
		extMimeTypeMap.put("mxf", "application/mxf");
		extMimeTypeMap.put("mxl", "application/vnd.recordare.musicxml");
		extMimeTypeMap.put("mxml", "application/xv+xml");
		extMimeTypeMap.put("mxs", "application/vnd.triscape.mxs");
		extMimeTypeMap.put("mxu", "video/vnd.mpegurl");
		extMimeTypeMap.put("n3", "text/n3");
		extMimeTypeMap.put("nbp", "application/vnd.wolfram.player");
		extMimeTypeMap.put("nc", "application/x-netcdf");
		extMimeTypeMap.put("ncx", "application/x-dtbncx+xml");
		extMimeTypeMap.put("n-gage", "application/vnd.nokia.n-gage.symbian.install");
		extMimeTypeMap.put("ngdat", "application/vnd.nokia.n-gage.data");
		extMimeTypeMap.put("nlu", "application/vnd.neurolanguage.nlu");
		extMimeTypeMap.put("nml", "application/vnd.enliven");
		extMimeTypeMap.put("nnd", "application/vnd.noblenet-directory");
		extMimeTypeMap.put("nns", "application/vnd.noblenet-sealer");
		extMimeTypeMap.put("nnw", "application/vnd.noblenet-web");
		extMimeTypeMap.put("npx", "image/vnd.net-fpx");
		extMimeTypeMap.put("nsf", "application/vnd.lotus-notes");
		extMimeTypeMap.put("nws", "message/rfc822");
		extMimeTypeMap.put("oa2", "application/vnd.fujitsu.oasys2");
		extMimeTypeMap.put("oa3", "application/vnd.fujitsu.oasys3");
		extMimeTypeMap.put("oas", "application/vnd.fujitsu.oasys");
		extMimeTypeMap.put("obd", "application/x-msbinder");
		extMimeTypeMap.put("oda", "application/oda");
		extMimeTypeMap.put("odb", "application/vnd.oasis.opendocument.database");
		extMimeTypeMap.put("odc", "application/vnd.oasis.opendocument.chart");
		extMimeTypeMap.put("odf", "application/vnd.oasis.opendocument.formula");
		extMimeTypeMap.put("odft", "application/vnd.oasis.opendocument.formula-template");
		extMimeTypeMap.put("odg", "application/vnd.oasis.opendocument.graphics");
		extMimeTypeMap.put("odi", "application/vnd.oasis.opendocument.image");
		extMimeTypeMap.put("odm", "application/vnd.oasis.opendocument.text-master");
		extMimeTypeMap.put("odp", "application/vnd.oasis.opendocument.presentation");
		extMimeTypeMap.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		extMimeTypeMap.put("odt", "application/vnd.oasis.opendocument.text");
		extMimeTypeMap.put("oga", "audio/ogg");
		extMimeTypeMap.put("ogv", "video/ogg");
		extMimeTypeMap.put("ogx", "application/ogg");
		extMimeTypeMap.put("onetoc", "application/onenote");
		extMimeTypeMap.put("opf", "application/oebps-package+xml");
		extMimeTypeMap.put("org", "application/vnd.lotus-organizer");
		extMimeTypeMap.put("osf", "application/vnd.yamaha.openscoreformat");
		extMimeTypeMap.put("osfpvg", "application/vnd.yamaha.openscoreformat.osfpvg+xml");
		extMimeTypeMap.put("otc", "application/vnd.oasis.opendocument.chart-template");
		extMimeTypeMap.put("otf", "application/x-font-otf");
		extMimeTypeMap.put("otg", "application/vnd.oasis.opendocument.graphics-template");
		extMimeTypeMap.put("oth", "application/vnd.oasis.opendocument.text-web");
		extMimeTypeMap.put("oti", "application/vnd.oasis.opendocument.image-template");
		extMimeTypeMap.put("otp", "application/vnd.oasis.opendocument.presentation-template");
		extMimeTypeMap.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
		extMimeTypeMap.put("ott", "application/vnd.oasis.opendocument.text-template");
		extMimeTypeMap.put("oxt", "application/vnd.openofficeorg.extension");
		extMimeTypeMap.put("p", "text/x-pascal");
		extMimeTypeMap.put("p10", "application/pkcs10");
		extMimeTypeMap.put("p12", "application/x-pkcs12");
		extMimeTypeMap.put("p7b", "application/x-pkcs7-certificates");
		extMimeTypeMap.put("p7c", "application/x-pkcs7-mime");
		extMimeTypeMap.put("p7m", "application/x-pkcs7-mime");
		//extMimeTypeMap.put("p7m", "application/pkcs7-mime");
		extMimeTypeMap.put("p7r", "application/x-pkcs7-certreqresp");
		extMimeTypeMap.put("p7s", "application/x-pkcs7-signature");
		//extMimeTypeMap.put("p7s", "application/pkcs7-signature");
		extMimeTypeMap.put("p8", "application/pkcs8");
		extMimeTypeMap.put("par", "text/plain-bas");
		extMimeTypeMap.put("paw", "application/vnd.pawaafile");
		extMimeTypeMap.put("pbd", "application/vnd.powerbuilder6");
		extMimeTypeMap.put("pbm", "image/x-portable-bitmap");
		extMimeTypeMap.put("pcf", "application/x-font-pcf");
		extMimeTypeMap.put("pcl", "application/vnd.hp-pcl");
		extMimeTypeMap.put("pclxl", "application/vnd.hp-pclxl");
		extMimeTypeMap.put("pcurl", "application/vnd.curl.pcurl");
		extMimeTypeMap.put("pcx", "image/x-pcx");
		extMimeTypeMap.put("pdb", "application/vnd.palm");
		extMimeTypeMap.put("pdf", "application/pdf");
		extMimeTypeMap.put("pfa", "application/x-font-type1");
		extMimeTypeMap.put("pfr", "application/font-tdpfr");
		extMimeTypeMap.put("pfx", "application/x-pkcs12");
		extMimeTypeMap.put("pgm", "image/x-portable-graymap");
		extMimeTypeMap.put("pgn", "application/x-chess-pgn");
		extMimeTypeMap.put("pgp", "application/pgp-signature");
		//extMimeTypeMap.put("pgp", "application/pgp-encrypted");
		extMimeTypeMap.put("pic", "image/x-pict");
		extMimeTypeMap.put("pjpeg", "image/pjpeg");
		extMimeTypeMap.put("pki", "application/pkixcmp");
		extMimeTypeMap.put("pkipath", "application/pkix-pkipath");
		extMimeTypeMap.put("pko", "application/ynd.ms-pkipko");
		extMimeTypeMap.put("plb", "application/vnd.3gpp.pic-bw-large");
		extMimeTypeMap.put("plc", "application/vnd.mobius.plc");
		extMimeTypeMap.put("plf", "application/vnd.pocketlearn");
		extMimeTypeMap.put("pls", "application/pls+xml");
		extMimeTypeMap.put("pma", "application/x-perfmon");
		extMimeTypeMap.put("pmc", "application/x-perfmon");
		extMimeTypeMap.put("pml", "application/x-perfmon");
		//extMimeTypeMap.put("pml", "application/vnd.ctc-posml");
		extMimeTypeMap.put("pmr", "application/x-perfmon");
		extMimeTypeMap.put("pmw", "application/x-perfmon");
		//extMimeTypeMap.put("png", "image/x-png");
		//extMimeTypeMap.put("png", "image/x-citrix-png");
		extMimeTypeMap.put("png", "image/png");
		extMimeTypeMap.put("pnm", "image/x-portable-anymap");
		extMimeTypeMap.put("portpkg", "application/vnd.macports.portpkg");
		extMimeTypeMap.put("pot,", "application/vnd.ms-powerpoint");
		extMimeTypeMap.put("potm", "application/vnd.ms-powerpoint.template.macroenabled.12");
		extMimeTypeMap.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
		extMimeTypeMap.put("ppam", "application/vnd.ms-powerpoint.addin.macroenabled.12");
		extMimeTypeMap.put("ppd", "application/vnd.cups-ppd");
		extMimeTypeMap.put("ppm", "image/x-portable-pixmap");
		extMimeTypeMap.put("pps", "application/vnd.ms-powerpoint");
		extMimeTypeMap.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
		extMimeTypeMap.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		extMimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
		extMimeTypeMap.put("pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12");
		extMimeTypeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		extMimeTypeMap.put("prc", "application/x-mobipocket-ebook");
		extMimeTypeMap.put("pre", "application/vnd.lotus-freelance");
		extMimeTypeMap.put("prf", "application/pics-rules");
		extMimeTypeMap.put("ps", "application/postscript");
		extMimeTypeMap.put("psb", "application/vnd.3gpp.pic-bw-small");
		extMimeTypeMap.put("psd", "image/vnd.adobe.photoshop");
		extMimeTypeMap.put("psf", "application/x-font-linux-psf");
		extMimeTypeMap.put("pskcxml", "application/pskc+xml");
		extMimeTypeMap.put("ptid", "application/vnd.pvi.ptid1");
		extMimeTypeMap.put("pub", "application/x-mspublisher");
		extMimeTypeMap.put("pvb", "application/vnd.3gpp.pic-bw-var");
		extMimeTypeMap.put("pwn", "application/vnd.3m.post-it-notes");
		extMimeTypeMap.put("pya", "audio/vnd.ms-playready.media.pya");
		extMimeTypeMap.put("pyv", "video/vnd.ms-playready.media.pyv");
		extMimeTypeMap.put("qam", "application/vnd.epson.quickanime");
		extMimeTypeMap.put("qbo", "application/vnd.intu.qbo");
		extMimeTypeMap.put("qfx", "application/vnd.intu.qfx");
		extMimeTypeMap.put("qps", "application/vnd.publishare-delta-tree");
		extMimeTypeMap.put("qt", "video/quicktime");
		extMimeTypeMap.put("qxd", "application/vnd.quark.quarkxpress");
		extMimeTypeMap.put("ra", "audio/x-pn-realaudio");
		extMimeTypeMap.put("ram", "audio/x-pn-realaudio");
		extMimeTypeMap.put("rar", "application/x-rar-compressed");
		extMimeTypeMap.put("ras", "image/x-cmu-raster");
		extMimeTypeMap.put("rcprofile", "application/vnd.ipunplugged.rcprofile");
		extMimeTypeMap.put("rdf", "application/rdf+xml");
		extMimeTypeMap.put("rdz", "application/vnd.data-vision.rdz");
		extMimeTypeMap.put("rep", "application/vnd.businessobjects");
		extMimeTypeMap.put("res", "application/x-dtbresource+xml");
		extMimeTypeMap.put("rgb", "image/x-rgb");
		extMimeTypeMap.put("rif", "application/reginfo+xml");
		extMimeTypeMap.put("rip", "audio/vnd.rip");
		extMimeTypeMap.put("rl", "application/resource-lists+xml");
		extMimeTypeMap.put("rlc", "image/vnd.fujixerox.edmics-rlc");
		extMimeTypeMap.put("rld", "application/resource-lists-diff+xml");
		extMimeTypeMap.put("rm", "application/vnd.rn-realmedia");
		extMimeTypeMap.put("rmi", "audio/mid");
		extMimeTypeMap.put("rmp", "audio/x-pn-realaudio-plugin");
		extMimeTypeMap.put("rms", "application/vnd.jcp.javame.midlet-rms");
		extMimeTypeMap.put("rnc", "application/relax-ng-compact-syntax");
		extMimeTypeMap.put("roff", "application/x-troff");
		extMimeTypeMap.put("rp9", "application/vnd.cloanto.rp9");
		extMimeTypeMap.put("rpss", "application/vnd.nokia.radio-presets");
		extMimeTypeMap.put("rpst", "application/vnd.nokia.radio-preset");
		extMimeTypeMap.put("rq", "application/sparql-query");
		extMimeTypeMap.put("rs", "application/rls-services+xml");
		extMimeTypeMap.put("rsd", "application/rsd+xml");
		extMimeTypeMap.put("rss", "application/rss+xml");
		extMimeTypeMap.put("rtf", "application/rtf");
		extMimeTypeMap.put("rtx", "text/richtext");
		extMimeTypeMap.put("s", "text/x-asm");
		extMimeTypeMap.put("saf", "application/vnd.yamaha.smaf-audio");
		extMimeTypeMap.put("sbml", "application/sbml+xml");
		extMimeTypeMap.put("sc", "application/vnd.ibm.secure-container");
		extMimeTypeMap.put("scd", "application/x-msschedule");
		extMimeTypeMap.put("scm", "application/vnd.lotus-screencam");
		extMimeTypeMap.put("scq", "application/scvp-cv-request");
		extMimeTypeMap.put("scs", "application/scvp-cv-response");
		extMimeTypeMap.put("sct", "text/scriptlet");
		extMimeTypeMap.put("scurl", "text/vnd.curl.scurl");
		extMimeTypeMap.put("sda", "application/vnd.stardivision.draw");
		extMimeTypeMap.put("sdc", "application/vnd.stardivision.calc");
		extMimeTypeMap.put("sdd", "application/vnd.stardivision.impress");
		extMimeTypeMap.put("sdkm", "application/vnd.solent.sdkm+xml");
		extMimeTypeMap.put("sdp", "application/sdp");
		extMimeTypeMap.put("sdw", "application/vnd.stardivision.writer");
		extMimeTypeMap.put("see", "application/vnd.seemail");
		extMimeTypeMap.put("seed", "application/vnd.fdsn.seed");
		extMimeTypeMap.put("sema", "application/vnd.sema");
		extMimeTypeMap.put("semd", "application/vnd.semd");
		extMimeTypeMap.put("semf", "application/vnd.semf");
		extMimeTypeMap.put("ser", "application/java-serialized-object");
		extMimeTypeMap.put("setpay", "application/set-payment-initiation");
		extMimeTypeMap.put("setreg", "application/set-registration-initiation");
		extMimeTypeMap.put("sfd-hdstx", "application/vnd.hydrostatix.sof-data");
		extMimeTypeMap.put("sfs", "application/vnd.spotfire.sfs");
		extMimeTypeMap.put("sgl", "application/vnd.stardivision.writer-global");
		extMimeTypeMap.put("sgml", "text/sgml");
		extMimeTypeMap.put("sh", "application/x-sh");
		extMimeTypeMap.put("shar", "application/x-shar");
		extMimeTypeMap.put("shf", "application/shf+xml");
		extMimeTypeMap.put("sis", "application/vnd.symbian.install");
		extMimeTypeMap.put("sit", "application/x-stuffit");
		extMimeTypeMap.put("sitx", "application/x-stuffitx");
		extMimeTypeMap.put("skp", "application/vnd.koan");
		extMimeTypeMap.put("sldm", "application/vnd.ms-powerpoint.slide.macroenabled.12");
		extMimeTypeMap.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
		extMimeTypeMap.put("slt", "application/vnd.epson.salt");
		extMimeTypeMap.put("sm", "application/vnd.stepmania.stepchart");
		extMimeTypeMap.put("smf", "application/vnd.stardivision.math");
		extMimeTypeMap.put("smi", "application/smil+xml");
		extMimeTypeMap.put("snd", "audio/basic");
		extMimeTypeMap.put("snf", "application/x-font-snf");
		extMimeTypeMap.put("spc", "application/x-pkcs7-certificates");
		extMimeTypeMap.put("spf", "application/vnd.yamaha.smaf-phrase");
		extMimeTypeMap.put("spl", "application/futuresplash");
		extMimeTypeMap.put("spl", "application/x-futuresplash");
		extMimeTypeMap.put("spot", "text/vnd.in3d.spot");
		extMimeTypeMap.put("spp", "application/scvp-vp-response");
		extMimeTypeMap.put("spq", "application/scvp-vp-request");
		extMimeTypeMap.put("src", "application/x-wais-source");
		extMimeTypeMap.put("sru", "application/sru+xml");
		extMimeTypeMap.put("srx", "application/sparql-results+xml");
		extMimeTypeMap.put("sse", "application/vnd.kodak-descriptor");
		extMimeTypeMap.put("ssf", "application/vnd.epson.ssf");
		extMimeTypeMap.put("ssml", "application/ssml+xml");
		extMimeTypeMap.put("sst", "application/vnd.ms-pkicertstore");
		extMimeTypeMap.put("st", "application/vnd.sailingtracker.track");
		extMimeTypeMap.put("stc", "application/vnd.sun.xml.calc.template");
		extMimeTypeMap.put("std", "application/vnd.sun.xml.draw.template");
		extMimeTypeMap.put("stf", "application/vnd.wt.stf");
		extMimeTypeMap.put("sti", "application/vnd.sun.xml.impress.template");
		extMimeTypeMap.put("stk", "application/hyperstudio");
		extMimeTypeMap.put("stl", "application/vnd.ms-pkistl");
		extMimeTypeMap.put("stl", "application/vnd.ms-pki.stl");
		extMimeTypeMap.put("stm", "text/html");
		extMimeTypeMap.put("str", "application/vnd.pg.format");
		extMimeTypeMap.put("stw", "application/vnd.sun.xml.writer.template");
		extMimeTypeMap.put("sub", "image/vnd.dvb.subtitle");
		extMimeTypeMap.put("sus", "application/vnd.sus-calendar");
		extMimeTypeMap.put("sv4cpio", "application/x-sv4cpio");
		extMimeTypeMap.put("sv4crc", "application/x-sv4crc");
		extMimeTypeMap.put("svc", "application/vnd.dvb.service");
		extMimeTypeMap.put("svd", "application/vnd.svd");
		extMimeTypeMap.put("svg", "image/svg+xml");
		extMimeTypeMap.put("swf", "application/x-shockwave-flash");
		extMimeTypeMap.put("swi", "application/vnd.aristanetworks.swi");
		extMimeTypeMap.put("sxc", "application/vnd.sun.xml.calc");
		extMimeTypeMap.put("sxd", "application/vnd.sun.xml.draw");
		extMimeTypeMap.put("sxg", "application/vnd.sun.xml.writer.global");
		extMimeTypeMap.put("sxi", "application/vnd.sun.xml.impress");
		extMimeTypeMap.put("sxm", "application/vnd.sun.xml.math");
		extMimeTypeMap.put("sxw", "application/vnd.sun.xml.writer");
		//extMimeTypeMap.put("t", "application/x-troff");
		extMimeTypeMap.put("t", "text/troff");
		extMimeTypeMap.put("tao", "application/vnd.tao.intent-module-archive");
		extMimeTypeMap.put("tar", "application/x-tar");
		extMimeTypeMap.put("tcap", "application/vnd.3gpp2.tcap");
		extMimeTypeMap.put("tcl", "application/x-tcl");
		extMimeTypeMap.put("teacher", "application/vnd.smart.teacher");
		extMimeTypeMap.put("tei", "application/tei+xml");
		extMimeTypeMap.put("tex", "application/x-tex");
		extMimeTypeMap.put("texi", "application/x-texinfo");
		extMimeTypeMap.put("texinfo", "application/x-texinfo");
		extMimeTypeMap.put("tfi", "application/thraud+xml");
		extMimeTypeMap.put("tfm", "application/x-tex-tfm");
		extMimeTypeMap.put("tgz", "application/x-compressed");
		extMimeTypeMap.put("thmx", "application/vnd.ms-officetheme");
		extMimeTypeMap.put("tif", "image/tiff");
		extMimeTypeMap.put("tiff", "image/tiff");
		extMimeTypeMap.put("tmo", "application/vnd.tmobile-livetv");
		extMimeTypeMap.put("torrent", "application/x-bittorrent");
		extMimeTypeMap.put("tpl", "application/vnd.groove-tool-template");
		extMimeTypeMap.put("tpt", "application/vnd.trid.tpt");
		extMimeTypeMap.put("tr", "application/x-troff");
		extMimeTypeMap.put("tra", "application/vnd.trueapp");
		extMimeTypeMap.put("trm", "application/x-msterminal");
		extMimeTypeMap.put("tsd", "application/timestamped-data");
		extMimeTypeMap.put("tsv", "text/tab-separated-values");
		extMimeTypeMap.put("ttf", "application/x-font-ttf");
		extMimeTypeMap.put("ttl", "text/turtle");
		extMimeTypeMap.put("twd", "application/vnd.simtech-mindmapper");
		extMimeTypeMap.put("txd", "application/vnd.genomatix.tuxedo");
		extMimeTypeMap.put("txf", "application/vnd.mobius.txf");
		extMimeTypeMap.put("txt", "text/plain");
		extMimeTypeMap.put("ufd", "application/vnd.ufdl");
		extMimeTypeMap.put("uls", "text/iuls");
		extMimeTypeMap.put("umj", "application/vnd.umajin");
		extMimeTypeMap.put("unityweb", "application/vnd.unity");
		extMimeTypeMap.put("uoml", "application/vnd.uoml+xml");
		extMimeTypeMap.put("uri", "text/uri-list");
		extMimeTypeMap.put("ustar", "application/x-ustar");
		extMimeTypeMap.put("utz", "application/vnd.uiq.theme");
		extMimeTypeMap.put("uu", "text/x-uuencode");
		extMimeTypeMap.put("uva", "audio/vnd.dece.audio");
		extMimeTypeMap.put("uvh", "video/vnd.dece.hd");
		extMimeTypeMap.put("uvi", "image/vnd.dece.graphic");
		extMimeTypeMap.put("uvm", "video/vnd.dece.mobile");
		extMimeTypeMap.put("uvp", "video/vnd.dece.pd");
		extMimeTypeMap.put("uvs", "video/vnd.dece.sd");
		extMimeTypeMap.put("uvu", "video/vnd.uvvu.mp4");
		extMimeTypeMap.put("uvv", "video/vnd.dece.video");
		extMimeTypeMap.put("vcd", "application/x-cdlink");
		extMimeTypeMap.put("vcf", "text/x-vcard");
		extMimeTypeMap.put("vcg", "application/vnd.groove-vcard");
		extMimeTypeMap.put("vcs", "text/x-vcalendar");
		extMimeTypeMap.put("vcx", "application/vnd.vcx");
		extMimeTypeMap.put("vis", "application/vnd.visionary");
		extMimeTypeMap.put("viv", "video/vnd.vivo");
		extMimeTypeMap.put("vrml", "x-world/x-vrml");
		extMimeTypeMap.put("vsd", "application/vnd.visio");
		extMimeTypeMap.put("vsdx", "application/vnd.visio2013");
		extMimeTypeMap.put("vsf", "application/vnd.vsf");
		extMimeTypeMap.put("vtu", "model/vnd.vtu");
		extMimeTypeMap.put("vxml", "application/voicexml+xml");
		extMimeTypeMap.put("wad", "application/x-doom");
		extMimeTypeMap.put("wav", "audio/x-wav");
		extMimeTypeMap.put("wax", "audio/x-ms-wax");
		extMimeTypeMap.put("wbmp", "image/vnd.wap.wbmp");
		extMimeTypeMap.put("wbs", "application/vnd.criticaltools.wbs+xml");
		extMimeTypeMap.put("wbxml", "application/vnd.wap.wbxml");
		extMimeTypeMap.put("wcm", "application/vnd.ms-works");
		extMimeTypeMap.put("wdb", "application/vnd.ms-works");
		extMimeTypeMap.put("weba", "audio/webm");
		extMimeTypeMap.put("webm", "video/webm");
		extMimeTypeMap.put("webp", "image/webp");
		extMimeTypeMap.put("wg", "application/vnd.pmi.widget");
		extMimeTypeMap.put("wgt", "application/widget");
		extMimeTypeMap.put("wks", "application/vnd.ms-works");
		extMimeTypeMap.put("wm", "video/x-ms-wm");
		extMimeTypeMap.put("wma", "audio/x-ms-wma");
		extMimeTypeMap.put("wmd", "application/x-ms-wmd");
		extMimeTypeMap.put("wmf", "application/x-msmetafile");
		extMimeTypeMap.put("wml", "text/vnd.wap.wml");
		extMimeTypeMap.put("wmlc", "application/vnd.wap.wmlc");
		extMimeTypeMap.put("wmls", "text/vnd.wap.wmlscript");
		extMimeTypeMap.put("wmlsc", "application/vnd.wap.wmlscriptc");
		extMimeTypeMap.put("wmv", "video/x-ms-wmv");
		extMimeTypeMap.put("wmx", "video/x-ms-wmx");
		extMimeTypeMap.put("wmz", "application/x-ms-wmz");
		extMimeTypeMap.put("woff", "application/x-font-woff");
		extMimeTypeMap.put("wpd", "application/vnd.wordperfect");
		extMimeTypeMap.put("wpl", "application/vnd.ms-wpl");
		extMimeTypeMap.put("wps", "application/vnd.ms-works");
		extMimeTypeMap.put("wqd", "application/vnd.wqd");
		extMimeTypeMap.put("wri", "application/x-mswrite");
		extMimeTypeMap.put("wrl", "x-world/x-vrml");
		extMimeTypeMap.put("wrl", "model/vrml");
		extMimeTypeMap.put("wrz", "x-world/x-vrml");
		extMimeTypeMap.put("wsdl", "application/wsdl+xml");
		extMimeTypeMap.put("wspolicy", "application/wspolicy+xml");
		extMimeTypeMap.put("wtb", "application/vnd.webturbo");
		extMimeTypeMap.put("wvx", "video/x-ms-wvx");
		extMimeTypeMap.put("x3d", "application/vnd.hzn-3d-crossword");
		extMimeTypeMap.put("xaf", "x-world/x-vrml");
		extMimeTypeMap.put("xap", "application/x-silverlight-app");
		extMimeTypeMap.put("xar", "application/vnd.xara");
		extMimeTypeMap.put("xbap", "application/x-ms-xbap");
		extMimeTypeMap.put("xbd", "application/vnd.fujixerox.docuworks.binder");
		extMimeTypeMap.put("xbm", "image/x-xbitmap");
		extMimeTypeMap.put("xdf", "application/xcap-diff+xml");
		extMimeTypeMap.put("xdm", "application/vnd.syncml.dm+xml");
		extMimeTypeMap.put("xdp", "application/vnd.adobe.xdp+xml");
		extMimeTypeMap.put("xdssc", "application/dssc+xml");
		extMimeTypeMap.put("xdw", "application/vnd.fujixerox.docuworks");
		extMimeTypeMap.put("xenc", "application/xenc+xml");
		extMimeTypeMap.put("xer", "application/patch-ops-error+xml");
		extMimeTypeMap.put("xfdf", "application/vnd.adobe.xfdf");
		extMimeTypeMap.put("xfdl", "application/vnd.xfdl");
		extMimeTypeMap.put("xhtml", "application/xhtml+xml");
		extMimeTypeMap.put("xif", "image/vnd.xiff");
		extMimeTypeMap.put("xla", "application/vnd.ms-excel");
		extMimeTypeMap.put("xlam", "application/vnd.ms-excel.addin.macroenabled.12");
		extMimeTypeMap.put("xlc", "application/vnd.ms-excel");
		extMimeTypeMap.put("xlm", "application/vnd.ms-excel");
		extMimeTypeMap.put("xls", "application/vnd.ms-excel");
		extMimeTypeMap.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroenabled.12");
		extMimeTypeMap.put("xlsm", "application/vnd.ms-excel.sheet.macroenabled.12");
		extMimeTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		extMimeTypeMap.put("xlt", "application/vnd.ms-excel");
		extMimeTypeMap.put("xltm", "application/vnd.ms-excel.template.macroenabled.12");
		extMimeTypeMap.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		extMimeTypeMap.put("xlw", "application/vnd.ms-excel");
		extMimeTypeMap.put("xml", "application/xml");
		extMimeTypeMap.put("xo", "application/vnd.olpc-sugar");
		extMimeTypeMap.put("xof", "x-world/x-vrml");
		extMimeTypeMap.put("xop", "application/xop+xml");
		extMimeTypeMap.put("xpi", "application/x-xpinstall");
		extMimeTypeMap.put("xpm", "image/x-xpixmap");
		extMimeTypeMap.put("xpr", "application/vnd.is-xpr");
		extMimeTypeMap.put("xps", "application/vnd.ms-xpsdocument");
		extMimeTypeMap.put("xpw", "application/vnd.intercon.formnet");
		extMimeTypeMap.put("xslt", "application/xslt+xml");
		extMimeTypeMap.put("xsm", "application/vnd.syncml+xml");
		extMimeTypeMap.put("xspf", "application/xspf+xml");
		extMimeTypeMap.put("xul", "application/vnd.mozilla.xul+xml");
		extMimeTypeMap.put("xwd", "image/x-xwindowdump");
		extMimeTypeMap.put("xyz", "chemical/x-xyz");
		extMimeTypeMap.put("yaml", "text/yaml");
		extMimeTypeMap.put("yang", "application/yang");
		extMimeTypeMap.put("yin", "application/yin+xml");
		extMimeTypeMap.put("z", "application/x-compress");
		extMimeTypeMap.put("zaz", "application/vnd.zzazz.deck+xml");
		extMimeTypeMap.put("zip", "application/zip");
		extMimeTypeMap.put("zir", "application/vnd.zul");
		extMimeTypeMap.put("zmm", "application/vnd.handheld-entertainment+xml");

	}
   
    /** 
     * 得到文件头 
     *  
     * @param file 文件
     * @return 文件头 
     * @throws IOException 
     */  
    private static String getFileHeadContent(File file) throws IOException {

        try(InputStream inputStream = new FileInputStream(file)) {
        	byte[] b = new byte[28];
            inputStream.read(b, 0, 28);
            return String.valueOf(Hex.encodeHex(b,false));  
        }
    }  
      
    /** 
     * 判断文件类型 
     *  
     * @param file 文件
     * @return 文件类型 
     * @throws IOException 异常
     */  
    public static String getFileType(File file) throws IOException {
    	String fileHead = getFileHeadContent(file);  
        
        if (fileHead == null || fileHead.length() == 0) {  
            return null;  
        }  
          
        fileHead = fileHead.toUpperCase();  
        for(Entry<String,String> entry : mFileTypes.entrySet()){
        	if(fileHead.startsWith(entry.getKey())){
        		if("office2003".equals(entry.getValue()) || "officeX".equals(entry.getValue())){
        			if( isOfficeFileByExtName(file.getName())){
        				return StringUtils.lowerCase(getFileExtName(file.getName())); 
        			}
        		}
        		return entry.getValue();
        	}
        }  
        //System.out.println("\n空");
        return "unknown"; 
    }
    
    public static String getFileType(String fileName) throws IOException {
    	return getFileType(new File(fileName));
    }
    
    /**
     * 获取文件的名称 ，去掉后缀名
     * @param fileName 文件名
     * @return  文件名
     */
    public static String truncateFileExtName(String fileName){
		if (fileName == null || fileName.isEmpty())
			return "";
		int firstIndex = fileName.lastIndexOf("/");
		
		int	firstIndex2 = fileName.lastIndexOf("\\");
		if(firstIndex<firstIndex2)
			firstIndex = firstIndex2;
		if(firstIndex<0)
			firstIndex = 0;
		else
			firstIndex = firstIndex+1;
		
		int lastIndex = fileName.lastIndexOf(".");
		if(lastIndex<0)
			return fileName;
		
		return fileName.substring(firstIndex,lastIndex);
	}
    /**
     * 获取文件的后缀名
     * @param fileName 文件名
     * @return 后缀名
     */
    public static String getFileExtName(String fileName){
		if (fileName == null || fileName.isEmpty())
			return "";
		int lastIndex = fileName.lastIndexOf(".");
		if(lastIndex<0)
			return "";
		return fileName.substring(lastIndex + 1, fileName.length());
	}

  
	/*
	 * 判断输入的文件是够是office的文件
	 */
	public static boolean isOfficeFileByExtName(String fileName){
		String suffix =StringUtils.lowerCase(getFileExtName(fileName)); 
		if(StringUtils.isBlank(suffix))
			return false;
		
		if (suffix.equalsIgnoreCase("doc") || suffix.equalsIgnoreCase("docx")) {
			return true;
		} else if (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx")) {
			return true;
		} else if (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")) {
			return true;
		} else if (suffix.equalsIgnoreCase("vsd") || suffix.equalsIgnoreCase("vsdx")) {
			return true;
		} else if (suffix.equalsIgnoreCase("pub") || suffix.equalsIgnoreCase("pubx")) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * 判断输入的文件是够是office的文件
	 */
	public static boolean isOffice2003FileByExtName(String fileName){
		
		String suffix =StringUtils.lowerCase(getFileExtName(fileName)); 

		if(StringUtils.isBlank(suffix))
			return false;
		
		if (suffix.equalsIgnoreCase("doc")) {
			return true;
		} else if (suffix.equalsIgnoreCase("ppt")) {
			return true;
		} else if (suffix.equalsIgnoreCase("xls")) {
			return true;
		} else if (suffix.equalsIgnoreCase("vsd")) {
			return true;
		} else if (suffix.equalsIgnoreCase("pubx")) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * 判断输入的文件是够是office的文件
	 */
	public static boolean isOfficeFile(File file){
		try {
			String fileHead = getFileHeadContent(file);
			if(fileHead.startsWith(mFileTypes.get("office2003"))) 
				return true;
			if(fileHead.startsWith(mFileTypes.get("officeX"))) 
				return true;
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	public static boolean isOfficeFile(String fileName){
    	return isOfficeFile(new File(fileName));
    }
	
	public static boolean isOffice2003File(File file){
		try {
			String fileHead = getFileHeadContent(file);
			if(fileHead.startsWith(mFileTypes.get("office2003"))) 
				return true;
			
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	public static boolean isOffice2003File(String fileName){
    	return isOffice2003File(new File(fileName));
    }
	
	public static String mapExtNameToMimeType(String extName){    
      	String mimeType = extMimeTypeMap.get(StringUtils.lowerCase(extName));
      	if(mimeType!=null)
      		return mimeType;
      	return "application/octet-stream";
    }

	public static String getFileMimeType(String fileUrl){    
		return mapExtNameToMimeType(getFileExtName(fileUrl));
    }
}
