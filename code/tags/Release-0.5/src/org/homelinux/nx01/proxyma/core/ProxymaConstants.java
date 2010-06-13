package org.homelinux.nx01.proxyma.core;

/**
 *<p>
 * User: makko
 * Date: 18-dic-2006
 * Time: 17.30.32
 * </p><p>
 * Simple Global Constants Class Container for proxyma Project.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxymaConstants {
    //Servlet parameter names
    public final static String proxyHost = "proxyHost";
    public final static String proxyPort = "proxyPort";
    public final static String showMasqueradedResources = "showMasqueradedResources";

    //System property for specify the path of the logfiles
    public final static String proxymaLoggingBasepathProperty = "proxyma.logging.basepath";

    //System property for the configuration file of the log4j subsystem
    public final static String log4jConfigurationFileProperty = "proxyma.log4j.configurationFile";
    public final static String log4jConfigurationFileName = "proxyma_log4j.properties";

    //servlet parameter default values
    public final static boolean DEFAULT_SHOW_MASQUERADED_RESOURCES = true;

    //Misc Constants
    public final static String EMPTY_STRING = "";
    public final static int BUFSIZE = 1024;

    //True, false enabled disabled..
    public final static String TRUE = "true";
    public final static String FALSE = "false";
    public final static String ENABLED = "enabled";
    public final static String DISABLED = "disabled";
    public final static String REWRITE = "rewrite";
    public final static String REMOVE = "remove";

    //HTTP RETURN STATUS
    public final static int SERVER_ERROR_400 = 400;
    public final static int REDIRECT_300 = 300;
    public final static int REDIRECT_306 = 306;
    public final static int REDIRECT_307 = 307;

    //HTTP Headers
    public final static String HOST_HEADER = "host";
    public final static String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public final static String CONTENT_TYPE = "content-type";
    public final static String FORM_POST_MODE = "application/x-www-form-urlencoded";
    public final static String LOCATION = "Location";
    public final static String CACHE_CONTROL = "cache-control";
    public final static String PRAGMA = "pragma";

    //MIME Content types
    public final static String HTML_PAGE = "html";
    public final static String GZIP_DATA = "gzip";
    public final static String TEXT_PLAIN = "text/plain";
    public final static String JAVASCRIPT_LIBRARY = "javascript";
    public final static String CSS_STYLESHEET = "css";

    //Misc HTTP Constants
    public final static String PATH_SEPARATOR = "/";
    public final static String QUERYSTRING_SEPARATOR = "?";
    public final static String GET_METHOD = "GET";
    public final static String DEFAULT_CHARSET = "UTF-8";
    public final static String NO_CACHE = "no-cache";

    //LOGGING POLICY Constants
    public final static int LOGGING_POLICY_NONE = 0;
    public final static int LOGGING_POLICY_PRODUCTION = 1;
    public final static int LOGGING_POLICY_ONLINE_AUDIT = 2;
    public final static int LOGGING_POLICY_ONFILE_AUDIT = 3;
    public final static String[] LOGGING_POLICY_STRING = {"None", "Production", "On-Line Audit", "On-File Audit"};

    //Default Values for new rule.
    public final static int DEFAULT_MAX_POST_SIZE = 65535;
    public final static boolean DEFAULT_REWRITER_ENGINE_ENABLED = true;
    public final static int DEFAULT_LOGGING_POLICY = LOGGING_POLICY_PRODUCTION;
    public final static boolean DEFAULT_RULE_ENABLED = true;
    public final static int DEFAULT_JAVASCRIPT_HANDLING_MODE = RewriterConstants.REWRITE;
    public final static String DEFAULT_NEW_RULE_FOLDER = "proxyma";
    public final static String DEFAULT_NEW_RULE_PROXYPASSHOST = "http://proxyma.sourceforge.net/";

    //Placeholter for the substitutions into the row template
    public final static String proxyFolderPlaceHolder = "%PROXYFOLDER%";
    public final static String proxyPassHostPlaceHolder = "%PROXYPASSHOST%";
    public final static String statusPlaceHolder = "%STATUS%";
    public final static String statusColorPlaceHolder = "%COLOR%";
    public final static String bgcolorPlaceHolder = "%BGCOLOR%";
    public final static String evenBgcolor = "#f0f0f0";
    public final static String oddBgcolor = "#daeaff";


    //Static content templates of the page that shows masqueraded resources
    public final static String html_head_template =
        "<html>\n" +
            "<head><title>Proxyma Available Resources:</title><style type=\"text/css\">td{font-family: Verdana, Arial, Helvetica, sans-serif; font-size: small;}</style></head>\n" +
            "<body><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"medium\"><b>Available Rules:</b><hr/>\n" +
                "<table align=\"center\" width=\"95%\">\n" +
                    "<tr bgcolor=\"#bacaff\">\n" +
                        "<td width=\"20%\" align=\"left\"><b>Resource</b></td>" +
                        "<td width=\"65%\" align=\"left\"><b>Masqueraded Resource</b></td>" +
                        "<td width=\"15%\" align=\"center\"><b>status</b></td>\n" +
                    "</tr>\n";
    public final static String html_resource_row_template =
                    "<tr bgcolor=\""+bgcolorPlaceHolder+"\">\n" +
                        "<td align=\"left\"><a href=\"./"+proxyFolderPlaceHolder+"/\">"+proxyFolderPlaceHolder+"</a></td>" +
                        "<td align=\"left\"><a href=\""+proxyPassHostPlaceHolder+"\">"+proxyPassHostPlaceHolder+"</a></td>" +
                        "<td align=\"center\"><font color=\""+statusColorPlaceHolder+"\">"+statusPlaceHolder+"</font></td>\n" +
                    "</tr>\n";
    public final static String html_tail_template =
                "</table>\n" +
                "<hr/>Generated by Proxyma.\n" +
            "</font></body>\n" +
        "</html>\n";

    //Misc Messages
    public final static String MASQUERADED_RESOURCE_NOTFOUND = "Resource not served by Proxyma.";
    public final static String DESTINATION_TEMPORARY_DISABLED = "Resource temporary disabled by the Administrator.";
    public final static String SHOW_RULES_LIST = "Show the rules list.";
}
