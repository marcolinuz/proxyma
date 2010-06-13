package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;
import org.homelinux.nx01.proxyma.beans.ResponseResourceBean;
import org.homelinux.nx01.proxyma.beans.RuleBean;
import org.homelinux.nx01.proxyma.cache.ByteBufferReader;
import org.homelinux.nx01.proxyma.cache.ResourceCache;
import org.homelinux.nx01.proxyma.cache.SmartBuffer;
import org.homelinux.nx01.proxyma.cache.SmartBufferReader;
import org.homelinux.nx01.proxyma.debug.ProxyRequestDumper;
import org.homelinux.nx01.proxyma.debug.ProxyResponseDumper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * User: makko
 * Date: 5-dic-2006
 * Time: 15.16.12
 * </p><p>
 * This class implements the proxy connection to the wanted resource.
 * It uses the URLConnection for the connections to the remote resources and the
 * cache subsystem to speed up retriving of static contents.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class RequestForwarder {

    /**
     * Class Constructor, sets the private attributes of the class.
     */
    public RequestForwarder() {
        // Generate a new RewriterEngine for the url rewriting
        // Note that if the rewrite engine is disabled (by  configuration) this class will not be used)
        parserEngine = new RewriterEngine();
    }

    /**
     * This method implements the proxy connection in GET and POST mode.
     *
     * @param ruleSet             the ruleSet to use to find relate rules
     * @param rule                the main rule to use for fetch and process the resource
     * @param request             the client request bean thet rappresents the resource to fetch
     * @param httpServletResponse is the response for the client.
     * @param debugData           the stringbuffer to use in debug mode to write debug data.
     * @return the http_Status Code of the forwarded request.
     */
    public int fetchData(RuleSet ruleSet, RuleBean rule, ClientRequestBean request, HttpServletResponse httpServletResponse, StringBuffer debugData) {
        int exitStatus = 0;
        HttpURLConnection urlconnection = null;
        ResponseResourceBean responseResource = null;
        SmartBuffer originalData = null;
        ProxyResponseDumper dumper = null;

        //If the the cache i enabled and the resource is into it.. then I will forward it without contact the remote server.
        if (rule.isCacheEnabled())
            responseResource = contextResourceCache.getResource(request.getProxyRequestUrl());

        if (responseResource == null) {
            //execute the url connection
            urlconnection = connectToResource(request, rule, debugData);
            if (urlconnection != null) {
                try {
                    //don't follow any redirects and send it back to the client.
                    exitStatus = urlconnection.getResponseCode();
                    if (exitStatus >= ProxymaConstants.MULTIPLE_CHOICES && exitStatus <= ProxymaConstants.TEMPORARY_REDIRECT &&
                            exitStatus != ProxymaConstants.UNUSED_30X_CODE && exitStatus != HttpURLConnection.HTTP_NOT_MODIFIED) {
                        httpServletResponse.sendRedirect(parserEngine.rewriteUrl(urlconnection.getHeaderField(ProxymaConstants.LOCATION), rule, ruleSet));
                        urlconnection.disconnect();

                        //Handling logging data if needed
                        if (rule.isDebugLoggingEnabled()) {
                            debugData.append("RequestForwarder() DEBUG: Obtained Redirect to \""
                                    + urlconnection.getHeaderField(ProxymaConstants.LOCATION) + "\"\n\n");
                        }
                        return exitStatus;
                    } else if (exitStatus >= ProxymaConstants.BAD_REQUEST) {
                        return exitStatus;
                    }

                    //Build a Response Resource based upon the urlConnection..
                    responseResource = ResponseResourceFactory.buldResponseResource(request, urlconnection, rule, ruleSet, parserEngine);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (urlconnection != null)
                        urlconnection.disconnect();
                    return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                } finally {
                    if (urlconnection != null)
                        urlconnection.disconnect();
                }
            } else {
                return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            }

            try {
                //Handling logging data if needed
                if (rule.isDebugLoggingEnabled()) {
                    dumper = new ProxyResponseDumper();
                    dumper.dumpResponse(responseResource, rule, debugData);
                }

                //If the rewriter engine is enabled and the resource is an html page, a javascript or a css, we have to perform the url rewriting
                if (rule.isRewriteEngineEnabled()) {
                    String contentType = responseResource.getOriginaContentType();

                    if (contentType.indexOf(ProxymaConstants.HTML_PAGE) >= 0) {
                        //parsing the HTML Page
                        originalData = responseResource.getData();
                        parserEngine.rewriteLinks(rule, ruleSet, responseResource);
                    } else if (contentType.indexOf(ProxymaConstants.CSS_STYLESHEET) >= 0) {
                        //Parsing the CSS Stylesheet
                        originalData = responseResource.getData();
                        parserEngine.rewriteStyleLinks(rule, ruleSet, responseResource);
                    } else if ((contentType.indexOf(ProxymaConstants.JAVASCRIPT_LIBRARY) >= 0) &&
                            (request.getPostData() == null)) {
                        //Parsing Javascript if the rule need it.
                        if (rule.getJavascriptHandlingMode() == RewriterConstants.PROCESS) {
                            originalData = responseResource.getData();
                            parserEngine.rewriteJavaScriptUrls(rule, ruleSet, responseResource);
                        }
                    }
                }

                //set the Content-Length
                responseResource.setHeader(ProxymaConstants.CONTENT_LENGTH, Integer.toString((int) responseResource.getData().getSize()));

            } catch (Exception e) {
                //don't cache objects on errors.
                responseResource.setCacheable(false);
                e.printStackTrace();
            }
        } else {
            //Using cached object..
            if (rule.isPerformanceTestEnabled())
                responseResource.incrementHitsCounter();

            if (rule.isDebugLoggingEnabled()) {
                dumper = new ProxyResponseDumper();
                dumper.dumpCacheStatus(contextResourceCache, debugData);
                dumper.dumpResponse(responseResource, rule, debugData);
            }
        }

        //Handling logging data if needed
        if (rule.isDebugLoggingEnabled()) {
            if (rule.isRewriteEngineEnabled() &&
                    ((responseResource.getOriginaContentType().indexOf(ProxymaConstants.HTML_PAGE) >= 0) ||
                    (responseResource.getOriginaContentType().indexOf(ProxymaConstants.CSS_STYLESHEET) >= 0) ||
                    (responseResource.getOriginaContentType().indexOf(ProxymaConstants.JAVASCRIPT_LIBRARY) >= 0)))
                dumper.dumpData(originalData, responseResource, parserEngine, rule, debugData);
            else
                debugData.append("############## UNTOUCHED (OR UNPRINTABLE) DATA OMITTED ############\n\n");
        }

        //add the resource to the cache if is cachable
        if (rule.isCacheEnabled() && responseResource.isCacheable())
            contextResourceCache.storeResource(responseResource);

        //Send back resource to the client
        if (rule.isOnlineDebugDisabled())
            sendBackResource(responseResource, httpServletResponse, rule);

        return exitStatus;
    }


    /**
     * Executes the connection to the remote server and returns an HttpURLConnection attached to
     * the wanted resource.
     *
     * @param request the request bean thet rappresents the resource to fetch
     * @param rule    the rule to use for the configurations
     * @return an urlconnection to the resource or null if there is any problem.
     */
    private HttpURLConnection connectToResource(ClientRequestBean request, RuleBean rule, StringBuffer debugData) {
        HttpURLConnection urlconnection = null;
        StringBuffer postData = request.getPostData();
        Hashtable requestHeaders = request.getHeaders();

        try {
            URL url = new URL(request.getProxyRequestUrl());
            urlconnection = (HttpURLConnection) url.openConnection();

            if (rule.isProxyAuthenticationEnabled())
                urlconnection.setRequestProperty(ProxymaConstants.PROXY_AUTHORIZATION, rule.getProxyAuthentication());

            urlconnection.setDoInput(true);
            urlconnection.setUseCaches(false);
            urlconnection.setInstanceFollowRedirects(false);

            //If this is a POST prepare the urlconnection to send the data
            if (postData != null) {
                urlconnection.setDoOutput(true);
                urlconnection.setRequestProperty(ProxymaConstants.CONTENT_TYPE, ProxymaConstants.FORM_POST_MODE);
            }

            //set request headers
            if (requestHeaders != null) {
                String headerName;
                for (Enumeration enumeration = requestHeaders.keys(); enumeration.hasMoreElements(); urlconnection.setRequestProperty(headerName, (String) requestHeaders.get(headerName)))
                    headerName = (String) enumeration.nextElement();
            }

            //set cookies
            List cookies = parserEngine.rewriteCookies(request, rule);
            if (cookies != null) {
                String cookie;
                for (Iterator iter = cookies.iterator(); iter.hasNext(); urlconnection.addRequestProperty(ProxymaConstants.COOKIE, cookie))
                    cookie = (String) iter.next();
            }

            //If this is a POST, send now the Post Data
            if (postData != null) {
                PrintWriter printwriter = new PrintWriter(urlconnection.getOutputStream());
                printwriter.print(postData.toString());
                printwriter.close();
            }

            if (rule.isDebugLoggingEnabled())
                new ProxyRequestDumper().dumpRequest(request, cookies, debugData);


        } catch (IOException e) {
            e.printStackTrace();
            if (urlconnection != null) {
                urlconnection.disconnect();
                urlconnection = null;
            }
        }

        return urlconnection;
    }


    /**
     * Send back to the client the given resource.
     *
     * @param resource            the resource to send back
     * @param httpServletResponse the response to use to send data
     * @param rule                the current rule
     * @return the original return code of the reource or a 500 error.
     */
    private int sendBackResource(ResponseResourceBean resource, HttpServletResponse httpServletResponse, RuleBean rule) {
        //Use the cache to fetch the Resource that will return to the client
        BufferedOutputStream bos = null;
        int exitStatus = -1;

        //set the returnCode
        exitStatus = resource.getReturnCode();
        httpServletResponse.setStatus(exitStatus);

        //set the headers of the response..
        Iterator iter = resource.getHeadersNames();
        while (iter.hasNext()) {
            String headerName = (String) iter.next();
            String headerValue = (String) resource.getHeader(headerName);
            httpServletResponse.setHeader(headerName, headerValue);
        }

        //set the cookies
        iter = resource.getCookies().iterator();
        while (iter.hasNext()) {
            httpServletResponse.addCookie((Cookie) iter.next());
        }

        //increase the hits counter if the performance test is enabled.
        if (rule.isPerformanceTestEnabled()) {
            resource.incrementHitsCounter();
        }

        //flows data out to the client..
        try {
            bos = new BufferedOutputStream(httpServletResponse.getOutputStream());
            ByteBufferReader data = new SmartBufferReader(resource.getData());
            byte[] buffer = new byte[ProxymaConstants.BUFSIZE];
            int count;
            while ((count = data.read(buffer, buffer.length)) >= 0)
                bos.write(buffer, 0, count);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                exitStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }

        return exitStatus;
    }

    /**
     * Sets the cache to use for this instace of the forwarder
     *
     * @param contextResourceCache the cache to use.
     */
    protected void setContextCache(ResourceCache contextResourceCache) {
        if (contextResourceCache == null)
            ProxymaLog.instance.errors("ERROR: Troubles to initialize the cache subsystem! (Sorry, the cache will not work)");
        else
            this.contextResourceCache = contextResourceCache;
    }

    /**
     * Obtains the cache object of this instance of Proxyma
     *
     * @return the cache object
     */
    public ResourceCache getCurrentContextCache() {
        return contextResourceCache;
    }

    //The rewriter engine of this forwarder
    private RewriterEngine parserEngine = null;

    //The context level cache for speedup the proxy (NOTE: only rules that are setted to use the cache will use it)
    private ResourceCache contextResourceCache = null;
}
