package org.homelinux.nx01.proxyma.core;

/**
 * <p/>
 * User: makko
 * Date: 18-dic-2006
 * Time: 17.30.32
 * </p><p>
 * Simple Global Constants Class Container for proxyma Project.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxymaConstants {
    //Servlet parameter names
    public final static String proxyHost = "proxyHost";
    public final static String proxyPort = "proxyPort";
    public final static String showMasqueradedResources = "showMasqueradedResources";
    public final static String ehcacheSubsystemConfigurationFile = "ehcacheSubsystemConfigurationFile";
    public final static String ehcacheSubsystemStatus = "ehcacheSubsystemStatus";

    //Misc Constants
    public final static String EMPTY_STRING = "";
    public final static int BUFSIZE = 1024; //these are bytes (1Kb)
    public final static int MAX_SIZE_OF_RAMBUFFER = 262144; //these are bytes (256Kb)
    public final static int NULL_VALUE = -1;

    //True, false enabled disabled..
    public final static String TRUE = "True";
    public final static String FALSE = "False";
    public final static String ENABLED = "Enabled";
    public final static String DISABLED = "Disabled";
    public final static String PROCESS = "Process";
    public final static String REMOVE = "Remove";

    //HTTP RETURN STATUS
    public final static int MULTIPLE_CHOICES = 300;
    public final static int UNUSED_30X_CODE = 306;
    public final static int TEMPORARY_REDIRECT = 307;
    public final static int INTERNAL_SERVER_ERROR = 500;
    public final static int BAD_REQUEST = 400;
    public final static int OK = 200;
    public final static int NOT_FOUND = 404;
    public final static int FORBIDDEN = 403;

    //HTTP Headers
    public final static String HOST = "host";
    public final static String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public final static String CONTENT_TYPE = "Content-Type";
    public final static String CONTENT_ENCODING = "Content-Encoding";
    public final static String CACHE_CONTROL = "ResourceCache-Control";
    public final static String PRAGMA = "Pragma";
    public final static String CONTENT_LENGTH = "Content-Length";
    public final static String EXPIRES = "Expires";
    public final static String SET_COOKIE = "Set-Cookie";
    public final static String KEEP_ALIVE = "Keep-Alive";
    public final static String COOKIE = "Cookie";
    public final static String FORM_POST_MODE = "application/x-www-form-urlencoded";
    public final static String LOCATION = "Location";

    //Misc HTTP Constants
    public final static String PATH_SEPARATOR = "/";
    public final static String QUERYSTRING_SEPARATOR = "?";
    public final static String GET_METHOD = "GET";
    public final static String POST_METHOD = "POST";
    public final static String DEFAULT_CHARSET = "UTF-8";
    public final static String NO_CACHE = "no-cache";
    public final static String PUBLIC = "public";
    public final static String PRIVATE = "private";
    public final static String NO_STORE = "no-store";
    public final static String MAX_AGE = "max-age=";
    public final static String MAX_AGE_ZERO = "max-age=0";
    public final static String COOKIE_PATH = "path=";
    public final static String COOKIE_DOMAIN = "domain=";
    public final static String COOKIE_EXPIRES = "expires=";
    public final static String COOKIE_VERSION = "version=";
    public final static String COOKIE_SECURE = "secure";
    public final static String COOKIE_TOKENS_DELIMITER = ";";
    public final static String COOKIE_VALUE_DELIMITER = "=";
    public final static String JSESSIONID_COOKIE = "JSESSIONID";
    public final static String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
    public final static String DOT = ".";

    //MIME Content types
    public final static String HTML_PAGE = "html";
    public final static String GZIP_DATA = "gzip";
    public final static String TEXT_PLAIN = "text/plain";
    public final static String TEXT_HTML = "text/html";
    public final static String JAVASCRIPT_LIBRARY = "javascript";
    public final static String CSS_STYLESHEET = "css";

    //System property for the configuration file of the logging subsystem
    public final static String log4jConfigurationFileName = "proxyma_log4j.properties";
    public final static String proxymaLoggingBasepathSystemProperty = "proxyma.logging.basepath";

    //Logging Subsystem Default Values
    public final static String DEFAULT_LOGGING_PATH = "./";
    public final static String DEFAULT_LOGGING_CONFIGURATION_FILE = DEFAULT_LOGGING_PATH + log4jConfigurationFileName;
    public final static String DEFAULT_EHCACHE_CONFIGURATION_FILE = DEFAULT_LOGGING_PATH + "ehcache.xml";

    //servlet parameter default values
    public final static boolean DEFAULT_SHOW_MASQUERADED_RESOURCES = true;

    //LOGGING POLICY Constants
    public final static int LOGGING_POLICY_NONE = 0;
    public final static int LOGGING_POLICY_PRODUCTION = 1;
    public final static int LOGGING_POLICY_PERFORMANCE_TEST = 2;
    public final static int LOGGING_POLICY_ONLINE_AUDIT = 3;
    public final static int LOGGING_POLICY_ONFILE_AUDIT = 4;
    public final static String[] LOGGING_POLICY_STRING = {"None", "Production", "Performance Test", "On-Line Audit", "On-File Audit"};

    //Default Values for new rule.
    public final static boolean DEFAULT_REWRITER_ENGINE_ENABLED = true;
    public final static int DEFAULT_LOGGING_POLICY = LOGGING_POLICY_PRODUCTION;
    public final static boolean DEFAULT_RULE_ENABLED = true;
    public final static int DEFAULT_JAVASCRIPT_HANDLING_MODE = RewriterConstants.PROCESS;
    public final static String DEFAULT_NEW_RULE_FOLDER = "proxyma";
    public final static String DEFAULT_NEW_RULE_PROXYPASSHOST = "http://proxyma.sourceforge.net/";
    public final static boolean DEFAULT_CACHE_STATUS = true;
    public final static int DEFAULT_MAX_POST_SIZE = 65535; //these are bytes (64Kb)
    public final static int DEFAULT_MAX_CACHED_RESOURCE_SIZE = 5242880; //these are bytes (5Mb)

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
            "<tr bgcolor=\"" + bgcolorPlaceHolder + "\">\n" +
            "<td align=\"left\"><a href=\"./" + proxyFolderPlaceHolder + "/\">" + proxyFolderPlaceHolder + "</a></td>" +
            "<td align=\"left\"><a href=\"" + proxyPassHostPlaceHolder + "\">" + proxyPassHostPlaceHolder + "</a></td>" +
            "<td align=\"center\"><font color=\"" + statusColorPlaceHolder + "\">" + statusPlaceHolder + "</font></td>\n" +
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
