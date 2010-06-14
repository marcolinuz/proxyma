package m.c.m.proxyma.core;

/**
 * <p>
 * This class is a collection of the most important (from a proxy point of view)
 * headers and header values that can be set into a response.<br/>
 *
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
 */
public class HttpConstants {
    //HTTP Headers
    public final static String HOST = "host";
    public final static String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public final static String CONTENT_TYPE = "Content-Type";
    public final static String CONTENT_ENCODING = "Content-Encoding";
    public final static String TRANSFER_ENCODING = "Transfer-Encoding";
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
    public final static String DEFAULT_CONTENT_TYPE = "text/html";
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
}
