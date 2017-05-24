package forpdateam.ru.forpda.utils;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by radiationx on 23.05.17.
 */

public class MimeTypeUtil {
    private static HashMap<String, String> types = new HashMap<>();

    private static void init() {
        types.put("323", "text/h323");
        types.put("3g2", "video/3gpp2");
        types.put("3gp", "video/3gpp");
        types.put("3gp2", "video/3gpp2");
        types.put("3gpp", "video/3gpp");
        types.put("7z", "application/x-7z-compressed");
        types.put("aa", "audio/audible");
        types.put("AAC", "audio/aac");
        types.put("aaf", "application/octet-stream");
        types.put("aax", "audio/vnd.audible.aax");
        types.put("ac3", "audio/ac3");
        types.put("aca", "application/octet-stream");
        types.put("accda", "application/msaccess.addin");
        types.put("accdb", "application/msaccess");
        types.put("accdc", "application/msaccess.cab");
        types.put("accde", "application/msaccess");
        types.put("accdr", "application/msaccess.runtime");
        types.put("accdt", "application/msaccess");
        types.put("accdw", "application/msaccess.webapplication");
        types.put("accft", "application/msaccess.ftemplate");
        types.put("acx", "application/internet-property-stream");
        types.put("AddIn", "text/xml");
        types.put("ade", "application/msaccess");
        types.put("adobebridge", "application/x-bridge-url");
        types.put("adp", "application/msaccess");
        types.put("ADT", "audio/vnd.dlna.adts");
        types.put("ADTS", "audio/aac");
        types.put("afm", "application/octet-stream");
        types.put("ai", "application/postscript");
        types.put("aif", "audio/aiff");
        types.put("aifc", "audio/aiff");
        types.put("aiff", "audio/aiff");
        types.put("air", "application/vnd.adobe.air-application-installer-package+zip");
        types.put("amc", "application/mpeg");
        types.put("anx", "application/annodex");
        types.put("apk", "application/vnd.android.package-archive");
        types.put("application", "application/x-ms-application");
        types.put("art", "image/x-jg");
        types.put("asa", "application/xml");
        types.put("asax", "application/xml");
        types.put("ascx", "application/xml");
        types.put("asd", "application/octet-stream");
        types.put("asf", "video/x-ms-asf");
        types.put("ashx", "application/xml");
        types.put("asi", "application/octet-stream");
        types.put("asm", "text/plain");
        types.put("asmx", "application/xml");
        types.put("aspx", "application/xml");
        types.put("asr", "video/x-ms-asf");
        types.put("asx", "video/x-ms-asf");
        types.put("atom", "application/atom+xml");
        types.put("au", "audio/basic");
        types.put("avi", "video/x-msvideo");
        types.put("axa", "audio/annodex");
        types.put("axs", "application/olescript");
        types.put("axv", "video/annodex");
        types.put("bas", "text/plain");
        types.put("bcpio", "application/x-bcpio");
        types.put("bin", "application/octet-stream");
        types.put("bmp", "image/bmp");
        types.put("c", "text/plain");
        types.put("cab", "application/octet-stream");
        types.put("caf", "audio/x-caf");
        types.put("calx", "application/vnd.ms-office.calx");
        types.put("cat", "application/vnd.ms-pki.seccat");
        types.put("cc", "text/plain");
        types.put("cd", "text/plain");
        types.put("cdda", "audio/aiff");
        types.put("cdf", "application/x-cdf");
        types.put("cer", "application/x-x509-ca-cert");
        types.put("cfg", "text/plain");
        types.put("chm", "application/octet-stream");
        types.put("class", "application/x-java-applet");
        types.put("clp", "application/x-msclip");
        types.put("cmd", "text/plain");
        types.put("cmx", "image/x-cmx");
        types.put("cnf", "text/plain");
        types.put("cod", "image/cis-cod");
        types.put("config", "application/xml");
        types.put("contact", "text/x-ms-contact");
        types.put("coverage", "application/xml");
        types.put("cpio", "application/x-cpio");
        types.put("cpp", "text/plain");
        types.put("crd", "application/x-mscardfile");
        types.put("crl", "application/pkix-crl");
        types.put("crt", "application/x-x509-ca-cert");
        types.put("cs", "text/plain");
        types.put("csdproj", "text/plain");
        types.put("csh", "application/x-csh");
        types.put("csproj", "text/plain");
        types.put("css", "text/css");
        types.put("csv", "text/csv");
        types.put("cur", "application/octet-stream");
        types.put("cxx", "text/plain");
        types.put("dat", "application/octet-stream");
        types.put("datasource", "application/xml");
        types.put("dbproj", "text/plain");
        types.put("dcr", "application/x-director");
        types.put("def", "text/plain");
        types.put("deploy", "application/octet-stream");
        types.put("der", "application/x-x509-ca-cert");
        types.put("dgml", "application/xml");
        types.put("dib", "image/bmp");
        types.put("dif", "video/x-dv");
        types.put("dir", "application/x-director");
        types.put("disco", "text/xml");
        types.put("divx", "video/divx");
        types.put("dll", "application/x-msdownload");
        types.put("dll.config", "text/xml");
        types.put("dlm", "text/dlm");
        types.put("doc", "application/msword");
        types.put("docm", "application/vnd.ms-word.document.macroEnabled.12");
        types.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        types.put("dot", "application/msword");
        types.put("dotm", "application/vnd.ms-word.template.macroEnabled.12");
        types.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        types.put("dsp", "application/octet-stream");
        types.put("dsw", "text/plain");
        types.put("dtd", "text/xml");
        types.put("dtsConfig", "text/xml");
        types.put("dv", "video/x-dv");
        types.put("dvi", "application/x-dvi");
        types.put("dwf", "drawing/x-dwf");
        types.put("dwp", "application/octet-stream");
        types.put("dxr", "application/x-director");
        types.put("eml", "message/rfc822");
        types.put("emz", "application/octet-stream");
        types.put("eot", "application/vnd.ms-fontobject");
        types.put("eps", "application/postscript");
        types.put("etl", "application/etl");
        types.put("etx", "text/x-setext");
        types.put("evy", "application/envoy");
        types.put("exe", "application/octet-stream");
        types.put("exe.config", "text/xml");
        types.put("fdf", "application/vnd.fdf");
        types.put("fif", "application/fractals");
        types.put("filters", "application/xml");
        types.put("fla", "application/octet-stream");
        types.put("flac", "audio/flac");
        types.put("flr", "x-world/x-vrml");
        types.put("flv", "video/x-flv");
        types.put("fsscript", "application/fsharp-script");
        types.put("fsx", "application/fsharp-script");
        types.put("generictest", "application/xml");
        types.put("gif", "image/gif");
        types.put("group", "text/x-ms-group");
        types.put("gsm", "audio/x-gsm");
        types.put("gtar", "application/x-gtar");
        types.put("gz", "application/x-gzip");
        types.put("h", "text/plain");
        types.put("hdf", "application/x-hdf");
        types.put("hdml", "text/x-hdml");
        types.put("hhc", "application/x-oleobject");
        types.put("hhk", "application/octet-stream");
        types.put("hhp", "application/octet-stream");
        types.put("hlp", "application/winhlp");
        types.put("hpp", "text/plain");
        types.put("hqx", "application/mac-binhex40");
        types.put("hta", "application/hta");
        types.put("htc", "text/x-component");
        types.put("htm", "text/html");
        types.put("html", "text/html");
        types.put("htt", "text/webviewhtml");
        types.put("hxa", "application/xml");
        types.put("hxc", "application/xml");
        types.put("hxd", "application/octet-stream");
        types.put("hxe", "application/xml");
        types.put("hxf", "application/xml");
        types.put("hxh", "application/octet-stream");
        types.put("hxi", "application/octet-stream");
        types.put("hxk", "application/xml");
        types.put("hxq", "application/octet-stream");
        types.put("hxr", "application/octet-stream");
        types.put("hxs", "application/octet-stream");
        types.put("hxt", "text/html");
        types.put("hxv", "application/xml");
        types.put("hxw", "application/octet-stream");
        types.put("hxx", "text/plain");
        types.put("i", "text/plain");
        types.put("ico", "image/x-icon");
        types.put("ics", "application/octet-stream");
        types.put("idl", "text/plain");
        types.put("ief", "image/ief");
        types.put("iii", "application/x-iphone");
        types.put("inc", "text/plain");
        types.put("inf", "application/octet-stream");
        types.put("ini", "text/plain");
        types.put("inl", "text/plain");
        types.put("ins", "application/x-internet-signup");
        types.put("ipa", "application/x-itunes-ipa");
        types.put("ipg", "application/x-itunes-ipg");
        types.put("ipproj", "text/plain");
        types.put("ipsw", "application/x-itunes-ipsw");
        types.put("iqy", "text/x-ms-iqy");
        types.put("isp", "application/x-internet-signup");
        types.put("ite", "application/x-itunes-ite");
        types.put("itlp", "application/x-itunes-itlp");
        types.put("itms", "application/x-itunes-itms");
        types.put("itpc", "application/x-itunes-itpc");
        types.put("IVF", "video/x-ivf");
        types.put("jar", "application/java-archive");
        types.put("java", "application/octet-stream");
        types.put("jck", "application/liquidmotion");
        types.put("jcz", "application/liquidmotion");
        types.put("jfif", "image/pjpeg");
        types.put("jnlp", "application/x-java-jnlp-file");
        types.put("jpb", "application/octet-stream");
        types.put("jpe", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("jpg", "image/jpeg");
        types.put("js", "application/javascript");
        types.put("json", "application/json");
        types.put("jsx", "text/jscript");
        types.put("jsxbin", "text/plain");
        types.put("latex", "application/x-latex");
        types.put("library-ms", "application/windows-library+xml");
        types.put("lit", "application/x-ms-reader");
        types.put("loadtest", "application/xml");
        types.put("lpk", "application/octet-stream");
        types.put("lsf", "video/x-la-asf");
        types.put("lst", "text/plain");
        types.put("lsx", "video/x-la-asf");
        types.put("lzh", "application/octet-stream");
        types.put("m13", "application/x-msmediaview");
        types.put("m14", "application/x-msmediaview");
        types.put("m1v", "video/mpeg");
        types.put("m2t", "video/vnd.dlna.mpeg-tts");
        types.put("m2ts", "video/vnd.dlna.mpeg-tts");
        types.put("m2v", "video/mpeg");
        types.put("m3u", "audio/x-mpegurl");
        types.put("m3u8", "audio/x-mpegurl");
        types.put("m4a", "audio/m4a");
        types.put("m4b", "audio/m4b");
        types.put("m4p", "audio/m4p");
        types.put("m4r", "audio/x-m4r");
        types.put("m4v", "video/x-m4v");
        types.put("mac", "image/x-macpaint");
        types.put("mak", "text/plain");
        types.put("man", "application/x-troff-man");
        types.put("manifest", "application/x-ms-manifest");
        types.put("map", "text/plain");
        types.put("master", "application/xml");
        types.put("mda", "application/msaccess");
        types.put("mdb", "application/x-msaccess");
        types.put("mde", "application/msaccess");
        types.put("mdp", "application/octet-stream");
        types.put("me", "application/x-troff-me");
        types.put("mfp", "application/x-shockwave-flash");
        types.put("mht", "message/rfc822");
        types.put("mhtml", "message/rfc822");
        types.put("mid", "audio/mid");
        types.put("midi", "audio/mid");
        types.put("mix", "application/octet-stream");
        types.put("mk", "text/plain");
        types.put("mmf", "application/x-smaf");
        types.put("mno", "text/xml");
        types.put("mny", "application/x-msmoney");
        types.put("mod", "video/mpeg");
        types.put("mov", "video/quicktime");
        types.put("movie", "video/x-sgi-movie");
        types.put("mp2", "video/mpeg");
        types.put("mp2v", "video/mpeg");
        types.put("mp3", "audio/mpeg");
        types.put("mp4", "video/mp4");
        types.put("mp4v", "video/mp4");
        types.put("mpa", "video/mpeg");
        types.put("mpe", "video/mpeg");
        types.put("mpeg", "video/mpeg");
        types.put("mpf", "application/vnd.ms-mediapackage");
        types.put("mpg", "video/mpeg");
        types.put("mpp", "application/vnd.ms-project");
        types.put("mpv2", "video/mpeg");
        types.put("mqv", "video/quicktime");
        types.put("ms", "application/x-troff-ms");
        types.put("msi", "application/octet-stream");
        types.put("mso", "application/octet-stream");
        types.put("mts", "video/vnd.dlna.mpeg-tts");
        types.put("mtx", "application/xml");
        types.put("mvb", "application/x-msmediaview");
        types.put("mvc", "application/x-miva-compiled");
        types.put("mxp", "application/x-mmxp");
        types.put("nc", "application/x-netcdf");
        types.put("nsc", "video/x-ms-asf");
        types.put("nws", "message/rfc822");
        types.put("ocx", "application/octet-stream");
        types.put("oda", "application/oda");
        types.put("odb", "application/vnd.oasis.opendocument.database");
        types.put("odc", "application/vnd.oasis.opendocument.chart");
        types.put("odf", "application/vnd.oasis.opendocument.formula");
        types.put("odg", "application/vnd.oasis.opendocument.graphics");
        types.put("odh", "text/plain");
        types.put("odi", "application/vnd.oasis.opendocument.image");
        types.put("odl", "text/plain");
        types.put("odm", "application/vnd.oasis.opendocument.text-master");
        types.put("odp", "application/vnd.oasis.opendocument.presentation");
        types.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        types.put("odt", "application/vnd.oasis.opendocument.text");
        types.put("oga", "audio/ogg");
        types.put("ogg", "audio/ogg");
        types.put("ogv", "video/ogg");
        types.put("ogx", "application/ogg");
        types.put("one", "application/onenote");
        types.put("onea", "application/onenote");
        types.put("onepkg", "application/onenote");
        types.put("onetmp", "application/onenote");
        types.put("onetoc", "application/onenote");
        types.put("onetoc2", "application/onenote");
        types.put("opus", "audio/ogg");
        types.put("orderedtest", "application/xml");
        types.put("osdx", "application/opensearchdescription+xml");
        types.put("otf", "application/font-sfnt");
        types.put("otg", "application/vnd.oasis.opendocument.graphics-template");
        types.put("oth", "application/vnd.oasis.opendocument.text-web");
        types.put("otp", "application/vnd.oasis.opendocument.presentation-template");
        types.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        types.put("ott", "application/vnd.oasis.opendocument.text-template");
        types.put("oxt", "application/vnd.openofficeorg.extension");
        types.put("p10", "application/pkcs10");
        types.put("p12", "application/x-pkcs12");
        types.put("p7b", "application/x-pkcs7-certificates");
        types.put("p7c", "application/pkcs7-mime");
        types.put("p7m", "application/pkcs7-mime");
        types.put("p7r", "application/x-pkcs7-certreqresp");
        types.put("p7s", "application/pkcs7-signature");
        types.put("pbm", "image/x-portable-bitmap");
        types.put("pcast", "application/x-podcast");
        types.put("pct", "image/pict");
        types.put("pcx", "application/octet-stream");
        types.put("pcz", "application/octet-stream");
        types.put("pdf", "application/pdf");
        types.put("pfb", "application/octet-stream");
        types.put("pfm", "application/octet-stream");
        types.put("pfx", "application/x-pkcs12");
        types.put("pgm", "image/x-portable-graymap");
        types.put("pic", "image/pict");
        types.put("pict", "image/pict");
        types.put("pkgdef", "text/plain");
        types.put("pkgundef", "text/plain");
        types.put("pko", "application/vnd.ms-pki.pko");
        types.put("pls", "audio/scpls");
        types.put("pma", "application/x-perfmon");
        types.put("pmc", "application/x-perfmon");
        types.put("pml", "application/x-perfmon");
        types.put("pmr", "application/x-perfmon");
        types.put("pmw", "application/x-perfmon");
        types.put("png", "image/png");
        types.put("pnm", "image/x-portable-anymap");
        types.put("pnt", "image/x-macpaint");
        types.put("pntg", "image/x-macpaint");
        types.put("pnz", "image/png");
        types.put("pot", "application/vnd.ms-powerpoint");
        types.put("potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
        types.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        types.put("ppa", "application/vnd.ms-powerpoint");
        types.put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        types.put("ppm", "image/x-portable-pixmap");
        types.put("pps", "application/vnd.ms-powerpoint");
        types.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        types.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        types.put("ppt", "application/vnd.ms-powerpoint");
        types.put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        types.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        types.put("prf", "application/pics-rules");
        types.put("prm", "application/octet-stream");
        types.put("prx", "application/octet-stream");
        types.put("ps", "application/postscript");
        types.put("psc1", "application/PowerShell");
        types.put("psd", "application/octet-stream");
        types.put("psess", "application/xml");
        types.put("psm", "application/octet-stream");
        types.put("psp", "application/octet-stream");
        types.put("pub", "application/x-mspublisher");
        types.put("pwz", "application/vnd.ms-powerpoint");
        types.put("qht", "text/x-html-insertion");
        types.put("qhtm", "text/x-html-insertion");
        types.put("qt", "video/quicktime");
        types.put("qti", "image/x-quicktime");
        types.put("qtif", "image/x-quicktime");
        types.put("qtl", "application/x-quicktimeplayer");
        types.put("qxd", "application/octet-stream");
        types.put("ra", "audio/x-pn-realaudio");
        types.put("ram", "audio/x-pn-realaudio");
        types.put("rar", "application/x-rar-compressed");
        types.put("ras", "image/x-cmu-raster");
        types.put("rat", "application/rat-file");
        types.put("rc", "text/plain");
        types.put("rc2", "text/plain");
        types.put("rct", "text/plain");
        types.put("rdlc", "application/xml");
        types.put("reg", "text/plain");
        types.put("resx", "application/xml");
        types.put("rf", "image/vnd.rn-realflash");
        types.put("rgb", "image/x-rgb");
        types.put("rgs", "text/plain");
        types.put("rm", "application/vnd.rn-realmedia");
        types.put("rmi", "audio/mid");
        types.put("rmp", "application/vnd.rn-rn_music_package");
        types.put("roff", "application/x-troff");
        types.put("rpm", "audio/x-pn-realaudio-plugin");
        types.put("rqy", "text/x-ms-rqy");
        types.put("rtf", "application/rtf");
        types.put("rtx", "text/richtext");
        types.put("ruleset", "application/xml");
        types.put("s", "text/plain");
        types.put("safariextz", "application/x-safari-safariextz");
        types.put("scd", "application/x-msschedule");
        types.put("scr", "text/plain");
        types.put("sct", "text/scriptlet");
        types.put("sd2", "audio/x-sd2");
        types.put("sdp", "application/sdp");
        types.put("sea", "application/octet-stream");
        types.put("searchConnector-ms", "application/windows-search-connector+xml");
        types.put("setpay", "application/set-payment-initiation");
        types.put("setreg", "application/set-registration-initiation");
        types.put("settings", "application/xml");
        types.put("sgimb", "application/x-sgimb");
        types.put("sgml", "text/sgml");
        types.put("sh", "application/x-sh");
        types.put("shar", "application/x-shar");
        types.put("shtml", "text/html");
        types.put("sit", "application/x-stuffit");
        types.put("sitemap", "application/xml");
        types.put("skin", "application/xml");
        types.put("sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12");
        types.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        types.put("slk", "application/vnd.ms-excel");
        types.put("sln", "text/plain");
        types.put("slupkg-ms", "application/x-ms-license");
        types.put("smd", "audio/x-smd");
        types.put("smi", "application/octet-stream");
        types.put("smx", "audio/x-smd");
        types.put("smz", "audio/x-smd");
        types.put("snd", "audio/basic");
        types.put("snippet", "application/xml");
        types.put("snp", "application/octet-stream");
        types.put("sol", "text/plain");
        types.put("sor", "text/plain");
        types.put("spc", "application/x-pkcs7-certificates");
        types.put("spl", "application/futuresplash");
        types.put("spx", "audio/ogg");
        types.put("src", "application/x-wais-source");
        types.put("srf", "text/plain");
        types.put("SSISDeploymentManifest", "text/xml");
        types.put("ssm", "application/streamingmedia");
        types.put("sst", "application/vnd.ms-pki.certstore");
        types.put("stl", "application/vnd.ms-pki.stl");
        types.put("sv4cpio", "application/x-sv4cpio");
        types.put("sv4crc", "application/x-sv4crc");
        types.put("svc", "application/xml");
        types.put("svg", "image/svg+xml");
        types.put("swf", "application/x-shockwave-flash");
        types.put("t", "application/x-troff");
        types.put("tar", "application/x-tar");
        types.put("tcl", "application/x-tcl");
        types.put("testrunconfig", "application/xml");
        types.put("testsettings", "application/xml");
        types.put("tex", "application/x-tex");
        types.put("texi", "application/x-texinfo");
        types.put("texinfo", "application/x-texinfo");
        types.put("tgz", "application/x-compressed");
        types.put("thmx", "application/vnd.ms-officetheme");
        types.put("thn", "application/octet-stream");
        types.put("tif", "image/tiff");
        types.put("tiff", "image/tiff");
        types.put("tlh", "text/plain");
        types.put("tli", "text/plain");
        types.put("toc", "application/octet-stream");
        types.put("tr", "application/x-troff");
        types.put("trm", "application/x-msterminal");
        types.put("trx", "application/xml");
        types.put("ts", "video/vnd.dlna.mpeg-tts");
        types.put("tsv", "text/tab-separated-values");
        types.put("ttf", "application/font-sfnt");
        types.put("tts", "video/vnd.dlna.mpeg-tts");
        types.put("txt", "text/plain");
        types.put("u32", "application/octet-stream");
        types.put("uls", "text/iuls");
        types.put("user", "text/plain");
        types.put("ustar", "application/x-ustar");
        types.put("vb", "text/plain");
        types.put("vbdproj", "text/plain");
        types.put("vbk", "video/mpeg");
        types.put("vbproj", "text/plain");
        types.put("vbs", "text/vbscript");
        types.put("vcf", "text/x-vcard");
        types.put("vcproj", "application/xml");
        types.put("vcs", "text/plain");
        types.put("vcxproj", "application/xml");
        types.put("vddproj", "text/plain");
        types.put("vdp", "text/plain");
        types.put("vdproj", "text/plain");
        types.put("vdx", "application/vnd.ms-visio.viewer");
        types.put("vml", "text/xml");
        types.put("vscontent", "application/xml");
        types.put("vsct", "text/xml");
        types.put("vsd", "application/vnd.visio");
        types.put("vsi", "application/ms-vsi");
        types.put("vsix", "application/vsix");
        types.put("vsixlangpack", "text/xml");
        types.put("vsixmanifest", "text/xml");
        types.put("vsmdi", "application/xml");
        types.put("vspscc", "text/plain");
        types.put("vss", "application/vnd.visio");
        types.put("vsscc", "text/plain");
        types.put("vssettings", "text/xml");
        types.put("vssscc", "text/plain");
        types.put("vst", "application/vnd.visio");
        types.put("vstemplate", "text/xml");
        types.put("vsto", "application/x-ms-vsto");
        types.put("vsw", "application/vnd.visio");
        types.put("vsx", "application/vnd.visio");
        types.put("vtx", "application/vnd.visio");
        types.put("wav", "audio/wav");
        types.put("wave", "audio/wav");
        types.put("wax", "audio/x-ms-wax");
        types.put("wbk", "application/msword");
        types.put("wbmp", "image/vnd.wap.wbmp");
        types.put("wcm", "application/vnd.ms-works");
        types.put("wdb", "application/vnd.ms-works");
        types.put("wdp", "image/vnd.ms-photo");
        types.put("webarchive", "application/x-safari-webarchive");
        types.put("webm", "video/webm");
        types.put("webp", "image/webp");
        types.put("webtest", "application/xml");
        types.put("wiq", "application/xml");
        types.put("wiz", "application/msword");
        types.put("wks", "application/vnd.ms-works");
        types.put("WLMP", "application/wlmoviemaker");
        types.put("wlpginstall", "application/x-wlpg-detect");
        types.put("wlpginstall3", "application/x-wlpg3-detect");
        types.put("wm", "video/x-ms-wm");
        types.put("wma", "audio/x-ms-wma");
        types.put("wmd", "application/x-ms-wmd");
        types.put("wmf", "application/x-msmetafile");
        types.put("wml", "text/vnd.wap.wml");
        types.put("wmlc", "application/vnd.wap.wmlc");
        types.put("wmls", "text/vnd.wap.wmlscript");
        types.put("wmlsc", "application/vnd.wap.wmlscriptc");
        types.put("wmp", "video/x-ms-wmp");
        types.put("wmv", "video/x-ms-wmv");
        types.put("wmx", "video/x-ms-wmx");
        types.put("wmz", "application/x-ms-wmz");
        types.put("woff", "application/font-woff");
        types.put("wpl", "application/vnd.ms-wpl");
        types.put("wps", "application/vnd.ms-works");
        types.put("wri", "application/x-mswrite");
        types.put("wrl", "x-world/x-vrml");
        types.put("wrz", "x-world/x-vrml");
        types.put("wsc", "text/scriptlet");
        types.put("wsdl", "text/xml");
        types.put("wvx", "video/x-ms-wvx");
        types.put("x", "application/directx");
        types.put("xaf", "x-world/x-vrml");
        types.put("xaml", "application/xaml+xml");
        types.put("xap", "application/x-silverlight-app");
        types.put("xbap", "application/x-ms-xbap");
        types.put("xbm", "image/x-xbitmap");
        types.put("xdr", "text/plain");
        types.put("xht", "application/xhtml+xml");
        types.put("xhtml", "application/xhtml+xml");
        types.put("xla", "application/vnd.ms-excel");
        types.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        types.put("xlc", "application/vnd.ms-excel");
        types.put("xld", "application/vnd.ms-excel");
        types.put("xlk", "application/vnd.ms-excel");
        types.put("xll", "application/vnd.ms-excel");
        types.put("xlm", "application/vnd.ms-excel");
        types.put("xls", "application/vnd.ms-excel");
        types.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        types.put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        types.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        types.put("xlt", "application/vnd.ms-excel");
        types.put("xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        types.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        types.put("xlw", "application/vnd.ms-excel");
        types.put("xml", "text/xml");
        types.put("xmta", "application/xml");
        types.put("xof", "x-world/x-vrml");
        types.put("XOML", "text/plain");
        types.put("xpm", "image/x-xpixmap");
        types.put("xps", "application/vnd.ms-xpsdocument");
        types.put("xrm-ms", "text/xml");
        types.put("xsc", "application/xml");
        types.put("xsd", "text/xml");
        types.put("xsf", "text/xml");
        types.put("xsl", "text/xml");
        types.put("xslt", "text/xml");
        types.put("xsn", "application/octet-stream");
        types.put("xss", "application/xml");
        types.put("xspf", "application/xspf+xml");
        types.put("xtp", "application/octet-stream");
        types.put("xwd", "image/x-xwindowdump");
        types.put("z", "application/x-compress");
        types.put("zip", "application/zip");
    }

    public static String getType(String extension) {
        if (types.size() == 0) init();
        String res = types.get(extension);
        Log.e("FORPDA_LOG", "GET MIME TYPE " + extension + " : " + res);
        return res;
    }

    public static boolean isImage(String extension) {
        String type = getType(extension);
        if (type == null)
            return false;
        return type.contains("image/");
    }

}
