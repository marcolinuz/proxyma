package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;
import org.homelinux.nx01.proxyma.beans.ProxyConfigurationBean;
import org.homelinux.nx01.proxyma.beans.RuleBean;
import org.homelinux.nx01.proxyma.cache.CachesPool;
import org.homelinux.nx01.proxyma.cache.ResourceCache;
import org.homelinux.nx01.proxyma.debug.ClientRequestDumper;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p/>
 * User: makko
 * Date: 4-May-2007
 * Time: 12.06.36
 * </p><p>
 * This class is one of the main classes of the proxyma project.
 * It implements a lightweight reverse-proxy with basic url-rewriting capabilities.
 * Note: most of the actual dirty work of rewriting and proxying is done in RequestForwarder.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 * 
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ReverseProxy {
    /**
     * This method builds the Proxy instance using the default Configuration to load the rules from the
     * ProxymaRulesetPool (The default context is the context path of the web-application).
     * Note: this is the default constructor for this class.
     */
    public ReverseProxy(ProxyConfigurationBean config) {
        // Allocate the RequestForwarder Class for this Proxy
        reqForwarder = new RequestForwarder();

        //set the configuration for this proxy
        configuration = config;
    }

    /**
     * Do the work for a servlet container..
     *
     * @param request the servlet request
     * @param response the servlet response
     * @throws IOException
     * @throws ServletException
     */
    public void doProxy (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //build a generic request from the original servlet request
        ClientRequestBean theRequest = ClientRequestFactory.buldProxyRequest(request);

        //do the proxy work..
        run(theRequest, response);
    }

    /**
     * Do the work for a Portlet container..
     * WARNING This is not working yet!!!
     *
     * @param request the portlet request
     * @param response the portlet response
     * @throws IOException
     * @throws ServletException
     */
    public void doProxy (PortletRequest request, PortletResponse response) throws IOException, ServletException {
        //build the generic request from the original portlet request
        ClientRequestBean theRequest = ClientRequestFactory.buldProxyRequest(request);

        run(theRequest, (HttpServletResponse)response);
    }

    /**
     * Handle the proxy request, both POST and GET.
     * This method was created because 99% of  the code was the same between GET and POST.
     *
     * @param request  the requrest.
     * @param response the response.
     * @throws IOException      if an IOException is thrown during processing.
     */
    private void run(ClientRequestBean request, HttpServletResponse response)
            throws IOException {
        //regexp that matches the basepath of proxyma
        Pattern pattern = getServletPathMatchPattern(request.getPath());
        Matcher matcher = pattern.matcher(request.getRequestURI());

        //Understads if this is a post or a get method.
        boolean isPost = ProxymaConstants.POST_METHOD.equalsIgnoreCase(request.getMethod());

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

        //set the basepath into the request bean
        request.setProxymaBasePath(proxymaBasePath);

        //Reusing the regexp
        matcher.reset();

        //If the proxy is not yet initialized, obtain the global Ruleset and initialize the cache subsystem for this instance of proxyma.
        if (ruleSet == null) {
            String removeFromRegexp = request.getPath() + ".*$";
            RuleSetsPool ruleSetsPool = RuleSetsPool.getInstance();
            CachesPool cachesPool = CachesPool.getInstance();

            //check if we must use the default context (the context-path of the application)
            if (configuration.getProxyContext() == null)
                configuration.setProxyContext(proxymaBasePath.replaceFirst(removeFromRegexp, ProxymaConstants.EMPTY_STRING));

            //Use the appropriate context to retrieve rules.
            ruleSet = ruleSetsPool.getRuleSet(configuration.getProxyContext());

            //Set the standard Context of the HttpReverseProxyServlet for this ruleSet.
            //This statement is important for the rewriters to rewrite links that belongs
            //to other rules.
            ruleSet.setProxymaStandardContext(proxymaBasePath);

            //Set the domain for this instance of proxyma, this is useful to rewrite
            //domain cookies.
            String proxymaHost = request.getServerName();
            if (proxymaHost.indexOf(ProxymaConstants.DOT) >= 0)
                ruleSet.setProxymaDomain(proxymaHost.substring(proxymaHost.indexOf(ProxymaConstants.DOT)));
            else
                ruleSet.setProxymaDomain(proxymaHost);

            //Use the appropriate context to obtain the ResourceCache Object
            ResourceCache theResourceCache = cachesPool.getCache(configuration);

            //Set the ResourceCache for the requests forwarder..
            reqForwarder.setContextCache(theResourceCache);
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
            if ((configuration.isShowMasqueradedResourcesEnabled()) && (splittedPath[0].length() == 0)) {
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
        RuleBean activeRule = ruleSet.getRule(splittedPath[0]);
        if (activeRule == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            ProxymaLog.instance.accessLog(request.getRemoteAddr(),
                    request.getRemoteUser(),
                    request.getMethod(),
                    request.getPathInfo(),
                    request.getProtocol(),
                    ProxymaConstants.MASQUERADED_RESOURCE_NOTFOUND,
                    request.getHeader("User-Agent"),
                    HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //If setted, start the performance test from here..
        long startTime = 0;
        if (activeRule.isPerformanceTestEnabled())
            startTime = new Date().getTime();


        //this is the url to retrive with the requestForwarder
        StringBuffer proxyRequestUrl = new StringBuffer();
        proxyRequestUrl.append(activeRule.getProxyPassHost()).append(splittedPath[1]);


        //get the query string
        if (request.getQueryString() != null) {
            proxyRequestUrl.append(ProxymaConstants.QUERYSTRING_SEPARATOR);
            proxyRequestUrl.append(request.getQueryString());
        }

        //set the proxy requesturl into the request bean
        request.setProxyRequestUrl(proxyRequestUrl.toString());

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

        //Get Post Data:
        if (isPost)
            request.setPostData(getPostData(request, activeRule));

        //Generate Debud informations..
        if (activeRule.isDebugLoggingEnabled()) {
            debugData = new StringBuffer();
            new ClientRequestDumper().dumpRequest(request, debugData);
        }

        //Do the dirty job..
        int returnCode = reqForwarder.fetchData(ruleSet, activeRule, request, response, debugData);

        if (returnCode >= ProxymaConstants.BAD_REQUEST)
            response.sendError(returnCode);

        //Calculate the performance test result
        if (activeRule.isPerformanceTestEnabled()) {
            long runTime = new Date().getTime() - startTime;
            ProxymaLog.instance.audit("Run-Time: [" + (runTime) + "] ms. (URL: " + proxyRequestUrl.toString() + ")");
        }

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
                RuleBean ruleBean = (RuleBean) iter.next();
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
     * Read the post data of a POST request and stores it into a StringBuffer.
     *
     * @param req         the post request
     * @param currentRule current proxmya rule to use for determining is maximum post size is violated.
     * @return a StringBuffer with the post data.
     * @throws java.io.IOException if IOException occurs in processing.
     */
    private StringBuffer getPostData(ClientRequestBean req, RuleBean currentRule) throws IOException {
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
     * Obtain the request forwarder that is attached to this instance of Reverse Proxy.
     * It is useful to permit the proper shutdown of the Proxyma Library.
     *
     * @return the request forwarder for this instance.
     */
    public RequestForwarder getCurrentRequestForwarder() {
        return reqForwarder;
    }

    //Proxy Private attributes
    private Pattern servletPathMatchPattern = null;
    private RuleSet ruleSet = null;
    private RequestForwarder reqForwarder = null;
    private ProxyConfigurationBean configuration = null;
}
