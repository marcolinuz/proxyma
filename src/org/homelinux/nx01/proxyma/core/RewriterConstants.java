package org.homelinux.nx01.proxyma.core;

/**
 * <p>
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 * </p><p>
 * User: makko
 * Date: 18-dic-2006
 * Time: 17.30.32
 * </p><p>
 * Simple Global Constants Class Container for RewriterEngine.
 * </p>
 */
public class RewriterConstants {
    //HANDLED TAG NAMES
    public final static String A = "a";
    public final static String APPLET = "applet";
    public final static String AREA = "area";
    public final static String IMG = "img";
    public final static String LINK = "link";
    public final static String FORM = "form";
    public final static String INPUT = "input";
    public final static String SCRIPT = "script";
    public final static String STYLE = "style";
    public final static String BODY = "body";
    public final static String BASE = "base";
    public final static String FRAME = "frame";
    public final static String IFRAME = "iframe";
    public final static String INS = "ins";
    public final static String DEL = "del";
    public final static String OBJECT = "del";
    public final static String TD = "td";
    public final static String TABLE = "table";


    //TAG ATTRIBUTES
    public final static String HREF = "href";
    public final static String SRC = "src";
    public final static String ACTION = "action";
    public final static String ONLOAD = "onLoad";
    public final static String JAVASCRIPT_LIBRARY = "javascript";
    public final static String ARCHIVE = "archive";
    public final static String CODE = "code";
    public final static String CODEBASE = "codebase";
    public final static String LONGDESC = "longdesc";
    public final static String ISMAP = "ismap";
    public final static String USEMAP = "usemap";
    public final static String CITE = "cite";
    public final static String DATA = "data";
    public final static String BACKGROUND = "background";

    //JAVASCRIPT EVENTS
    public final static String EVENTS[] = {"onClick", "onRollOver", "onRollOut", "onChange"};

    //MISC CONSTANTS
    public final static String EQ = "=";
    public final static String QUOT = "\"";
    public final static String APOS = "'";
    public final static String CSS_REGEXP_URL_PREFIX = "url\\(";
    public final static String CSS_URL_PREFIX = "url(";
    public final static String LINEBEGIN_REGEXP = "^";
    public final static String HTTP = "http";
    public final static String HTTPS = "https";
    public final static int HTTP_STANDARD_PORT = 80;
    public final static int HTTPS_STANDARD_PORT = 443;
    public final static String HTTP_URL_PREFIX = "http://";
    public final static String HTTPS_URL_PREFIX = "https://";

    //JAVASCRIPT HANDLING MODES
    public final static int REWRITE = 0;
    public final static int REMOVE = 1;

    //MISC CONSTANTS
    public final static String EMPTY_STRING = "";
}
