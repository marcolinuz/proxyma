package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;
import org.homelinux.nx01.proxyma.debug.ProxyRequestDumper;
import org.homelinux.nx01.proxyma.debug.ServletRequestDumper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * User: makko
 * Date: 4-May-2007
 * Time: 12.06.36
 * </p><p>
 * This class is one of the main classes of the proxyma project.
 * It implements a lightweight reverse-proxy with basic url-rewriting capabilities.
 * Most of the actual dirty work of rewriting and proxying is done in RequestForwarder.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ReverseProxyForServlets {

    /**
     * This method buids the Proxy instance using the dafault context to load the rules from the
     * ProxymaRulesetPool (The default context is the context path of the web-application).
     * Note: this is the default constructor for this class.
     */
    public ReverseProxyForServlets() {
        // Allocate the RequestForwarder Class for this Proxy
        reqForwarder = new RequestForwarder();

        //Set the logging basepath System property if not setted by the user
        ProxymaLog.checkAndSetDefaultSystemLoggingBasepathProperty();

        //Load the configuration for the log4jsubsystem looking for it into the path specified by the System property.
        ProxymaLog.checkAndLoadDefaultLog4jConfigurationFileProperty();

    }

    /**
     * This method buids the Proxy instance using the passed customContext to load the rules from the
     * ProxymaRulesetPool (The default context is the context path of the web-application).
     * This constructor could be useful if you are using proxyma as a library into your custom application.
     *
     * @param customContext the string that rappresents the context to use in the getRuleSet() method of the ProxymaRuleSetsPool.
     */
    public ReverseProxyForServlets(String customContext) {
        // Allocate the RequestForwarder Class for this Proxy
        reqForwarder = new RequestForwarder();

        //Set the customContext vaiable
        this.customContext = customContext;

        //Set the logging basepath System property if not setted by the user
        ProxymaLog.checkAndSetDefaultSystemLoggingBasepathProperty();

        //Load the configuration for the log4jsubsystem looking for it into the path specified by the System property.
        ProxymaLog.checkAndLoadDefaultLog4jConfigurationFileProperty();
    }

    /**
     * Handle the proxy request, both POST and GET.
     * This method was created because 90% of the code was duplicated in doGet and doPost.
     *
     * @param request  the requrest.
     * @param response the response.
     * @param isPost   true if handling a POST request, false if handling a GET request.
     * @throws ServletException if a ServletException is thrown during processing.
     * @throws IOException      if an IOException is thrown during processing.
     */
    public void doProxy(HttpServletRequest request, HttpServletResponse response, boolean isPost)
            throws ServletException, IOException {
        //regexp that matches the basepath of proxyma
        Pattern pattern = getServletPathMatchPattern(request.getServletPath());
        Matcher matcher = pattern.matcher(request.getRequestURI());

        //In debug mode this will countains the debug data..
        StringBuffer debugData = null;

        //searching for proxyma base url.
        matcher.find();

        //This is the proxyma base path url (without trailing "/")
        //NOTE: Here an exception is thrown if the path is the same as the basepath and did not end with a "/"
        String proxymaBasePath;
        try {
            proxymaBasePath = matcher.group();
        } catch (IllegalStateException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Reusing the regexp
        matcher.reset();

        //If the servlet is not yet initialized, obtain the global Ruleset for this instance of proxyma.
        if (ruleSet == null) {
            String removeFromRegexp = request.getServletPath() + ".*$";
            ProxymaRuleSetsPool ruleSetsPool = ProxymaRuleSetsPool.getInstance();

            //Use the appropriate context to retrieve rules.
            if (customContext == null)
                ruleSet = ruleSetsPool.getRuleSet(proxymaBasePath.replaceFirst(removeFromRegexp, ProxymaConstants.EMPTY_STRING));
            else
                ruleSet = ruleSetsPool.getRuleSet(customContext);
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
            int returnCode;

            //if configured, show to the user the current availables resources
            if ((showMasqueradedResources) && (splittedPath[0].length() == 0)) {
                showRules(ruleSet.getRulesAsCollection().iterator(), response);
                returnCode = HttpServletResponse.SC_OK;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                returnCode = HttpServletResponse.SC_BAD_REQUEST;
            }

            ProxymaLog.instance.accessLog(request.getRemoteAddr(),
                                          request.getRemoteUser(),
                                          request.getMethod(),
                                          request.getPathInfo(),
                                          request.getProtocol(),
                                          ProxymaConstants.SHOW_RULES_LIST,
                                          request.getHeader("User-Agent"),
                                          returnCode);
            return;
        }

        //Search for a rule that matches current context.. if it's not foud an error is sent back.
        ProxymaRuleBean activeRule = ruleSet.getRule(splittedPath[0]);
        if (activeRule == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            if (activeRule.isProductionLoggingEnabled()) {
                ProxymaLog.instance.accessLog(request.getRemoteAddr(),
                                              request.getRemoteUser(),
                                              request.getMethod(),
                                              request.getPathInfo(),
                                              request.getProtocol(),
                                              ProxymaConstants.MASQUERADED_RESOURCE_NOTFOUND,
                                              request.getHeader("User-Agent"),
                                              HttpServletResponse.SC_NOT_FOUND);
                }
            return;
        }

        //this is the url to retrive with the requestForwarder
        StringBuffer proxyRequestUrl = new StringBuffer();
        proxyRequestUrl.append(activeRule.getProxyPassHost()).append(splittedPath[1]);


        //get the query string
        if (request.getQueryString() != null) {
            proxyRequestUrl.append(ProxymaConstants.QUERYSTRING_SEPARATOR);
            proxyRequestUrl.append(request.getQueryString());
        }

        // this call works only the first time for any rule. For further calls it will be ignored.
        // It sets the constant attribute for the new context of the RewriterEngine for the current rule
        activeRule.getParserPatterns().setNewContext(new StringBuffer().append(proxymaBasePath).append(splittedPath[0]).append(ProxymaConstants.PATH_SEPARATOR).toString());

        //If the rule is disabled exit with forbidden error
        if (!(activeRule.isRuleEnabled())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            if (activeRule.isProductionLoggingEnabled()) {
                ProxymaLog.instance.accessLog(request.getRemoteAddr(),
                                              request.getRemoteUser(),
                                              request.getMethod(),
                                              request.getPathInfo(),
                                              request.getProtocol(),
                                              ProxymaConstants.DESTINATION_TEMPORARY_DISABLED,
                                              request.getHeader("User-Agent"),
                                              HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        //retrive the request headers.
        Hashtable requestHeaders = new Hashtable();
        getHeaders(request, requestHeaders);

        //Get Post Data:
        StringBuffer postData = null;

        if (isPost)
            postData = getPostData(request, activeRule);

        //Generate Debud informations..
        if (activeRule.isDebugLoggingEnabled()) {
            debugData = new StringBuffer();
            new ServletRequestDumper().dumpRequest(request, debugData);
            new ProxyRequestDumper().dumpRequest(requestHeaders, postData, proxyRequestUrl.toString(), proxymaBasePath, debugData);
        }  

        //Do the dirty job..
        int returnCode = isPost ?
                reqForwarder.doPost(ruleSet, activeRule, proxyRequestUrl.toString(), postData, requestHeaders, response, debugData) :
                reqForwarder.doGet(ruleSet, activeRule, proxyRequestUrl.toString(), requestHeaders, response, debugData);

        if (returnCode >= ProxymaConstants.SERVER_ERROR_400)
            response.sendError(returnCode);

        //if production log level is enabled, writes the track of the request into the proxyma_access.log
        if (activeRule.isProductionLoggingEnabled()) {
            ProxymaLog.instance.accessLog(request.getRemoteAddr(),
                                          request.getRemoteUser(),
                                          request.getMethod(),
                                          request.getPathInfo(),
                                          request.getProtocol(),
                                          proxyRequestUrl.toString(),
                                          request.getHeader("User-Agent"),
                                          returnCode);
        }

        //Dumps the debug informations into the right place by handling the proper Logging Policy
        if (activeRule.isDebugLoggingEnabled()) {
            switch (activeRule.getLoggingPolicy()) {
                case ProxymaConstants.LOGGING_POLICY_ONLINE_AUDIT:
                    response.setContentType(ProxymaConstants.TEXT_PLAIN);
                    PrintStream online_out = new PrintStream(response.getOutputStream());
                    online_out.println(debugData.toString());
                    online_out.flush();
                    break;

                case ProxymaConstants.LOGGING_POLICY_ONFILE_AUDIT:
                    PrintStream onfile_out = ProxymaLog.instance.getAuditOutputStream();
                    onfile_out.println(debugData.toString());
                    onfile_out.flush();
                    break;

                default:
                    //THis is a strange case...
                    ProxymaLog.instance.errors("WARNING: Logging Policy for rule \"" + activeRule.getProxyFolder() + "\" is malconfigured!");
                    break;
            }
        }
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
     * @param req         the post request
     * @param currentRule current proxmya rule to use for determining is maximum post size is violated.
     * @return a StringBuffer with the post data.
     * @throws IOException if IOException occurs in processing.
     */
    private StringBuffer getPostData(HttpServletRequest req, ProxymaRuleBean currentRule) throws IOException {
        StringBuffer retValue = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        char[] buffer = new char[ProxymaConstants.BUFSIZE];
        int count;
        int postSize = 0; //size of the POST data
        while ((count = reader.read(buffer, 0, ProxymaConstants.BUFSIZE)) > -1) {
            retValue.append(buffer, 0, count);
            postSize += count;
            if ((postSize > currentRule.getMaxPostSize()) && (currentRule.getMaxPostSize() == 0)) {
                ProxymaLog.instance.errors("WARNING: POST data override the maximum allowed size (" +
                        currentRule.getMaxPostSize() + " bytes).");
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

    /**
     * Gets the status of the showMasqueradedResources flag.
     * (see setShowMasqueradedResources() for more informations).
     *
     * @return the status of the flag.
     */
    public boolean isShowMasqueradedResources() {
        return showMasqueradedResources;
    }

    /**
     * Sets the staus of the flag.
     * If its value is true, when the proxy is called without specify any
     * proxyFolder it will provide a page with the current configurated rules.
     * If its value is false, when the proxy is called without specify any
     * proxyFolder an http BAD_REQUEST error is returned to the client
     *
     * @param showMasqueradedResources the new status for the flag
     */
    public void setShowMasqueradedResources(boolean showMasqueradedResources) {
        this.showMasqueradedResources = showMasqueradedResources;
    }

    //Proxy Private attributes
    private Pattern servletPathMatchPattern = null;
    private ProxymaRuleSet ruleSet = null;
    private RequestForwarder reqForwarder = null;
    private boolean showMasqueradedResources = ProxymaConstants.DEFAULT_SHOW_MASQUERADED_RESOURCES;
    private String customContext = null;
}
