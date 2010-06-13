package org.homelinux.nx01.proxyma.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * User: makko
 * Date: 08-jan-2006
 * Time: 20.46.32
 * </p><p>
 * This class countains the regular expression patterns and strings used by RewriterEngine.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class RewriterPatterns {

    /**
     * Sets the variables for the new base path to use for the url rewriter methods
     *
     * @param context the new context path to use for the rewriter methods
     */
    public void setNewContext(String context) {
        if (newHtmlContext == null) {
            newHtmlContext = context;
            newCssContext = RewriterConstants.CSS_URL_PREFIX + newHtmlContext;
            newJsContextQuote = RewriterConstants.QUOT + newHtmlContext;
            newJsContextApos = RewriterConstants.APOS + newHtmlContext;
        }
    }

    /**
     * Initialize the Host based Patterns for the matching of the urls.
     * It's used to match strings that will be rewritten.
     *
     * @param proxyHost the host path part of the url to search for
     */
    public void setProxedHost(String proxyHost) throws MalformedURLException {
        if (proxedHost == null) {
            proxedHost = proxyHost;
            cssProxedHostPattern = Pattern.compile(RewriterConstants.CSS_REGEXP_URL_PREFIX + proxedHost, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            urlProxedHostPattern = Pattern.compile(RewriterConstants.LINEBEGIN_REGEXP + proxedHost);
            jsProxedHostQuotePattern = Pattern.compile(RewriterConstants.QUOT + proxedHost, Pattern.MULTILINE);
            jsProxedHostAposPattern = Pattern.compile(RewriterConstants.APOS + proxedHost, Pattern.MULTILINE);

            //Addendum: In some cases the host can have a synonim..
            //For example: http://www.foo.bar/ == http://www.foo.bar:80/
            // ..and also: https://www.foo.bar/ == https://www.foo.bar:443/
	        // and we want to handle this.. ;O)
            int standardPort = 0;
            if (proxedHost.startsWith(RewriterConstants.HTTP))
                standardPort = RewriterConstants.HTTP_STANDARD_PORT;
            else if (proxedHost.startsWith(RewriterConstants.HTTPS))
                standardPort = RewriterConstants.HTTPS_STANDARD_PORT;

            URL checkUrl = new URL(proxedHost);
            if (checkUrl.getPort() == standardPort) {
                synonimProxedHost = proxedHost.replaceFirst(":" + standardPort + "/$", "/");
                cssProxedSynonimHostPattern = Pattern.compile(RewriterConstants.CSS_REGEXP_URL_PREFIX + synonimProxedHost, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                urlProxedSynonimHostPattern = Pattern.compile(RewriterConstants.LINEBEGIN_REGEXP + synonimProxedHost);
                jsProxedSynonimHostQuotePattern = Pattern.compile(RewriterConstants.QUOT + synonimProxedHost, Pattern.MULTILINE);
                jsProxedSynonimHostAposPattern = Pattern.compile(RewriterConstants.APOS + synonimProxedHost, Pattern.MULTILINE);
            } else if (checkUrl.getPort() == -1) {
                synonimProxedHost = proxedHost.replaceFirst("/$", ":" + standardPort + "/");
                cssProxedSynonimHostPattern = Pattern.compile(RewriterConstants.CSS_REGEXP_URL_PREFIX + synonimProxedHost, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                urlProxedSynonimHostPattern = Pattern.compile(RewriterConstants.LINEBEGIN_REGEXP + synonimProxedHost);
                jsProxedSynonimHostQuotePattern = Pattern.compile(RewriterConstants.QUOT + synonimProxedHost, Pattern.MULTILINE);
                jsProxedSynonimHostAposPattern = Pattern.compile(RewriterConstants.APOS + synonimProxedHost, Pattern.MULTILINE);
            } else {
                synonimProxedHost = null;
                cssProxedSynonimHostPattern = null;
                urlProxedSynonimHostPattern = null;
                jsProxedSynonimHostQuotePattern = null;
                jsProxedSynonimHostAposPattern = null;
            }
        }
    }

    /**
     * Initialize the basedpath Patterns for the matching of the urls.
     * They will be used to match strings that will be rewritten.
     *
     * @param proxyPath the basepath part of the url to search for
     */
    public void setProxedBasePath(String proxyPath) {
        if (proxedBasePath == null) {
            proxedBasePath = proxyPath;
            cssProxedBasePathPattern = Pattern.compile(RewriterConstants.CSS_REGEXP_URL_PREFIX + proxedBasePath, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            urlProxedBasePathPattern = Pattern.compile(RewriterConstants.LINEBEGIN_REGEXP + proxedBasePath);
            jsProxedBasePathQuotePattern = Pattern.compile(RewriterConstants.QUOT + proxedBasePath, Pattern.MULTILINE);
            jsProxedBasePathAposPattern = Pattern.compile(RewriterConstants.APOS + proxedBasePath, Pattern.MULTILINE);
        }
    }

    /**
     * Obtains the new context global variable
     *
     * @return the new context
     */
    public String getNewContext() {
        return newHtmlContext;
    }

    /**
     * Obtains the proxedHost global Attribute
     *
     * @return the current value of proxedHost global attribute
     */
    public String getProxedHost() {
        return proxedHost;
    }

    /**
     * Obtains the synonimProxedHost global Attribute
     *
     * @return the current value of synonimProxedHost global attribute
     */
    public String getSynonimProxedHost() {
        return synonimProxedHost;
    }

    /**
     * Obtains the proxedBasePath global Attribute
     *
     * @return the current value of proxedBasePath global attribute
     */
    public String getProxedBasePath() {
        return proxedBasePath;
    }

    /*
     * Methods to get the self explained properties
     */
    public String getNewHtmlContext() {
        return newHtmlContext;
    }

    public String getNewCssContext() {
        return newCssContext;
    }

    public String getNewJsContextQuote() {
        return newJsContextQuote;
    }

    public String getNewJsContextApos() {
        return newJsContextApos;
    }


    /*
     * Methods to obtain Matchers from compiled patterns
     */
    public Matcher getCssProxedHostMatcher(String content) {
        return cssProxedHostPattern.matcher(content);
    }

    public Matcher getCssProxedSynonimHostMatcher(String content) {
        return cssProxedSynonimHostPattern.matcher(content);
    }

    public Matcher getCssProxedBasePathMatcher(String content) {
        return cssProxedBasePathPattern.matcher(content);
    }

    public Matcher getUrlProxedHostMatcher(String theUrl) {
        return urlProxedHostPattern.matcher(theUrl);
    }

    public Matcher getUrlProxedSynonimHostMatcher(String theUrl) {
        return urlProxedSynonimHostPattern.matcher(theUrl);
    }

    public Matcher getUrlProxedBasePathMatcher(String theUrl) {
        return urlProxedBasePathPattern.matcher(theUrl);
    }

    public Matcher getJsProxedHostQuoteMatcher(String content) {
        return jsProxedHostQuotePattern.matcher(content);
    }

    public Matcher getJsProxedSynonimHostQuoteMatcher(String content) {
        return jsProxedSynonimHostQuotePattern.matcher(content);
    }

    public Matcher getJsProxedBasePathQuoteMatcher(String content) {
        return jsProxedBasePathQuotePattern.matcher(content);
    }

    public Matcher getJsProxedHostAposMatcher(String content) {
        return jsProxedHostAposPattern.matcher(content);
    }

    public Matcher getJsProxedSynonimHostAposMatcher(String content) {
        return jsProxedSynonimHostAposPattern.matcher(content);
    }

    public Matcher getJsProxedBasePathAposMatcher(String content) {
        return jsProxedBasePathAposPattern.matcher(content);
    }

    //Constant variables (doesn't change the value after the initialization)
    private String proxedHost = null;
    private String synonimProxedHost = null;
    private String proxedBasePath = null;

    //New Contexts
    private String newHtmlContext = null;
    private String newCssContext = null;
    private String newJsContextQuote = null;
    private String newJsContextApos = null;

    //Parsing Patterns
    private Pattern cssProxedHostPattern = null;
    private Pattern cssProxedSynonimHostPattern = null;
    private Pattern cssProxedBasePathPattern = null;

    private Pattern urlProxedHostPattern = null;
    private Pattern urlProxedSynonimHostPattern = null;
    private Pattern urlProxedBasePathPattern = null;

    private Pattern jsProxedHostQuotePattern = null;
    private Pattern jsProxedSynonimHostQuotePattern = null;
    private Pattern jsProxedBasePathQuotePattern = null;
    private Pattern jsProxedHostAposPattern = null;
    private Pattern jsProxedSynonimHostAposPattern = null;
    private Pattern jsProxedBasePathAposPattern = null;
}
