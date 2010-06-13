package org.homelinux.nx01.proxyma;

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.ProxymaRuleSet;
import org.homelinux.nx01.proxyma.core.ProxymaRuleSetsPool;
import org.homelinux.nx01.proxyma.core.RequestForwarder;
import org.homelinux.nx01.proxyma.debug.ProxyRequestDumper;
import org.homelinux.nx01.proxyma.debug.RequestDumper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 * </p><p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 12.06.36
 * </p><p>
 * This class is the main servlet of the proxyma project.
 * It implements a lightweight reverse-proxy with basic url-rewriting capabilities.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 */
public class HttpReverseProxyServlet extends HttpServlet {

    /**
     * This method retrives configuration parameters from the web.xml and
     * initialize the private members of the servlet.
     */
    public void init() {
        String proxyHost = null;
        String proxyPort = null;

        // try to understand if a proxy is required for outgoing connections
        proxyHost = getInitParameter(ProxymaConstants.proxyHost);
        proxyPort = getInitParameter(ProxymaConstants.proxyPort);

        if ((proxyHost != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyHost.trim()))) &&
                (proxyPort != null) && (!(ProxymaConstants.EMPTY_STRING.equals(proxyPort.trim())))) {
            System.getProperties().put("proxySet", ProxymaConstants.TRUE);
            System.getProperties().put("proxyHost", proxyHost.trim());
            System.getProperties().put("proxyPort", proxyPort.trim());
        }

        // check if should show the masqueraded resources on incomplete requests
        String showMasqueradedResources = getInitParameter(ProxymaConstants.showMasqueradedResources);
        if ((showMasqueradedResources == null) || (ProxymaConstants.EMPTY_STRING.equals(showMasqueradedResources.trim()))) {
            this.showMasqueradedResources = ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES;
        } else {
            if (ProxymaConstants.TRUE.equalsIgnoreCase(showMasqueradedResources.trim()))
                this.showMasqueradedResources = true;
            else
                this.showMasqueradedResources = false;
        }

        // Allocate the RequestForwarder Class for this servvlet
        reqForwarder = new RequestForwarder();
    }

    /**
     * This method handles the GET requests.
     *
     * @param httpservletrequest  the requrest..
     * @param httpservletresponse the response..
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException {

        //regexp that matches the basepath of proxyma
        Pattern pattern = getServletPathMatchPattern(httpservletrequest.getServletPath());
        Matcher matcher = pattern.matcher(httpservletrequest.getRequestURI());

        //searching for proxyma base url.
        matcher.find();

        //This is the proxyma base path url (without trailing "/")
        //NOTE: Here an exception is trown if the path is the same as the basepath and did not end with a "/"
        String proxymaBasePath = null;
        try {
            proxymaBasePath = matcher.group();
        } catch (IllegalStateException e) {
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Reusing the regexp
        matcher.reset();

        //If the servlet is not yet initialized, obtain the global Ruleset for thit instance of proxyma.
        if (ruleSet == null) {
            String removeFromRegexp = httpservletrequest.getServletPath() + ".*$";
            ProxymaRuleSetsPool ruleSetsPool = ProxymaRuleSetsPool.getInstance();
            ruleSet = ruleSetsPool.getRuleSet(proxymaBasePath.replaceFirst(removeFromRegexp, ProxymaConstants.EMPTY_STRING));

            //Set the standard Context of the HttpReverseProxyServlet for this ruleSet.
            //This statement is important for the rewriters to rewrite links that belongs
            //to other rules.
            ruleSet.setProxymaStandardContext(proxymaBasePath);
        }

        //this is the url without the basepath of proxyma (/ or /x/y.. without trailing "/")
        String proxymaRelativeUrl = matcher.replaceFirst(ProxymaConstants.EMPTY_STRING);

        //Processing url relative to proxyma..
        String[] splittedPath = proxymaRelativeUrl.split(ProxymaConstants.PATH_SEPARATOR, 2);

        //The url shouldn't be empty (at least a proxyFolder must be specified)
        //and it must countain at least slash (so if only the folder is specified it must end with "/")
        if (splittedPath.length < 2) {
            //if configured, show to the user the current availables resources
            if ((showMasqueradedResources) && (splittedPath[0].length() == 0))
                showRules(ruleSet.getRulesAsCollection().iterator(), httpservletresponse);
            else
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Search for a rule that matches current context.. if it's not foud an error is sent back.
        ProxymaRuleBean activeRule = (ProxymaRuleBean) ruleSet.getRule(splittedPath[0]);
        if (activeRule == null) {
            httpservletresponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //this is the url to retrive with the requestForwarder
        StringBuffer proxyRequestUrl = new StringBuffer();
        proxyRequestUrl.append(activeRule.getProxyPassHost()).append(splittedPath[1]);


        //get the query string
        if (httpservletrequest.getQueryString() != null) {
            proxyRequestUrl.append(ProxymaConstants.QUERYSTRING_SEPARATOR);
            proxyRequestUrl.append(httpservletrequest.getQueryString());
        }

        // this call works only the first time for any rule. For further calls it will be ignored.
        // It sets the constant attribute for the new context of the RewriterEngine for the current rule
        activeRule.getParserPatterns().setNewContext(new StringBuffer().append(proxymaBasePath).append(splittedPath[0]).append(ProxymaConstants.PATH_SEPARATOR).toString());

        //If the rule is disabled exit with forbidden error
        if (!(activeRule.isRuleEnabled())) {
            httpservletresponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        //retrive the request headers.
        Hashtable requestHeaders = new Hashtable();
        getHeaders(httpservletrequest, requestHeaders);

        //if the debug mode is enabled send to the client response the request parameters.
        if (activeRule.isDebugModeEnabled()) {
            RequestDumper rd = new RequestDumper();
            rd.dumpGet(getServletContext(), httpservletrequest, httpservletresponse);

            ProxyRequestDumper prd = new ProxyRequestDumper();
            prd.dumpRequest(requestHeaders, null, proxyRequestUrl.toString(), proxymaBasePath, httpservletresponse);
        }

        //Do the dirty job..
        int returnCode = reqForwarder.doAction(httpservletrequest.getMethod().toUpperCase(), proxyRequestUrl.toString(), null, requestHeaders, httpservletresponse, activeRule, ruleSet);
        if (returnCode >= ProxymaConstants.SERVER_ERROR_400)
            httpservletresponse.sendError(returnCode);
    }

    /**
     * This method handles the POST requests.
     *
     * @param httpservletrequest  the request..
     * @param httpservletresponse the response..
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
            throws ServletException, IOException {

        //regexp that matches the basepath of proxyma
        Pattern pattern = getServletPathMatchPattern(httpservletrequest.getServletPath());
        Matcher matcher = pattern.matcher(httpservletrequest.getRequestURI());

        //searching for proxyma base url.
        matcher.find();

        //This is the proxyma base path url (without trailing "/")
        //NOTE: Here an exception is trown if the path is the same as the basepath and did not end with a "/"
        String proxymaBasePath = null;
        try {
            proxymaBasePath = matcher.group();
        } catch (IllegalStateException e) {
            httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Reusing the regexp
        matcher.reset();

        //If the servlet is not yet initialized, obtain the global Ruleset for thit instance of proxyma.
        if (ruleSet == null) {
            String removeFromRegexp = httpservletrequest.getServletPath() + ".*$";
            ProxymaRuleSetsPool ruleSetsPool = ProxymaRuleSetsPool.getInstance();
            ruleSet = ruleSetsPool.getRuleSet(proxymaBasePath.replaceFirst(removeFromRegexp, ProxymaConstants.EMPTY_STRING));

            //Set the standard Context of the HttpReverseProxyServlet for this ruleSet.
            //This statement is important for the rewriters to rewrite links that belongs
            //to other rules.
            ruleSet.setProxymaStandardContext(proxymaBasePath);
        }

        //this is the url without the basepath of proxyma (/ or /x/y.. without trailing "/")
        String proxymaRelativeUrl = matcher.replaceFirst(ProxymaConstants.EMPTY_STRING);

        //Processing url relative to proxyma..
        String[] splittedPath = proxymaRelativeUrl.split(ProxymaConstants.PATH_SEPARATOR, 2);

        //The url shouldn't be empty (at least a proxyFolder must be specified)
        //and it must countain at least slash (so if only the folder is specified it must end with "/")
        if (splittedPath.length < 2) {
            //if configured, show to the user the current availables resources
            if ((showMasqueradedResources) && (splittedPath[0].length() == 0))
                showRules(ruleSet.getRulesAsCollection().iterator(), httpservletresponse);
            else
                httpservletresponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Search for a rule that matches current context.. if it's not foud an error is sent back.
        ProxymaRuleBean activeRule = (ProxymaRuleBean) ruleSet.getRule(splittedPath[0]);
        if (activeRule == null) {
            httpservletresponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //this is the url to retrive with the requestForwarder
        StringBuffer proxyRequestUrl = new StringBuffer();
        proxyRequestUrl.append(activeRule.getProxyPassHost()).append(splittedPath[1]);


        //get the query string
        if (httpservletrequest.getQueryString() != null) {
            proxyRequestUrl.append(ProxymaConstants.QUERYSTRING_SEPARATOR);
            proxyRequestUrl.append(httpservletrequest.getQueryString());
        }

        // this call works only the first time for any rule. For further calls it will be ignored.
        // It sets the constant attribute for the new context of the RewriterEngine for the current rule
        activeRule.getParserPatterns().setNewContext(new StringBuffer().append(proxymaBasePath).append(splittedPath[0]).append(ProxymaConstants.PATH_SEPARATOR).toString());

        //If the rule is disabled exit with forbidden error
        if (!(activeRule.isRuleEnabled())) {
            httpservletresponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        //retrive the request headers.
        Hashtable requestHeaders = new Hashtable();
        getHeaders(httpservletrequest, requestHeaders);

        //Get Post Data:
        StringBuffer postData = getPostData(httpservletrequest, activeRule);

        //if the debug mode is enabled send to the client response the request parameters.
        if (activeRule.isDebugModeEnabled()) {
            RequestDumper rd = new RequestDumper();
            rd.dumpGet(getServletContext(), httpservletrequest, httpservletresponse);

            ProxyRequestDumper prd = new ProxyRequestDumper();
            prd.dumpRequest(requestHeaders, postData, proxyRequestUrl.toString(), proxymaBasePath, httpservletresponse);
        }

        //Do the dirty job..
        int returnCode = reqForwarder.doAction(httpservletrequest.getMethod().toUpperCase(), proxyRequestUrl.toString(), postData, requestHeaders, httpservletresponse, activeRule, ruleSet);
        if (returnCode >= ProxymaConstants.SERVER_ERROR_400)
            httpservletresponse.sendError(returnCode);
    }


    /**
     * Get the request headers and sotres them into an HashTable.
     * note: it removes the "host" header.
     *
     * @param httpservletrequest the requrest
     * @param requestHeaders     the hastable with the stored headers.
     */
    private void getHeaders(HttpServletRequest httpservletrequest, Hashtable requestHeaders) {
        String s;
        for (Enumeration enumeration = httpservletrequest.getHeaderNames(); enumeration.hasMoreElements(); requestHeaders.put(s, httpservletrequest.getHeader(s)))
            s = (String) enumeration.nextElement();

        //removing "host" header
        requestHeaders.remove(ProxymaConstants.HOST_HEADER);
    }

    /**
     * Read the post data of a POST request and stores it into a StringBuffer.
     *
     * @param req the post request
     * @return a StringBuffer with the post data.
     * @throws IOException
     */
    private StringBuffer getPostData(HttpServletRequest req, ProxymaRuleBean currentRule) throws IOException {
        StringBuffer retValue = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        char[] buffer = new char[ProxymaConstants.BUFSIZE];
        int count = 0;
        int postSize = 0; //size of the POST data
        while ((count = reader.read(buffer, 0, ProxymaConstants.BUFSIZE)) > -1) {
            retValue.append(buffer, 0, count);
            postSize += count;
            if ((postSize > currentRule.getMaxPostSize()) && (currentRule.getMaxPostSize() == 0)) {
                System.out.println("WARNING! POST data override the maximum allowed size (" + currentRule.getMaxPostSize() + " bytes).");
                retValue = new StringBuffer(ProxymaConstants.EMPTY_STRING);
                break;
            }
        }
        return retValue;
    }

    /**
     * Initializes the Pattern for the matching of the contextPath of proxyma.
     * It is used to rebuild the original url of the requested resource.
     *
     * @param servletPath the current path of the HttpRewriterProxy servlet.
     * @return a regexp pattern.
     */
    private Pattern getServletPathMatchPattern(String servletPath) {
        if (servletPathMatchPattern == null)
            servletPathMatchPattern = Pattern.compile(new StringBuffer().append("^.*").append(servletPath).append(ProxymaConstants.PATH_SEPARATOR).toString());
        return servletPathMatchPattern;
    }

    /**
     * This method prints out a plain simple html page that lists the available rules
     * for the clients.
     * It is called if the request is incomplete and the appropriate servlet configuration
     * parameter is set.
     *
     * @param iter     an iterator that countains the curren set of rules
     * @param response the httpresponse to use to send the page to the client.
     */
    private void showRules(Iterator iter, HttpServletResponse response) {

        //Prepare the Buffered Output Stream and writes the page.
        try {
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());

            //write the header of the page
            out.write(ProxymaConstants.html_head_template.getBytes());

            //iterate the rules
            boolean even = true;
            while (iter.hasNext()) {
                ProxymaRuleBean ruleBean = (ProxymaRuleBean) iter.next();
                String ruleRow = ProxymaConstants.html_resource_row_template.replaceAll(ProxymaConstants.proxyFolderPlaceHolder, ruleBean.getProxyFolder());
                String tmpString = ruleRow.replaceAll(ProxymaConstants.proxyPassHostPlaceHolder, ruleBean.getProxyPassHost());
                ruleRow = tmpString.replaceFirst(ProxymaConstants.statusPlaceHolder, ruleBean.isRuleEnabled() ? "Active" : "Locked");
                tmpString = ruleRow.replaceFirst(ProxymaConstants.statusColorPlaceHolder, ruleBean.isRuleEnabled() ? "black" : "red");
                ruleRow = tmpString.replaceFirst(ProxymaConstants.bgcolorPlaceHolder, even ? ProxymaConstants.evenBgcolor : ProxymaConstants.oddBgcolor);
                even = !even;
                out.write(ruleRow.getBytes());
            }

            //write the footer of the page
            out.write(ProxymaConstants.html_tail_template.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    //Servlet Private attributes
    private Pattern servletPathMatchPattern = null;
    private ProxymaRuleSet ruleSet = null;
    private RequestForwarder reqForwarder = null;
    private boolean showMasqueradedResources = ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES;
}
