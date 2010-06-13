package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;
import org.homelinux.nx01.proxyma.beans.ResponseResourceBean;
import org.homelinux.nx01.proxyma.beans.RuleBean;
import org.homelinux.nx01.proxyma.cache.SmartBuffer;
import org.homelinux.nx01.proxyma.cache.SmartBufferReader;
import org.htmlparser.Attribute;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

/**
 * <p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 14.26.32
 * </p><p>
 * This class parses the html and rewites the URL that belongs to the reverse proxy site.
 * It uses the htmlparser library to parse the page. If required it parse any inline css and/or js in the page.
 * Please note that the code of this class is implemented with speed and memory safe in mind.. not for elegance.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * <p>
 * TODO: Make a better implementation of the css and javascript rewriting methods.
 * TODO: for example in this script <document.cookie = name + "=" + value + ";path=/;domain=.google.com";>
 * TODO: the domain of the cookie is not rewritten.
 * TODO: Javascript generated cookies does not work.
 * </p>
 */
public class RewriterEngine {
    /**
     * Rewriting engine core function. It visits the whole html into the passed resource with
     * the htmlparser library and rewrites the links on it.
     *
     * @param rule     is the current rule for the proxy
     * @param ruleSet  is the rulset for the current proxyma instance
     * @param responseResource is the wrapped original response of the remote server.
     * @throws ParserException
     */
    public void rewriteLinks(RuleBean rule, RuleSet ruleSet, ResponseResourceBean responseResource) throws ParserException, IOException {
        /**
         * Inner Class for the html analisys.
         */
        final NodeVisitor linkVisitor = new NodeVisitor() {

            public void visitTag(Tag tag) {
                String name = tag.getTagName();
                String tagValue = null;

                //selects the appropriate action based upon the tag and the attribute types
                //NOTE: probably this method will be improoved in the future because it doesn't handles
                //      all the Javascript events. I have also found some problem in the htmlparser
                //      library with pages that uses lot of javascript.
                if (RewriterConstants.A.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.HREF);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.HREF, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.IMG.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.SRC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.SRC, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.ISMAP);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.ISMAP, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.USEMAP);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.USEMAP, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.LONGDESC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.LONGDESC, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.LINK.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.HREF);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.HREF, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.FORM.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.ACTION);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.ACTION, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.INPUT.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.SRC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.SRC, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.TD.equalsIgnoreCase(name)) {
                    //NOTE: This is a NON-Standard attribute for this TAG but MSIE, Netscape and Firefox supports it.
                    //..so I added this statements.
                    tagValue = tag.getAttribute(RewriterConstants.BACKGROUND);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.BACKGROUND, rewriteUrl(tagValue));
                } else if (RewriterConstants.TABLE.equalsIgnoreCase(name)) {
                    //NOTE: This is a NON-Standard attribute for this TAG but MSIE, Netscape and Firefox supports it.
                    //..so I added this statements.
                    tagValue = tag.getAttribute(RewriterConstants.BACKGROUND);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.BACKGROUND, rewriteUrl(tagValue));

                } else if (RewriterConstants.SCRIPT.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.SRC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.SRC, rewriteUrl(tagValue));
                    else {
                        String Language = ((ScriptTag) tag).getLanguage();
                        if ((Language != null) && (Language.indexOf(RewriterConstants.JAVASCRIPT_LIBRARY) >= 0))
                            ((ScriptTag) tag).setScriptCode(rewriteJavaScriptUrls(((ScriptTag) tag).getScriptCode()));
                    }
                } else if (RewriterConstants.STYLE.equalsIgnoreCase(name)) {
                    tagValue = ((StyleTag) tag).getStyleCode();
                    if (tagValue != null) {
                        TextNode data = (TextNode) tag.getFirstChild();
                        data.setText(rewriteStyleLinks(tagValue));
                    }
                } else if (RewriterConstants.BODY.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.ONLOAD);
                    if (tagValue != null) {
                        tag.removeAttribute(RewriterConstants.ONLOAD);
                        Attribute attribute = new Attribute();
                        attribute.setName(RewriterConstants.ONLOAD);
                        attribute.setAssignment(RewriterConstants.EQ);
                        attribute.setRawValue(RewriterConstants.QUOT + rewriteJavaScriptUrls(tagValue) + RewriterConstants.QUOT);
                        tag.setAttributeEx(attribute);
                    }
                    tagValue = tag.getAttribute(RewriterConstants.BACKGROUND);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.BACKGROUND, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.BASE.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.HREF);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.HREF, rewriteUrl(tagValue));
                } else if (RewriterConstants.FRAME.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.SRC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.SRC, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.LONGDESC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.LONGDESC, rewriteUrl(tagValue));
                } else if (RewriterConstants.IFRAME.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.SRC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.SRC, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.LONGDESC);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.LONGDESC, rewriteUrl(tagValue));
                } else if (RewriterConstants.APPLET.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.ARCHIVE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.ARCHIVE, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.CODE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.CODE, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.CODEBASE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.CODEBASE, rewriteUrl(tagValue));
                } else if (RewriterConstants.OBJECT.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.ARCHIVE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.ARCHIVE, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.CODEBASE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.CODEBASE, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.DATA);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.DATA, rewriteUrl(tagValue));
                    tagValue = tag.getAttribute(RewriterConstants.USEMAP);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.USEMAP, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.AREA.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.HREF);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.HREF, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.DEL.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.CITE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.CITE, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                } else if (RewriterConstants.INS.equalsIgnoreCase(name)) {
                    tagValue = tag.getAttribute(RewriterConstants.CITE);
                    if (tagValue != null)
                        tag.setAttribute(RewriterConstants.CITE, rewriteUrl(tagValue));
                    rewriteJsEvents(tag);
                }
            }
        };

        //Set current rule for rewriting
        setCurrentRule(rule);
        setCurrentRuleSet(ruleSet);

        //Generates a parser for the given page
        SmartBufferReader reader = new SmartBufferReader(responseResource.getData());
        String content = new String(reader.getBytes(), responseResource.getEncodingCharset());
        Parser parser = new Parser(new Lexer(new Page(content, responseResource.getEncodingCharset())));

        //Generate a linkvisitor for the url rewriting
        NodeList myPage = parser.parse(null);
        myPage.visitAllNodesWith(linkVisitor);

        //Add to the response the rewritten data
        byte[] rewrittenContent = myPage.toHtml(true).getBytes();
        SmartBuffer rewrittenData = new SmartBuffer(ProxymaConstants.MAX_SIZE_OF_RAMBUFFER);
        rewrittenData.append(rewrittenContent, rewrittenContent.length);
        responseResource.setData(rewrittenData);
    }

    /**
     * This method sets the rule to use for rewrite the urls.
     *
     * @param rule the new rule
     */
    private void setCurrentRule(RuleBean rule) {
        currentRule = rule;
    }

    /**
     * This method sets the ruleSet to use for rewrite the urls.
     * This is the same forever, so it works only the first time.
     *
     * @param ruleSet the new ruleSet
     */
    private void setCurrentRuleSet(RuleSet ruleSet) {
        if (currentRuleSet == null)
            currentRuleSet = ruleSet;
    }

    /**
     * This method is very silly and i will improve it as soon as I can..
     * Anyway, it works exactly how i need.. for now.
     * NOTE that this method can't rewrite urls that belongs to other rules.
     *
     * @param javascriptContent countains the javascript code to parse
     * @return the same code with any proxy-related link rewritten
     */
    private String rewriteJavaScriptUrls(String javascriptContent) {
        if (currentRule.getJavascriptHandlingMode() == RewriterConstants.PROCESS) {
            RewriterPatterns rewriterPatterns = currentRule.getParserPatterns();

            //Executes some simple substitutions in the passed js using the rewriterPatterns.
            Matcher basePathMatcher = rewriterPatterns.getJsProxedBasePathQuoteMatcher(javascriptContent);
            String tempValue1 = basePathMatcher.replaceAll(rewriterPatterns.getNewJsContextQuote());
            Matcher hostMatcher = rewriterPatterns.getJsProxedHostQuoteMatcher(tempValue1);
            String tempValue2 = hostMatcher.replaceAll(rewriterPatterns.getNewJsContextQuote());

            basePathMatcher = rewriterPatterns.getJsProxedBasePathAposMatcher(tempValue2);
            tempValue1 = basePathMatcher.replaceAll(rewriterPatterns.getNewJsContextApos());
            hostMatcher = rewriterPatterns.getJsProxedHostAposMatcher(tempValue1);

            // handles synonims for the urls.
            if (rewriterPatterns.getSynonimProxedHost() != null) {
                tempValue2 = hostMatcher.replaceAll(rewriterPatterns.getNewJsContextApos());
                Matcher synonimHostMatcher = rewriterPatterns.getJsProxedSynonimHostQuoteMatcher(tempValue2);
                tempValue1 = synonimHostMatcher.replaceAll(rewriterPatterns.getNewJsContextQuote());
                synonimHostMatcher = rewriterPatterns.getJsProxedSynonimHostAposMatcher(tempValue1);
                return synonimHostMatcher.replaceAll(rewriterPatterns.getNewJsContextApos());
            } else {
                return hostMatcher.replaceAll(rewriterPatterns.getNewJsContextApos());
            }
        } else
            return RewriterConstants.EMPTY_STRING;
    }

    /**
     * This method is very silly and i will improve it as soon as I can..
     * Anyway, it works exactly how i need.. for now.
     * It sets the current rule for rewriting and then calls the
     * method that rewrites the url.
     *
     * @param rule              the rule to use for the rewriting
     * @param ruleSet  is the rulset for the current proxyma instance
     * @param responseResource countains the javascript code to parse
     */
    public void rewriteJavaScriptUrls(RuleBean rule, RuleSet ruleSet, ResponseResourceBean responseResource) throws IOException {
        setCurrentRule(rule);
        setCurrentRuleSet(ruleSet);

        SmartBufferReader reader = new SmartBufferReader(responseResource.getData());
        String jsContent = new String(reader.getBytes(), responseResource.getEncodingCharset());
        byte[] rewrittenContent = rewriteJavaScriptUrls(jsContent).getBytes();
        SmartBuffer rewrittenData = new SmartBuffer(ProxymaConstants.MAX_SIZE_OF_RAMBUFFER);
        rewrittenData.append(rewrittenContent, rewrittenContent.length);
        responseResource.setData(rewrittenData);
    }

    /**
     * This simple method searches in the sylesheet any url that
     * should be rewritten.. and rewrites it.
     * NOTE that this method can't rewrite urls that belongs to other rules.
     *
     * @param styleContent countains the css code to parse
     * @return the same code with any proxy-related link rewritten
     */
    private String rewriteStyleLinks(String styleContent) {
        RewriterPatterns rewriterPatterns = currentRule.getParserPatterns();

        //Executes some simple substitutions in the passed css data using the rewriterPatterns.
        Matcher basePathMatcher = rewriterPatterns.getCssProxedBasePathMatcher(styleContent);
        String tempValue1 = basePathMatcher.replaceAll(rewriterPatterns.getNewCssContext());
        Matcher hostMatcher = rewriterPatterns.getCssProxedHostMatcher(tempValue1);

        //handles synonims for the urls
        if (rewriterPatterns.getSynonimProxedHost() != null) {
            String tempValue2 = hostMatcher.replaceAll(rewriterPatterns.getNewCssContext());
            Matcher synonimHostMatcher = rewriterPatterns.getCssProxedSynonimHostMatcher(tempValue2);
            return synonimHostMatcher.replaceAll(rewriterPatterns.getNewCssContext());
        } else {
            return hostMatcher.replaceAll(rewriterPatterns.getNewCssContext());
        }
    }

    /**
     * This simple method searches in the sylesheet any url that
     * should be rewritten.. and rewrites it.
     * It sets the current rule for rewriting and then calls the
     * method that rewrites the url.
     * At the end it set the new content data of the Resource Buffer.
     *
     * @param responseResource is the remoter server response thet countains the css
     * @param ruleSet  is the rulset for the current proxyma instance
     * @param rule         the rule to use for the rewriting
     */
    public void rewriteStyleLinks(RuleBean rule, RuleSet ruleSet, ResponseResourceBean responseResource) throws IOException {
        setCurrentRule(rule);
        setCurrentRuleSet(ruleSet);
        SmartBufferReader reader = new SmartBufferReader(responseResource.getData());
        String styleContent = new String(reader.getBytes(), responseResource.getEncodingCharset());
        byte[] rewrittenContent = rewriteStyleLinks(styleContent).getBytes();
        SmartBuffer rewrittenData = new SmartBuffer(ProxymaConstants.MAX_SIZE_OF_RAMBUFFER);
        rewrittenData.append(rewrittenContent, rewrittenContent.length);
        responseResource.setData(rewrittenData);
    }

    /**
     * This method checks if the given url has to be rewritten..
     * and rewrites it if needed.
     * NOTE that this is the only method that can rewrite urls that belongs to other rules.
     *
     * @param theUrl to check for rewriting
     * @return the same same url or the equivalent rewritten url.
     */
    private String rewriteUrl(String theUrl) {
        String newUrl = theUrl;
        RewriterPatterns rewriterPatterns = currentRule.getParserPatterns();

        //check the passed url and rewites it if needed
        if (theUrl.startsWith(rewriterPatterns.getProxedBasePath())) {
            Matcher urlMatecher = rewriterPatterns.getUrlProxedBasePathMatcher(theUrl);
            newUrl = urlMatecher.replaceFirst(rewriterPatterns.getNewHtmlContext());
        } else if (theUrl.startsWith(rewriterPatterns.getProxedHost())) {
            Matcher urlMatecher = rewriterPatterns.getUrlProxedHostMatcher(theUrl);
            newUrl = urlMatecher.replaceFirst(rewriterPatterns.getNewHtmlContext());
        } else if ((rewriterPatterns.getSynonimProxedHost() != null) &&
                (theUrl.startsWith(rewriterPatterns.getSynonimProxedHost()))) {
            // handles synonim for host+port
            Matcher urlMatecher = rewriterPatterns.getUrlProxedSynonimHostMatcher(theUrl);
            newUrl = urlMatecher.replaceFirst(rewriterPatterns.getNewHtmlContext());
        } else if ((theUrl.startsWith(RewriterConstants.HTTP_URL_PREFIX)) || (theUrl.startsWith(RewriterConstants.HTTPS_URL_PREFIX))) {
            //no matches for the current rule, now we search for a rule that matches
            //the proxed host url..
            String protocol = null;
            if (theUrl.startsWith(RewriterConstants.HTTP_URL_PREFIX))
                protocol = RewriterConstants.HTTP_URL_PREFIX;
            else
                protocol = RewriterConstants.HTTPS_URL_PREFIX;

            //allocate an instance of the subEngine if it doesn't exists.
            if (subEngine == null)
                subEngine = new RewriterEngine();

            //Split the path and try to find a rule that matches the proxed host
            String[] urlPath = theUrl.replaceFirst(protocol, ProxymaConstants.EMPTY_STRING).split(ProxymaConstants.PATH_SEPARATOR);
            String searchString = protocol;
            for (int i = 0; i < urlPath.length; i++) {
                //Here we try some different ways to ask to the rulSet for a matching rule.
                //This is better than a full search into the collection of rules because its
                //running time depends only by the url length. (so it doesn't depend to the number of the rules)
                searchString += urlPath[i] + ProxymaConstants.PATH_SEPARATOR;
                RuleBean foundRule = currentRuleSet.getRuleByProxyPassHost(searchString);
                if (foundRule != null) {
                    //Founded a Matching Rule!

                    //Try to understand if the rule is incomplete (has the newContext == null)
                    if (foundRule.getNewContext() == null) {
                        //the rule is incomplete, complete it with the default Context.
                        foundRule.setNewContext(currentRuleSet.getProxymaStandardContext() + foundRule.getProxyFolder() + ProxymaConstants.PATH_SEPARATOR);
                    }

                    //If the rule was found, make me sure thei it will match in the subEngine
                    //This test avoid orrible stack overflows =:-O
                    if (theUrl.startsWith(searchString))
                        newUrl = subEngine.rewriteUrl(theUrl, foundRule, currentRuleSet);
                    else
                        newUrl = subEngine.rewriteUrl(theUrl + ProxymaConstants.PATH_SEPARATOR, foundRule, currentRuleSet);
                    break;
                }
            }
        }

        // if any debug mode is enabled append the url to the rewritten liso or to the NON rewritten list.
        if (currentRule.isDebugLoggingEnabled()) {
            if (newUrl.equals(theUrl))
                nonRewrittenUrls.append(theUrl + "\n");
            else
                rewrittenUrls.append(theUrl + " --> " + newUrl + "\n");
        }
        return newUrl;
    }

    /**
     * This method checks if the given url has to be rewritten..
     * and rewrites it if needed.
     * It sets the current rule for rewriting and then calls the
     * method that rewrites the url.
     *
     * @param theUrl to check for rewriting
     * @param rule   the rule to use for the rewriting
     * @return the same same url or the equivalent rewritten url.
     */
    public String rewriteUrl(String theUrl, RuleBean rule, RuleSet ruleSet) {
        setCurrentRule(rule);
        setCurrentRuleSet(ruleSet);
        return rewriteUrl(theUrl);
    }


    /**
     * Rewrites urls into common javascript events
     *
     * @param tag the tag to serach for events..
     */
    private void rewriteJsEvents(Tag tag) {
        for (int i = 0; i < RewriterConstants.EVENTS.length; i++) {
            String tagValue = tag.getAttribute(RewriterConstants.EVENTS[i]);
            if (tagValue != null) {
                tag.removeAttribute(RewriterConstants.EVENTS[i]);
                Attribute attribute = new Attribute();
                attribute.setName(RewriterConstants.EVENTS[i]);
                attribute.setAssignment(RewriterConstants.EQ);
                attribute.setRawValue(RewriterConstants.QUOT + rewriteJavaScriptUrls(tagValue) + RewriterConstants.QUOT);
                tag.setAttributeEx(attribute);
            }
        }
    }

     /**
     * Rewrites the path and the domain of the cookies for the remote host
     *
     * @param request    the request bean of the current client request
     * @param activeRule the rule bean of the active Rule
     */
    public List rewriteCookies(ClientRequestBean request, RuleBean activeRule) {
        if (request.getCookies().size() > 0) {
            ArrayList retValue = new ArrayList();
            Cookie cookie;
            StringBuffer currCookie = new StringBuffer();
            Iterator iter = request.getCookies().iterator();
            while (iter.hasNext()) {
                cookie = (Cookie) iter.next();

                //filter out the local Jsessionid Cookie.
                if (!(ProxymaConstants.JSESSIONID_COOKIE.equalsIgnoreCase(cookie.getName()) && (cookie.getPath() == null))) {
                    currCookie.append(cookie.getName());
                    currCookie.append(ProxymaConstants.COOKIE_VALUE_DELIMITER);
                    currCookie.append(cookie.getValue());

                    String strVal;
                    int intVal = cookie.getMaxAge();
                    if (intVal > 0) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat(ProxymaConstants.DATE_FORMAT);
                        long exp = new Date().getTime() + (intVal * 1000);
                        strVal = dateFormat.format(new Date(exp));
                        currCookie.append(ProxymaConstants.COOKIE_TOKENS_DELIMITER);
                        currCookie.append(ProxymaConstants.COOKIE_EXPIRES);
                        currCookie.append(strVal);
                    }
                    strVal = cookie.getPath();
                    if (strVal != null) {
                        currCookie.append(ProxymaConstants.COOKIE_TOKENS_DELIMITER);
                        currCookie.append(ProxymaConstants.COOKIE_PATH);
                        currCookie.append(strVal.replaceFirst(activeRule.getNewContext(), "/"));
                    }
                    strVal = cookie.getDomain();
                    if (strVal != null) {
                        currCookie.append(ProxymaConstants.COOKIE_TOKENS_DELIMITER);
                        currCookie.append(ProxymaConstants.COOKIE_DOMAIN);
                        currCookie.append(activeRule.getRuleDomain());
                    }

                    if (cookie.getSecure()) {
                        currCookie.append(ProxymaConstants.COOKIE_TOKENS_DELIMITER);
                        currCookie.append(ProxymaConstants.COOKIE_SECURE);
                    }

                    intVal = cookie.getVersion();
                    if (intVal > 1) {
                        currCookie.append(ProxymaConstants.COOKIE_TOKENS_DELIMITER);
                        currCookie.append(ProxymaConstants.COOKIE_VERSION);
                        currCookie.append(Integer.toString(intVal));
                    }
                    retValue.add(currCookie.toString());
                }
            }

            return retValue;
        } else {
            return request.getCookies();
        }
    }

    /**
     * If debug mode is true, the rewrittenUrls (StringBuffer) countains the
     * details of all the html urls that have been rewritten.
     *
     * @return all rewritten urls in the html (not in the css or js)
     */
    public String getRewrittenUrls() {
        String retValue = rewrittenUrls.toString();
        rewrittenUrls = new StringBuffer();
        return retValue;
    }

    /**
     * If debug mode is true, the nonRewrittenUrls (StringBuffer) countains the
     * details of all the html urls that were left untouched.
     *
     * @return all the urls that were left untouched
     */
    public String getNonRewrittenUrls() {
        String retValue = nonRewrittenUrls.toString();
        nonRewrittenUrls = new StringBuffer();
        return retValue;
    }

    //Secondary Rewriter Engine to parse urls by Proxed Host.
    RewriterEngine subEngine = null;

    //Debug Variables
    private StringBuffer rewrittenUrls = new StringBuffer();
    private StringBuffer nonRewrittenUrls = new StringBuffer();

    //Temporary variables.. these can change every time the rewriter is called.
    private RuleBean currentRule = null;
    private RuleSet currentRuleSet = null;
}
