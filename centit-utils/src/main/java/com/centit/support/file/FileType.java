package com.centit.support.file;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author 朱晓文 杨淮生 codefan@sina.com
 */
@SuppressWarnings("unused")
public abstract class FileType {

    public static final String OFFICE2003_FILE_HEAD = "D0CF11E0";
    public static final String OFFICE_XML_FILE_HEAD = "504B0304";
    protected static final Logger logger = LoggerFactory.getLogger(FileIOOpt.class);
    protected static final HashMap<String, String> mFileTypes =
        new HashMap<String, String>(50) {{
            // images
            put("FFD8FF", "jpg");
            put("89504E47", "png");
            put("47494638", "gif");
            put("49492A00", "tif");
            put("424D", "bmp");
            //
            put("41433130", "dwg"); // CAD
            put("38425053", "psd");
            put("7B5C727466", "rtf"); // 日记本
            put("3C3F786D6C", "xml");
            put("68746D6C3E", "html");
            put("44656C69766572792D646174653A", "eml"); // 邮件
            put(OFFICE2003_FILE_HEAD, "office2003");
            put("5374616E64617264204A", "mdb");
            put("252150532D41646F6265", "ps");
            put("255044462D312E", "pdf");
            put(OFFICE_XML_FILE_HEAD, "officeX");
            put("52617221", "rar");
            put("57415645", "wav");
            put("41564920", "avi");
            put("2E524D46", "rm");
            put("000001BA", "mpg");
            put("000001B3", "mpg");
            put("6D6F6F76", "mov");
            put("3026B2758E66CF11", "asf");
            put("4D546864", "mid");
            put("1F8B08", "gz");
            put("6D6F7669","csv");
            put("7B226461","json");

        }};
    protected static final HashMap<String, String> extMimeTypeMap
        = new HashMap<String, String>(1200) {{
        put("123", "application/vnd.lotus-1-2-3");
        put("323", "text/h323");
        put("3dml", "text/vnd.in3d.3dml");
        put("3g2", "video/3gpp2");
        put("3gp", "video/3gpp");
        put("7z", "application/x-7z-compressed");
        put("aab", "application/x-authorware-bin");
        put("aac", "audio/x-aac");
        put("aam", "application/x-authorware-map");
        put("aas", "application/x-authorware-seg");
        put("abw", "application/x-abiword");
        put("ac", "application/pkix-attr-cert");
        put("acc", "application/vnd.americandynamics.acc");
        put("ace", "application/x-ace-compressed");
        put("acu", "application/vnd.acucobol");
        put("acx", "application/internet-property-stream");
        put("adp", "audio/adpcm");
        put("aep", "application/vnd.audiograph");
        put("afp", "application/vnd.ibm.modcap");
        put("ahead", "application/vnd.ahead.space");
        put("ai", "application/postscript");
        put("aif", "audio/x-aiff");
        put("aifc", "audio/x-aiff");
        put("aiff", "audio/x-aiff");
        put("air", "application/vnd.adobe.air-application-installer-package+zip");
        put("ait", "application/vnd.dvb.ait");
        put("ami", "application/vnd.amiga.ami");
        put("apk", "application/vnd.android.package-archive");
        put("application", "application/x-ms-application");
        put("apr", "application/vnd.lotus-approach");
        put("asf", "video/x-ms-asf");
        put("aso", "application/vnd.accpac.simply.aso");
        put("asr", "video/x-ms-asf");
        put("asx", "video/x-ms-asf");
        put("atc", "application/vnd.acucorp");
        put("atom", "application/atom+xml");
        put("atomcat", "application/atomcat+xml");
        put("atomsvc", "application/atomsvc+xml");
        put("atx", "application/vnd.antix.game-component");
        put("au", "audio/basic");
        put("avi", "video/x-msvideo");
        put("aw", "application/applixware");
        put("axs", "application/olescript");
        put("azf", "application/vnd.airzip.filesecure.azf");
        put("azs", "application/vnd.airzip.filesecure.azs");
        put("azw", "application/vnd.amazon.ebook");
        put("bas", "text/plain");
        put("bcpio", "application/x-bcpio");
        put("bdf", "application/x-font-bdf");
        put("bdm", "application/vnd.syncml.dm+wbxml");
        put("bed", "application/vnd.realvnc.bed");
        put("bh2", "application/vnd.fujitsu.oasysprs");
        put("bin", "application/octet-stream");
        put("bmi", "application/vnd.bmi");
        put("bmp", "image/bmp");
        put("box", "application/vnd.previewsystems.box");
        put("btif", "image/prs.btif");
        put("bz", "application/x-bzip");
        put("bz2", "application/x-bzip2");
        put("c", "text/plain");
        put("c11amc", "application/vnd.cluetrust.cartomobile-config");
        put("c11amz", "application/vnd.cluetrust.cartomobile-config-pkg");
        put("c4g", "application/vnd.clonk.c4group");
        put("cab", "application/vnd.ms-cab-compressed");
        put("car", "application/vnd.curl.car");
        put("cat", "application/vnd.ms-pkiseccat");
        put("ccxml", "application/ccxml+xml,");
        put("cdbcmsg", "application/vnd.contact.cmsg");
        put("cdf", "application/x-cdf");
        put("cdkey", "application/vnd.mediastation.cdkey");
        put("cdmia", "application/cdmi-capability");
        put("cdmic", "application/cdmi-container");
        put("cdmid", "application/cdmi-domain");
        put("cdmio", "application/cdmi-object");
        put("cdmiq", "application/cdmi-queue");
        put("cdx", "chemical/x-cdx");
        put("cdxml", "application/vnd.chemdraw+xml");
        put("cdy", "application/vnd.cinderella");
        put("cer", "application/x-x509-ca-cert");
        put("cgm", "image/cgm");
        put("chat", "application/x-chat");
        put("chm", "application/vnd.ms-htmlhelp");
        put("chrt", "application/vnd.kde.kchart");
        put("cif", "chemical/x-cif");
        put("cii", "application/vnd.anser-web-certificate-issue-initiation");
        put("cil", "application/vnd.ms-artgalry");
        put("cla", "application/vnd.claymore");
        put("class", "application/java-vm");
        put("clkk", "application/vnd.crick.clicker.keyboard");
        put("clkp", "application/vnd.crick.clicker.palette");
        put("clkt", "application/vnd.crick.clicker.template");
        put("clkw", "application/vnd.crick.clicker.wordbank");
        put("clkx", "application/vnd.crick.clicker");
        put("clp", "application/x-msclip");
        put("cmc", "application/vnd.cosmocaller");
        put("cmdf", "chemical/x-cmdf");
        put("cml", "chemical/x-cml");
        put("cmp", "application/vnd.yellowriver-custom-menu");
        put("cmx", "image/x-cmx");
        put("cod", "image/cis-cod");
        //put("cod", "application/vnd.rim.cod");
        put("cpio", "application/x-cpio");
        put("cpt", "application/mac-compactpro");
        put("crd", "application/x-mscardfile");
        put("crl", "application/pkix-crl");
        put("crt", "application/x-x509-ca-cert");
        put("cryptonote", "application/vnd.rig.cryptonote");
        put("csh", "application/x-csh");
        put("csml", "chemical/x-csml");
        put("csp", "application/vnd.commonspace");
        put("css", "text/css");
        put("csv", "text/csv");
        put("cu", "application/cu-seeme");
        put("curl", "text/vnd.curl");
        put("cww", "application/prs.cww");
        put("dae", "model/vnd.collada+xml");
        put("daf", "application/vnd.mobius.daf");
        put("davmount", "application/davmount+xml");
        put("dcr", "application/x-director");
        put("dcurl", "text/vnd.curl.dcurl");
        put("dd2", "application/vnd.oma.dd2+xml");
        put("ddd", "application/vnd.fujixerox.ddd");
        put("deb", "application/x-debian-package");
        put("der", "application/x-x509-ca-cert");
        put("dfac", "application/vnd.dreamfactory");
        put("dir", "application/x-director");
        put("dis", "application/vnd.mobius.dis");
        put("djvu", "image/vnd.djvu");
        put("dll", "application/x-msdownload");
        put("dms", "application/octet-stream");
        put("dna", "application/vnd.dna");
        put("doc", "application/msword");
        put("docm", "application/vnd.ms-word.document.macroenabled.12");
        put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        put("dot", "application/msword");
        put("dotm", "application/vnd.ms-word.template.macroenabled.12");
        put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        put("dp", "application/vnd.osgi.dp");
        put("dpg", "application/vnd.dpgraph");
        put("dra", "audio/vnd.dra");
        put("dsc", "text/prs.lines.tag");
        put("dssc", "application/dssc+der");
        put("dtb", "application/x-dtbook+xml");
        put("dtd", "application/xml-dtd");
        put("dts", "audio/vnd.dts");
        put("dtshd", "audio/vnd.dts.hd");
        put("dvi", "application/x-dvi");
        put("dwf", "model/vnd.dwf");
        put("dwg", "image/vnd.dwg");
        put("dxf", "image/vnd.dxf");
        put("dxp", "application/vnd.spotfire.dxp");
        put("dxr", "application/x-director");
        put("ecelp4800", "audio/vnd.nuera.ecelp4800");
        put("ecelp7470", "audio/vnd.nuera.ecelp7470");
        put("ecelp9600", "audio/vnd.nuera.ecelp9600");
        put("edm", "application/vnd.novadigm.edm");
        put("edx", "application/vnd.novadigm.edx");
        put("efif", "application/vnd.picsel");
        put("ei6", "application/vnd.pg.osasli");
        put("eml", "message/rfc822");
        put("emma", "application/emma+xml");
        put("eol", "audio/vnd.digital-winds");
        put("eot", "application/vnd.ms-fontobject");
        put("eps", "application/postscript");
        put("epub", "application/epub+zip");
        put("es", "application/ecmascript");
        put("es3", "application/vnd.eszigno3+xml");
        put("esf", "application/vnd.epson.esf");
        put("etx", "text/x-setext");
        put("evy", "application/envoy");
        //put("exe", "application/octet-stream");
        put("exe", "application/x-msdownload");
        put("exi", "application/exi");
        put("ext", "application/vnd.novadigm.ext");
        put("ez2", "application/vnd.ezpix-album");
        put("ez3", "application/vnd.ezpix-package");
        put("f", "text/x-fortran");
        put("f4v", "video/x-f4v");
        put("fbs", "image/vnd.fastbidsheet");
        put("fcs", "application/vnd.isac.fcs");
        put("fdf", "application/vnd.fdf");
        put("fe_launch", "application/vnd.denovo.fcselayout-link");
        put("fg5", "application/vnd.fujitsu.oasysgp");
        put("fh", "image/x-freehand");
        put("fif", "application/fractals");
        put("fig", "application/x-xfig");
        put("fli", "video/x-fli");
        put("flo", "application/vnd.micrografx.flo");
        put("flr", "x-world/x-vrml");
        put("flv", "video/x-flv");
        put("flw", "application/vnd.kde.kivio");
        put("flx", "text/vnd.fmi.flexstor");
        put("fly", "text/vnd.fly");
        put("fm", "application/vnd.framemaker");
        put("fnc", "application/vnd.frogans.fnc");
        put("fpx", "image/vnd.fpx");
        put("fsc", "application/vnd.fsc.weblaunch");
        put("fst", "image/vnd.fst");
        put("ftc", "application/vnd.fluxtime.clip");
        put("fti", "application/vnd.anser-web-funds-transfer-initiation");
        put("fvt", "video/vnd.fvt");
        put("fxp", "application/vnd.adobe.fxp");
        put("fzs", "application/vnd.fuzzysheet");
        put("g2w", "application/vnd.geoplan");
        put("g3", "image/g3fax");
        put("g3w", "application/vnd.geospace");
        put("gac", "application/vnd.groove-account");
        put("gdl", "model/vnd.gdl");
        put("geo", "application/vnd.dynageo");
        put("gex", "application/vnd.geometry-explorer");
        put("ggb", "application/vnd.geogebra.file");
        put("ggt", "application/vnd.geogebra.tool");
        put("ghf", "application/vnd.groove-help");
        put("gif", "image/gif");
        put("gim", "application/vnd.groove-identity-message");
        put("gmx", "application/vnd.gmx");
        put("gnumeric", "application/x-gnumeric");
        put("gph", "application/vnd.flographit");
        put("gqf", "application/vnd.grafeq");
        put("gram", "application/srgs");
        put("grv", "application/vnd.groove-injector");
        put("grxml", "application/srgs+xml");
        put("gsf", "application/x-font-ghostscript");
        put("gtar", "application/x-gtar");
        put("gtm", "application/vnd.groove-tool-message");
        put("gtw", "model/vnd.gtw");
        put("gv", "text/vnd.graphviz");
        put("gxt", "application/vnd.geonext");
        put("gz", "application/x-gzip");
        put("h", "text/plain");
        put("h261", "video/h261");
        put("h263", "video/h263");
        put("h264", "video/h264");
        put("hal", "application/vnd.hal+xml");
        put("hbci", "application/vnd.hbci");
        put("hdf", "application/x-hdf");
        put("hlp", "application/winhlp");
        put("hpgl", "application/vnd.hp-hpgl");
        put("hpid", "application/vnd.hp-hpid");
        put("hps", "application/vnd.hp-hps");
        put("hqx", "application/mac-binhex40");
        put("hta", "application/hta");
        put("htc", "text/x-component");
        put("htke", "application/vnd.kenameaapp");
        put("htm", "text/html");
        put("html", "text/html");
        put("htt", "text/webviewhtml");
        put("hvd", "application/vnd.yamaha.hv-dic");
        put("hvp", "application/vnd.yamaha.hv-voice");
        put("hvs", "application/vnd.yamaha.hv-script");
        put("i2g", "application/vnd.intergeo");
        put("icc", "application/vnd.iccprofile");
        put("ice", "x-conference/x-cooltalk");
        put("ico", "image/x-icon");
        put("ics", "text/calendar");
        put("ief", "image/ief");
        put("ifm", "application/vnd.shana.informed.formdata");
        put("igl", "application/vnd.igloader");
        put("igm", "application/vnd.insors.igm");
        put("igs", "model/iges");
        put("igx", "application/vnd.micrografx.igx");
        put("iif", "application/vnd.shana.informed.interchange");
        put("iii", "application/x-iphone");
        put("imp", "application/vnd.accpac.simply.imp");
        put("ims", "application/vnd.ms-ims");
        put("ins", "application/x-internet-signup");
        put("ipfix", "application/ipfix");
        put("ipk", "application/vnd.shana.informed.package");
        put("irm", "application/vnd.ibm.rights-management");
        put("irp", "application/vnd.irepository.package+xml");
        put("isp", "application/x-internet-signup");
        put("itp", "application/vnd.shana.informed.formtemplate");
        put("ivp", "application/vnd.immervision-ivp");
        put("ivu", "application/vnd.immervision-ivu");
        put("jad", "text/vnd.sun.j2me.app-descriptor");
        put("jam", "application/vnd.jam");
        put("jar", "application/java-archive");
        put("java", "text/x-java-source,java");
        put("jfif", "image/pipeg");
        put("jisp", "application/vnd.jisp");
        put("jlt", "application/vnd.hp-jlyt");
        put("jnlp", "application/x-java-jnlp-file");
        put("joda", "application/vnd.joost.joda-archive");
        put("jpe", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("jpg", "image/jpeg");
        put("jpgv", "video/jpeg");
        put("jpm", "video/jpm");
        //put("js", "application/x-javascript");
        put("js", "application/javascript");
        put("json", "application/json");
        put("karbon", "application/vnd.kde.karbon");
        put("kfo", "application/vnd.kde.kformula");
        put("kia", "application/vnd.kidspiration");
        put("kml", "application/vnd.google-earth.kml+xml");
        put("kmz", "application/vnd.google-earth.kmz");
        put("kne", "application/vnd.kinar");
        put("kon", "application/vnd.kde.kontour");
        put("kpr", "application/vnd.kde.kpresenter");
        put("ksp", "application/vnd.kde.kspread");
        put("ktx", "image/ktx");
        put("ktz", "application/vnd.kahootz");
        put("kwd", "application/vnd.kde.kword");
        put("lasxml", "application/vnd.las.las+xml");
        put("latex", "application/x-latex");
        put("lbd", "application/vnd.llamagraphics.life-balance.desktop");
        put("lbe", "application/vnd.llamagraphics.life-balance.exchange+xml");
        put("les", "application/vnd.hhe.lesson-player");
        put("lha", "application/octet-stream");
        put("link66", "application/vnd.route66.link66+xml");
        put("lrm", "application/vnd.ms-lrm");
        put("lsf", "video/x-la-asf");
        put("lsx", "video/x-la-asf");
        put("ltf", "application/vnd.frogans.ltf");
        put("lvp", "audio/vnd.lucent.voice");
        put("lwp", "application/vnd.lotus-wordpro");
        put("lzh", "application/octet-stream");
        put("m13", "application/x-msmediaview");
        put("m14", "application/x-msmediaview");
        put("m21", "application/mp21");
        put("m3u", "audio/x-mpegurl");
        put("m3u8", "application/vnd.apple.mpegurl");
        put("m4v", "video/x-m4v");
        put("ma", "application/mathematica");
        put("mads", "application/mads+xml");
        put("mag", "application/vnd.ecowin.chart");
        put("man", "application/x-troff-man");
        put("mathml", "application/mathml+xml");
        put("mbk", "application/vnd.mobius.mbk");
        put("mbox", "application/mbox");
        put("mc1", "application/vnd.medcalcdata");
        put("mcd", "application/vnd.mcd");
        put("mcurl", "text/vnd.curl.mcurl");
        put("mdb", "application/x-msaccess");
        put("mdi", "image/vnd.ms-modi");
        put("me", "application/x-troff-me");
        put("meta4", "application/metalink4+xml");
        put("mets", "application/mets+xml");
        put("mfm", "application/vnd.mfmp");
        put("mgp", "application/vnd.osgeo.mapguide.package");
        put("mgz", "application/vnd.proteus.magazine");
        put("mht", "message/rfc822");
        put("mhtml", "message/rfc822");
        put("mid", "audio/mid");
        //put("mid", "audio/midi");
        put("mif", "application/vnd.mif");
        put("mj2", "video/mj2");
        put("mlp", "application/vnd.dolby.mlp");
        put("mmd", "application/vnd.chipnuts.karaoke-mmd");
        put("mmf", "application/vnd.smaf");
        put("mmr", "image/vnd.fujixerox.edmics-mmr");
        put("mny", "application/x-msmoney");
        put("mods", "application/mods+xml");
        put("mov", "video/quicktime");
        put("movie", "video/x-sgi-movie");
        put("mp2", "video/mpeg");
        put("mp3", "audio/mpeg");
        put("mp4", "video/mp4");
        //put("mp4", "application/mp4");
        put("mp4a", "audio/mp4");
        put("mpa", "video/mpeg");
        put("mpc", "application/vnd.mophun.certificate");
        put("mpe", "video/mpeg");
        put("mpeg", "video/mpeg");
        put("mpg", "video/mpeg");
        put("mpga", "audio/mpeg");
        put("mpkg", "application/vnd.apple.installer+xml");
        put("mpm", "application/vnd.blueice.multipass");
        put("mpn", "application/vnd.mophun.application");
        put("mpp", "application/vnd.ms-project");
        put("mpv2", "video/mpeg");
        put("mpy", "application/vnd.ibm.minipay");
        put("mqy", "application/vnd.mobius.mqy");
        put("mrc", "application/marc");
        put("mrcx", "application/marcxml+xml");
        put("ms", "application/x-troff-ms");
        put("mscml", "application/mediaservercontrol+xml");
        put("mseq", "application/vnd.mseq");
        put("msf", "application/vnd.epson.msf");
        put("msh", "model/mesh");
        put("msl", "application/vnd.mobius.msl");
        put("msty", "application/vnd.muvee.style");
        put("mts", "model/vnd.mts");
        put("mus", "application/vnd.musician");
        put("musicxml", "application/vnd.recordare.musicxml+xml");
        put("mvb", "application/x-msmediaview");
        put("mwf", "application/vnd.mfer");
        put("mxf", "application/mxf");
        put("mxl", "application/vnd.recordare.musicxml");
        put("mxml", "application/xv+xml");
        put("mxs", "application/vnd.triscape.mxs");
        put("mxu", "video/vnd.mpegurl");
        put("n3", "text/n3");
        put("nbp", "application/vnd.wolfram.player");
        put("nc", "application/x-netcdf");
        put("ncx", "application/x-dtbncx+xml");
        put("n-gage", "application/vnd.nokia.n-gage.symbian.install");
        put("ngdat", "application/vnd.nokia.n-gage.data");
        put("nlu", "application/vnd.neurolanguage.nlu");
        put("nml", "application/vnd.enliven");
        put("nnd", "application/vnd.noblenet-directory");
        put("nns", "application/vnd.noblenet-sealer");
        put("nnw", "application/vnd.noblenet-web");
        put("npx", "image/vnd.net-fpx");
        put("nsf", "application/vnd.lotus-notes");
        put("nws", "message/rfc822");
        put("oa2", "application/vnd.fujitsu.oasys2");
        put("oa3", "application/vnd.fujitsu.oasys3");
        put("oas", "application/vnd.fujitsu.oasys");
        put("obd", "application/x-msbinder");
        put("oda", "application/oda");
        put("odb", "application/vnd.oasis.opendocument.database");
        put("odc", "application/vnd.oasis.opendocument.chart");
        put("odf", "application/vnd.oasis.opendocument.formula");
        put("odft", "application/vnd.oasis.opendocument.formula-template");
        put("odg", "application/vnd.oasis.opendocument.graphics");
        put("odi", "application/vnd.oasis.opendocument.image");
        put("odm", "application/vnd.oasis.opendocument.text-master");
        put("odp", "application/vnd.oasis.opendocument.presentation");
        put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        put("odt", "application/vnd.oasis.opendocument.text");
        put("oga", "audio/ogg");
        put("ogv", "video/ogg");
        put("ogx", "application/ogg");
        put("onetoc", "application/onenote");
        put("opf", "application/oebps-package+xml");
        put("org", "application/vnd.lotus-organizer");
        put("osf", "application/vnd.yamaha.openscoreformat");
        put("osfpvg", "application/vnd.yamaha.openscoreformat.osfpvg+xml");
        put("otc", "application/vnd.oasis.opendocument.chart-template");
        put("otf", "application/x-font-otf");
        put("otg", "application/vnd.oasis.opendocument.graphics-template");
        put("oth", "application/vnd.oasis.opendocument.text-web");
        put("oti", "application/vnd.oasis.opendocument.image-template");
        put("otp", "application/vnd.oasis.opendocument.presentation-template");
        put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        put("ott", "application/vnd.oasis.opendocument.text-template");
        put("oxt", "application/vnd.openofficeorg.extension");
        put("p", "text/x-pascal");
        put("p10", "application/pkcs10");
        put("p12", "application/x-pkcs12");
        put("p7b", "application/x-pkcs7-certificates");
        put("p7c", "application/x-pkcs7-mime");
        put("p7m", "application/x-pkcs7-mime");
        //put("p7m", "application/pkcs7-mime");
        put("p7r", "application/x-pkcs7-certreqresp");
        put("p7s", "application/x-pkcs7-signature");
        //put("p7s", "application/pkcs7-signature");
        put("p8", "application/pkcs8");
        put("par", "text/plain-bas");
        put("paw", "application/vnd.pawaafile");
        put("pbd", "application/vnd.powerbuilder6");
        put("pbm", "image/x-portable-bitmap");
        put("pcf", "application/x-font-pcf");
        put("pcl", "application/vnd.hp-pcl");
        put("pclxl", "application/vnd.hp-pclxl");
        put("pcurl", "application/vnd.curl.pcurl");
        put("pcx", "image/x-pcx");
        put("pdb", "application/vnd.palm");
        put("pdf", "application/pdf");
        put("pfa", "application/x-font-type1");
        put("pfr", "application/font-tdpfr");
        put("pfx", "application/x-pkcs12");
        put("pgm", "image/x-portable-graymap");
        put("pgn", "application/x-chess-pgn");
        put("pgp", "application/pgp-signature");
        //put("pgp", "application/pgp-encrypted");
        put("pic", "image/x-pict");
        put("pjpeg", "image/pjpeg");
        put("pki", "application/pkixcmp");
        put("pkipath", "application/pkix-pkipath");
        put("pko", "application/ynd.ms-pkipko");
        put("plb", "application/vnd.3gpp.pic-bw-large");
        put("plc", "application/vnd.mobius.plc");
        put("plf", "application/vnd.pocketlearn");
        put("pls", "application/pls+xml");
        put("pma", "application/x-perfmon");
        put("pmc", "application/x-perfmon");
        put("pml", "application/x-perfmon");
        //put("pml", "application/vnd.ctc-posml");
        put("pmr", "application/x-perfmon");
        put("pmw", "application/x-perfmon");
        //put("png", "image/x-png");
        //put("png", "image/x-citrix-png");
        put("png", "image/png");
        put("pnm", "image/x-portable-anymap");
        put("portpkg", "application/vnd.macports.portpkg");
        put("pot,", "application/vnd.ms-powerpoint");
        put("potm", "application/vnd.ms-powerpoint.template.macroenabled.12");
        put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        put("ppam", "application/vnd.ms-powerpoint.addin.macroenabled.12");
        put("ppd", "application/vnd.cups-ppd");
        put("ppm", "image/x-portable-pixmap");
        put("pps", "application/vnd.ms-powerpoint");
        put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
        put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        put("ppt", "application/vnd.ms-powerpoint");
        put("pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12");
        put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        put("prc", "application/x-mobipocket-ebook");
        put("pre", "application/vnd.lotus-freelance");
        put("prf", "application/pics-rules");
        put("ps", "application/postscript");
        put("psb", "application/vnd.3gpp.pic-bw-small");
        put("psd", "image/vnd.adobe.photoshop");
        put("psf", "application/x-font-linux-psf");
        put("pskcxml", "application/pskc+xml");
        put("ptid", "application/vnd.pvi.ptid1");
        put("pub", "application/x-mspublisher");
        put("pvb", "application/vnd.3gpp.pic-bw-var");
        put("pwn", "application/vnd.3m.post-it-notes");
        put("pya", "audio/vnd.ms-playready.media.pya");
        put("pyv", "video/vnd.ms-playready.media.pyv");
        put("qam", "application/vnd.epson.quickanime");
        put("qbo", "application/vnd.intu.qbo");
        put("qfx", "application/vnd.intu.qfx");
        put("qps", "application/vnd.publishare-delta-tree");
        put("qt", "video/quicktime");
        put("qxd", "application/vnd.quark.quarkxpress");
        put("ra", "audio/x-pn-realaudio");
        put("ram", "audio/x-pn-realaudio");
        put("rar", "application/x-rar-compressed");
        put("ras", "image/x-cmu-raster");
        put("rcprofile", "application/vnd.ipunplugged.rcprofile");
        put("rdf", "application/rdf+xml");
        put("rdz", "application/vnd.data-vision.rdz");
        put("rep", "application/vnd.businessobjects");
        put("res", "application/x-dtbresource+xml");
        put("rgb", "image/x-rgb");
        put("rif", "application/reginfo+xml");
        put("rip", "audio/vnd.rip");
        put("rl", "application/resource-lists+xml");
        put("rlc", "image/vnd.fujixerox.edmics-rlc");
        put("rld", "application/resource-lists-diff+xml");
        put("rm", "application/vnd.rn-realmedia");
        put("rmi", "audio/mid");
        put("rmp", "audio/x-pn-realaudio-plugin");
        put("rms", "application/vnd.jcp.javame.midlet-rms");
        put("rnc", "application/relax-ng-compact-syntax");
        put("roff", "application/x-troff");
        put("rp9", "application/vnd.cloanto.rp9");
        put("rpss", "application/vnd.nokia.radio-presets");
        put("rpst", "application/vnd.nokia.radio-preset");
        put("rq", "application/sparql-query");
        put("rs", "application/rls-services+xml");
        put("rsd", "application/rsd+xml");
        put("rss", "application/rss+xml");
        put("rtf", "application/rtf");
        put("rtx", "text/richtext");
        put("s", "text/x-asm");
        put("saf", "application/vnd.yamaha.smaf-audio");
        put("sbml", "application/sbml+xml");
        put("sc", "application/vnd.ibm.secure-container");
        put("scd", "application/x-msschedule");
        put("scm", "application/vnd.lotus-screencam");
        put("scq", "application/scvp-cv-request");
        put("scs", "application/scvp-cv-response");
        put("sct", "text/scriptlet");
        put("scurl", "text/vnd.curl.scurl");
        put("sda", "application/vnd.stardivision.draw");
        put("sdc", "application/vnd.stardivision.calc");
        put("sdd", "application/vnd.stardivision.impress");
        put("sdkm", "application/vnd.solent.sdkm+xml");
        put("sdp", "application/sdp");
        put("sdw", "application/vnd.stardivision.writer");
        put("see", "application/vnd.seemail");
        put("seed", "application/vnd.fdsn.seed");
        put("sema", "application/vnd.sema");
        put("semd", "application/vnd.semd");
        put("semf", "application/vnd.semf");
        put("ser", "application/java-serialized-object");
        put("setpay", "application/set-payment-initiation");
        put("setreg", "application/set-registration-initiation");
        put("sfd-hdstx", "application/vnd.hydrostatix.sof-data");
        put("sfs", "application/vnd.spotfire.sfs");
        put("sgl", "application/vnd.stardivision.writer-global");
        put("sgml", "text/sgml");
        put("sh", "application/x-sh");
        put("shar", "application/x-shar");
        put("shf", "application/shf+xml");
        put("sis", "application/vnd.symbian.install");
        put("sit", "application/x-stuffit");
        put("sitx", "application/x-stuffitx");
        put("skp", "application/vnd.koan");
        put("sldm", "application/vnd.ms-powerpoint.slide.macroenabled.12");
        put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        put("slt", "application/vnd.epson.salt");
        put("sm", "application/vnd.stepmania.stepchart");
        put("smf", "application/vnd.stardivision.math");
        put("smi", "application/smil+xml");
        put("snd", "audio/basic");
        put("snf", "application/x-font-snf");
        put("spc", "application/x-pkcs7-certificates");
        put("spf", "application/vnd.yamaha.smaf-phrase");
        put("spl", "application/futuresplash");
        put("spl", "application/x-futuresplash");
        put("spot", "text/vnd.in3d.spot");
        put("spp", "application/scvp-vp-response");
        put("spq", "application/scvp-vp-request");
        put("src", "application/x-wais-source");
        put("sru", "application/sru+xml");
        put("srx", "application/sparql-results+xml");
        put("sse", "application/vnd.kodak-descriptor");
        put("ssf", "application/vnd.epson.ssf");
        put("ssml", "application/ssml+xml");
        put("sst", "application/vnd.ms-pkicertstore");
        put("st", "application/vnd.sailingtracker.track");
        put("stc", "application/vnd.sun.xml.calc.template");
        put("std", "application/vnd.sun.xml.draw.template");
        put("stf", "application/vnd.wt.stf");
        put("sti", "application/vnd.sun.xml.impress.template");
        put("stk", "application/hyperstudio");
        put("stl", "application/vnd.ms-pkistl");
        put("stl", "application/vnd.ms-pki.stl");
        put("stm", "text/html");
        put("str", "application/vnd.pg.format");
        put("stw", "application/vnd.sun.xml.writer.template");
        put("sub", "image/vnd.dvb.subtitle");
        put("sus", "application/vnd.sus-calendar");
        put("sv4cpio", "application/x-sv4cpio");
        put("sv4crc", "application/x-sv4crc");
        put("svc", "application/vnd.dvb.service");
        put("svd", "application/vnd.svd");
        put("svg", "image/svg+xml");
        put("swf", "application/x-shockwave-flash");
        put("swi", "application/vnd.aristanetworks.swi");
        put("sxc", "application/vnd.sun.xml.calc");
        put("sxd", "application/vnd.sun.xml.draw");
        put("sxg", "application/vnd.sun.xml.writer.global");
        put("sxi", "application/vnd.sun.xml.impress");
        put("sxm", "application/vnd.sun.xml.math");
        put("sxw", "application/vnd.sun.xml.writer");
        //put("t", "application/x-troff");
        put("t", "text/troff");
        put("tao", "application/vnd.tao.intent-module-archive");
        put("tar", "application/x-tar");
        put("tcap", "application/vnd.3gpp2.tcap");
        put("tcl", "application/x-tcl");
        put("teacher", "application/vnd.smart.teacher");
        put("tei", "application/tei+xml");
        put("tex", "application/x-tex");
        put("texi", "application/x-texinfo");
        put("texinfo", "application/x-texinfo");
        put("tfi", "application/thraud+xml");
        put("tfm", "application/x-tex-tfm");
        put("tgz", "application/x-compressed");
        put("thmx", "application/vnd.ms-officetheme");
        put("tif", "image/tiff");
        put("tiff", "image/tiff");
        put("tmo", "application/vnd.tmobile-livetv");
        put("torrent", "application/x-bittorrent");
        put("tpl", "application/vnd.groove-tool-template");
        put("tpt", "application/vnd.trid.tpt");
        put("tr", "application/x-troff");
        put("tra", "application/vnd.trueapp");
        put("trm", "application/x-msterminal");
        put("tsd", "application/timestamped-data");
        put("tsv", "text/tab-separated-values");
        put("ttf", "application/x-font-ttf");
        put("ttl", "text/turtle");
        put("twd", "application/vnd.simtech-mindmapper");
        put("txd", "application/vnd.genomatix.tuxedo");
        put("txf", "application/vnd.mobius.txf");
        put("txt", "text/plain");
        put("ufd", "application/vnd.ufdl");
        put("uls", "text/iuls");
        put("umj", "application/vnd.umajin");
        put("unityweb", "application/vnd.unity");
        put("uoml", "application/vnd.uoml+xml");
        put("uri", "text/uri-list");
        put("ustar", "application/x-ustar");
        put("utz", "application/vnd.uiq.theme");
        put("uu", "text/x-uuencode");
        put("uva", "audio/vnd.dece.audio");
        put("uvh", "video/vnd.dece.hd");
        put("uvi", "image/vnd.dece.graphic");
        put("uvm", "video/vnd.dece.mobile");
        put("uvp", "video/vnd.dece.pd");
        put("uvs", "video/vnd.dece.sd");
        put("uvu", "video/vnd.uvvu.mp4");
        put("uvv", "video/vnd.dece.video");
        put("vcd", "application/x-cdlink");
        put("vcf", "text/x-vcard");
        put("vcg", "application/vnd.groove-vcard");
        put("vcs", "text/x-vcalendar");
        put("vcx", "application/vnd.vcx");
        put("vis", "application/vnd.visionary");
        put("viv", "video/vnd.vivo");
        put("vrml", "x-world/x-vrml");
        put("vsd", "application/vnd.visio");
        put("vsdx", "application/vnd.visio2013");
        put("vsf", "application/vnd.vsf");
        put("vtu", "model/vnd.vtu");
        put("vxml", "application/voicexml+xml");
        put("wad", "application/x-doom");
        put("wav", "audio/x-wav");
        put("wax", "audio/x-ms-wax");
        put("wbmp", "image/vnd.wap.wbmp");
        put("wbs", "application/vnd.criticaltools.wbs+xml");
        put("wbxml", "application/vnd.wap.wbxml");
        put("wcm", "application/vnd.ms-works");
        put("wdb", "application/vnd.ms-works");
        put("weba", "audio/webm");
        put("webm", "video/webm");
        put("webp", "image/webp");
        put("wg", "application/vnd.pmi.widget");
        put("wgt", "application/widget");
        put("wks", "application/vnd.ms-works");
        put("wm", "video/x-ms-wm");
        put("wma", "audio/x-ms-wma");
        put("wmd", "application/x-ms-wmd");
        put("wmf", "application/x-msmetafile");
        put("wml", "text/vnd.wap.wml");
        put("wmlc", "application/vnd.wap.wmlc");
        put("wmls", "text/vnd.wap.wmlscript");
        put("wmlsc", "application/vnd.wap.wmlscriptc");
        put("wmv", "video/x-ms-wmv");
        put("wmx", "video/x-ms-wmx");
        put("wmz", "application/x-ms-wmz");
        put("woff", "application/x-font-woff");
        put("wpd", "application/vnd.wordperfect");
        put("wpl", "application/vnd.ms-wpl");
        put("wps", "application/vnd.ms-works");
        put("wqd", "application/vnd.wqd");
        put("wri", "application/x-mswrite");
        put("wrl", "x-world/x-vrml");
        put("wrl", "model/vrml");
        put("wrz", "x-world/x-vrml");
        put("wsdl", "application/wsdl+xml");
        put("wspolicy", "application/wspolicy+xml");
        put("wtb", "application/vnd.webturbo");
        put("wvx", "video/x-ms-wvx");
        put("x3d", "application/vnd.hzn-3d-crossword");
        put("xaf", "x-world/x-vrml");
        put("xap", "application/x-silverlight-app");
        put("xar", "application/vnd.xara");
        put("xbap", "application/x-ms-xbap");
        put("xbd", "application/vnd.fujixerox.docuworks.binder");
        put("xbm", "image/x-xbitmap");
        put("xdf", "application/xcap-diff+xml");
        put("xdm", "application/vnd.syncml.dm+xml");
        put("xdp", "application/vnd.adobe.xdp+xml");
        put("xdssc", "application/dssc+xml");
        put("xdw", "application/vnd.fujixerox.docuworks");
        put("xenc", "application/xenc+xml");
        put("xer", "application/patch-ops-error+xml");
        put("xfdf", "application/vnd.adobe.xfdf");
        put("xfdl", "application/vnd.xfdl");
        put("xhtml", "application/xhtml+xml");
        put("xif", "image/vnd.xiff");
        put("xla", "application/vnd.ms-excel");
        put("xlam", "application/vnd.ms-excel.addin.macroenabled.12");
        put("xlc", "application/vnd.ms-excel");
        put("xlm", "application/vnd.ms-excel");
        put("xls", "application/vnd.ms-excel");
        put("xlsb", "application/vnd.ms-excel.sheet.binary.macroenabled.12");
        put("xlsm", "application/vnd.ms-excel.sheet.macroenabled.12");
        put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        put("xlt", "application/vnd.ms-excel");
        put("xltm", "application/vnd.ms-excel.template.macroenabled.12");
        put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        put("xlw", "application/vnd.ms-excel");
        put("xml", "application/xml");
        put("xo", "application/vnd.olpc-sugar");
        put("xof", "x-world/x-vrml");
        put("xop", "application/xop+xml");
        put("xpi", "application/x-xpinstall");
        put("xpm", "image/x-xpixmap");
        put("xpr", "application/vnd.is-xpr");
        put("xps", "application/vnd.ms-xpsdocument");
        put("xpw", "application/vnd.intercon.formnet");
        put("xslt", "application/xslt+xml");
        put("xsm", "application/vnd.syncml+xml");
        put("xspf", "application/xspf+xml");
        put("xul", "application/vnd.mozilla.xul+xml");
        put("xwd", "image/x-xwindowdump");
        put("xyz", "chemical/x-xyz");
        put("yaml", "text/yaml");
        put("yang", "application/yang");
        put("yin", "application/yin+xml");
        put("z", "application/x-compress");
        put("zaz", "application/vnd.zzazz.deck+xml");
        put("zip", "application/zip");
        put("zir", "application/vnd.zul");
        put("zmm", "application/vnd.handheld-entertainment+xml");
    }};

    private FileType() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 得到文件头
     *
     * @param inputStream 文件
     * @return 文件头
     * @throws IOException
     */
    private static String getFileHeadContent(InputStream inputStream) throws IOException {
        byte[] b = new byte[28];
        inputStream.read(b, 0, 28);
        return String.valueOf(Hex.encodeHex(b, false));
    }

    /**
     * 得到文件头
     *
     * @param file 文件
     * @return 文件头
     * @throws IOException
     */
    private static String getFileHeadContent(File file) throws IOException {

        try (InputStream inputStream = new FileInputStream(file)) {
            return getFileHeadContent(inputStream);
        }
    }


    /**
     * 判断文件类型
     *
     * @param file 文件
     * @return 文件类型
     * @throws IOException 异常
     */
    public static String getFileType(InputStream file) throws IOException {
        String fileHead = getFileHeadContent(file);

        if (fileHead == null || fileHead.length() == 0) {
            return null;
        }

        fileHead = fileHead.toUpperCase();
        for (Entry<String, String> entry : mFileTypes.entrySet()) {
            if (fileHead.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        //System.out.println("\n空");
        return "unknown";
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
        for (Entry<String, String> entry : mFileTypes.entrySet()) {
            if (fileHead.startsWith(entry.getKey())) {
                if ("office2003".equals(entry.getValue()) || "officeX".equals(entry.getValue())) {
                    if (isOfficeFileByExtName(file.getName())) {
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
     *
     * @param fileName 文件名
     * @return 文件名
     */
    public static String truncateFileExtName(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return "";
        int firstIndex = fileName.lastIndexOf("/");

        int firstIndex2 = fileName.lastIndexOf("\\");
        if (firstIndex < firstIndex2) {
            firstIndex = firstIndex2;
        }

        firstIndex = firstIndex < 0 ? 0 : firstIndex + 1;

        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex < 0) {
            return fileName;
        }

        return fileName.substring(firstIndex, lastIndex);
    }

    /**
     * 截取文件后缀名
     *
     * @param fileName 文件名
     * @return 文件名包括文件路径，去掉后缀名
     */
    public static String truncateFileExtNameWithPath(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return "";
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex < 0)
            return "";
        return fileName.substring(0,lastIndex);
    }


    /**
     * 获取文件的后缀名
     *
     * @param fileName 文件名
     * @return 后缀名
     */
    public static String getFileExtName(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return "";
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex < 0)
            return "";
        return fileName.substring(lastIndex + 1, fileName.length());
    }

    /*
     * 判断输入的文件是够是office的文件
     */
    public static boolean isOfficeFileByExtName(String fileName) {
        String suffix = StringUtils.lowerCase(getFileExtName(fileName));
        if (StringUtils.isBlank(suffix))
            return false;

        return suffix.equalsIgnoreCase("doc") || suffix.equalsIgnoreCase("docx")
            || suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx")
            || suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")
            || suffix.equalsIgnoreCase("vsd") || suffix.equalsIgnoreCase("vsdx")
            || suffix.equalsIgnoreCase("pub") || suffix.equalsIgnoreCase("pubx");
    }

    /*
     * 判断输入的文件是够是office的文件
     */
    public static boolean isOffice2003FileByExtName(String fileName) {

        String suffix = StringUtils.lowerCase(getFileExtName(fileName));

        if (StringUtils.isBlank(suffix))
            return false;

        return suffix.equalsIgnoreCase("doc")
            || suffix.equalsIgnoreCase("ppt")
            || suffix.equalsIgnoreCase("xls")
            || suffix.equalsIgnoreCase("vsd")
            || suffix.equalsIgnoreCase("pub");
    }

    /*
     * 判断输入的文件是够是office的文件
     */
    public static boolean isOfficeFile(File file) {
        try {
            String fileHead = getFileHeadContent(file);
            if (fileHead.startsWith(OFFICE2003_FILE_HEAD))//mFileTypes.get("office2003")))
                return true;
            if (fileHead.startsWith(OFFICE_XML_FILE_HEAD))//mFileTypes.get("officeX")))
                return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static boolean isOfficeFile(String fileName) {
        return isOfficeFile(new File(fileName));
    }

    public static boolean isOffice2003File(File file) {
        try {
            String fileHead = getFileHeadContent(file);
            if (fileHead.startsWith(OFFICE2003_FILE_HEAD))//mFileTypes.get("office2003")))
                return true;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static boolean isOffice2003File(String fileName) {
        return isOffice2003File(new File(fileName));
    }

    public static String mapExtNameToMimeType(String extName) {
        String mimeType = extMimeTypeMap.get(StringUtils.lowerCase(extName));
        if (mimeType != null)
            return mimeType;
        return "application/octet-stream";
    }

    public static String getFileMimeType(String fileUrl) {
        return mapExtNameToMimeType(getFileExtName(fileUrl));
    }
}
