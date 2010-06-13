package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;
import org.homelinux.nx01.proxyma.debug.ProxyResponseDumper;
import org.htmlparser.util.ParserException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * <p/>
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 * </p><p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 15.16.12
 * </p><p>
 * This class implements the proxy connection to the wanted resource.
 * It uses the URLConnection class for the connections to the remote resources.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 */
public class RequestForwarder {

    /**
     * Class Constructor, sets the private attributes of the class.
     */
    public RequestForwarder() {

        //pattern to replace charset encoding
        charsetReplaceEncodingPattern = Pattern.compile("^.*=");

        // Generate a new RewriterEngine for the url rewriting
        // Note that if the rewrite engine is disabled (by  configuration) this class will not be used)
        parserEngine = new RewriterEngine();
    }

    /**
     * Uses the appropriate methods (GET or POST) to retrive the resource for the client.
     *
     * @param requestMethod       this could be "GET" or "POST"
     * @param requestUrl          the resuorce to rertive
     * @param postData            if the requestMethod is "POST" this string is used as Post Data.
     * @param requestHeaders      countains the headers to set for the HTTP connection
     * @param httpservletresponse is the response for the client.
     * @param rule                is the current configuration for the proxy
     * @return the http_Status Code of the forwarded request.
     */
    public int doAction(String requestMethod, String requestUrl, StringBuffer postData, Hashtable requestHeaders, HttpServletResponse httpservletresponse, ProxymaRuleBean rule, ProxymaRuleSet ruleSet) {
        if (ProxymaConstants.GET_METHOD.equals(requestMethod.toUpperCase()))
            return doGet(ruleSet, rule, requestUrl, requestHeaders, httpservletresponse);
        else
            return doPost(ruleSet, rule, requestUrl, postData, requestHeaders, httpservletresponse);
    }

    /**
     * This method implements the proxy connection in POST mode.
     *
     * @param rule                is the current configuration for the proxy
     * @param requestUrl          the resuorce where the POST will be perfomed.
     * @param postData            this string is used as Post Data.
     * @param requestHeaders      countains the headers to set for the HTTP connection
     * @param httpservletresponse is the response for the client.
     * @return the http_Status Code of the forwarded request.
     */
    private int doPost(ProxymaRuleSet ruleSet, ProxymaRuleBean rule, String requestUrl, StringBuffer postData, Hashtable requestHeaders, HttpServletResponse httpservletresponse) {
        int exitStatus = 0;
        HttpURLConnection urlconnection = null;

        // execute the Url connection
        try {
            URL url = new URL(requestUrl);
            urlconnection = (HttpURLConnection) url.openConnection();
            if (rule.isProxyAuthenticationEnabled())
                urlconnection.setRequestProperty(ProxymaConstants.PROXY_AUTHORIZATION, rule.getProxyAuthentication());
            urlconnection.setDoOutput(true);
            urlconnection.setDoInput(true);
            urlconnection.setUseCaches(false);
            urlconnection.setInstanceFollowRedirects(false);

            urlconnection.setRequestProperty(ProxymaConstants.CONTENT_TYPE, ProxymaConstants.FORM_POST_MODE);
            if (requestHeaders != null) {
                String headerName;
                for (Enumeration enumeration = requestHeaders.keys(); enumeration.hasMoreElements(); urlconnection.setRequestProperty(headerName, (String) requestHeaders.get(headerName)))
                    headerName = (String) enumeration.nextElement();
            }

            //Send Post Data..
            PrintWriter printwriter = new PrintWriter(urlconnection.getOutputStream());
            printwriter.print(postData.toString());
            printwriter.close();

            //don't follow any redirects and send it back to the client.
            int stat = urlconnection.getResponseCode();
            if (stat >= ProxymaConstants.REDIRECT_300 && stat <= ProxymaConstants.REDIRECT_307 &&
                    stat != ProxymaConstants.REDIRECT_306 && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                httpservletresponse.sendRedirect(parserEngine.rewriteUrl(urlconnection.getHeaderField(ProxymaConstants.LOCATION), rule, ruleSet));
                urlconnection.disconnect();
                if (rule.isDebugModeEnabled()) {
                    //print on the server log the url where it redirects
                    System.out.println("RequestForwarder() DEBUG: Obtained Redirect to \"" + urlconnection.getHeaderField(ProxymaConstants.LOCATION) + "\"");
                }
                return stat;
            } else if (stat >= ProxymaConstants.SERVER_ERROR_400) {
                return stat;
            } else {
                exitStatus = stat;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (urlconnection != null)
                urlconnection.disconnect();
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        //get the content type and the encoding of the remote resource
        String encoding = urlconnection.getContentEncoding();
        String contentType = urlconnection.getContentType();
        String encodingCharset = ProxymaConstants.DEFAULT_CHARSET;
        if (encoding == null)
            encoding = ProxymaConstants.EMPTY_STRING;
        if (contentType != null) {
            httpservletresponse.setContentType(contentType);
            encodingCharset = charsetReplaceEncodingPattern.matcher(contentType).replaceFirst("");
        } else
            contentType = ProxymaConstants.EMPTY_STRING;


        // if debug mode is enabled print to the response the debug informations
        if (rule.isDebugModeEnabled()) {
            ProxyResponseDumper prd = new ProxyResponseDumper();

            try {
                prd.dumpResponse(urlconnection, httpservletresponse);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Data forwording to the client
        Object inputStream = null;
        BufferedOutputStream bufferedoutputstream = null;
        try {
            //If the rewriter engine is enabled and the resource is an html page we have to perform the url rewriting
            if ((rule.isRewriteEngineEnabled()) && (contentType.indexOf(ProxymaConstants.HTML_PAGE) >= 0)) {
                //read the html data
                BufferedReader reader = null;
                if (encoding.indexOf(ProxymaConstants.GZIP_DATA) >= 0)
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(urlconnection.getInputStream())));
                else
                    reader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));

                char[] buffer = new char[ProxymaConstants.BUFSIZE];
                StringBuffer responseData = new StringBuffer();
                int count;
                while ((count = reader.read(buffer, 0, ProxymaConstants.BUFSIZE)) > -1)
                    responseData.append(buffer, 0, count);

                //parse the html
                String originalData = responseData.toString();
                String rewrittenData = parserEngine.rewriteLinks(rule, ruleSet, originalData, encodingCharset);

                //if debug mode is enabled encapsulate the resource in the debug informations
                if (rule.isDebugModeEnabled()) {
                    httpservletresponse.setContentType(ProxymaConstants.TEXT_PLAIN);
                    ProxyResponseDumper prd = new ProxyResponseDumper();
                    try {
                        prd.dumpData(originalData, rewrittenData, httpservletresponse, parserEngine, rule);
                    } catch (ServletException e) {
                        e.printStackTrace();
                    }
                } else {
                    //Send the data back to the client
                    bufferedoutputstream = new BufferedOutputStream(httpservletresponse.getOutputStream());

                    inputStream = new BufferedInputStream(new ByteArrayInputStream(rewrittenData.getBytes()));
                    int i;
                    while ((i = ((InputStream) (inputStream)).read()) >= 0)
                        bufferedoutputstream.write(i);
                }
            } else {
                //if the resource is not html or the rewriter engine is disabled send the data back to the client with no modifications.
                if (encoding.indexOf(ProxymaConstants.GZIP_DATA) >= 0)
                    inputStream = new GZIPInputStream(urlconnection.getInputStream());
                else
                    inputStream = new BufferedInputStream(urlconnection.getInputStream());

                bufferedoutputstream = new BufferedOutputStream(httpservletresponse.getOutputStream());
                if (rule.isDebugModeEnabled()) {
                    httpservletresponse.setContentType(ProxymaConstants.TEXT_PLAIN);
                    bufferedoutputstream.write("############## UNTOUCHED DATA FOLLOWS ############\n\n".getBytes());
                }
                int i;
                while ((i = ((InputStream) (inputStream)).read()) >= 0)
                    bufferedoutputstream.write(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    ((InputStream) (inputStream)).close();
                if (bufferedoutputstream != null) {
                    bufferedoutputstream.flush();
                    bufferedoutputstream.close();
                }
                if (urlconnection != null)
                    urlconnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                exitStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }

        return exitStatus;
    }

    /**
     * This method implements the proxy connection in GET mode.
     *
     * @param requestUrl          the resuorce where the GET will be perfomed.
     * @param requestHeaders      countains the headers to set for the HTTP connection
     * @param httpservletresponse is the response for the client.
     * @return the http_Status Code of the forwarded request.
     */
    private int doGet(ProxymaRuleSet ruleSet, ProxymaRuleBean rule, String requestUrl, Hashtable requestHeaders, HttpServletResponse httpservletresponse) {
        int exitStatus = 0;
        HttpURLConnection urlconnection = null;

        //execute the url connection
        try {
            URL url = new URL(requestUrl);
            urlconnection = (HttpURLConnection) url.openConnection();

            if (rule.isProxyAuthenticationEnabled())
                urlconnection.setRequestProperty(ProxymaConstants.PROXY_AUTHORIZATION, rule.getProxyAuthentication());

            urlconnection.setDoInput(true);
            urlconnection.setUseCaches(false);
            urlconnection.setInstanceFollowRedirects(false);

            if (requestHeaders != null) {
                String headerName;
                for (Enumeration enumeration = requestHeaders.keys(); enumeration.hasMoreElements(); urlconnection.setRequestProperty(headerName, (String) requestHeaders.get(headerName)))
                    headerName = (String) enumeration.nextElement();
            }

            //don't follow any redirects and send it back to the client.
            int stat = urlconnection.getResponseCode();
            if (stat >= ProxymaConstants.REDIRECT_300 && stat <= ProxymaConstants.REDIRECT_307 &&
                    stat != ProxymaConstants.REDIRECT_306 && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                httpservletresponse.sendRedirect(parserEngine.rewriteUrl(urlconnection.getHeaderField(ProxymaConstants.LOCATION), rule, ruleSet));
                urlconnection.disconnect();
                if (rule.isDebugModeEnabled()) {
                    //print on the server log the url where it redirects
                    System.out.println("RequestForwarder() DEBUG: Obtained Redirect to \"" + urlconnection.getHeaderField(ProxymaConstants.LOCATION) + "\"");
                }
                return stat;
            } else if (stat >= ProxymaConstants.SERVER_ERROR_400) {
                return stat;
            } else {
                exitStatus = stat;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (urlconnection != null)
                urlconnection.disconnect();
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        //get the content type and the encoding of the remote resource
        String encoding = urlconnection.getContentEncoding();
        String contentType = urlconnection.getContentType();
        String encodingCharset = ProxymaConstants.DEFAULT_CHARSET;
        if (encoding == null)
            encoding = ProxymaConstants.EMPTY_STRING;
        if (contentType != null) {
            httpservletresponse.setContentType(contentType);
            encodingCharset = charsetReplaceEncodingPattern.matcher(contentType).replaceFirst(ProxymaConstants.EMPTY_STRING);
        } else
            contentType = ProxymaConstants.EMPTY_STRING;

        // if debug mode is enabled print to the response the debug informations
        if (rule.isDebugModeEnabled()) {
            ProxyResponseDumper prd = new ProxyResponseDumper();

            try {
                prd.dumpResponse(urlconnection, httpservletresponse);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Data forwording to the client
        Object inputStream = null;
        BufferedOutputStream bufferedoutputstream = null;
        try {
            //If the rewriter engine is enabled and the resource is an html pageor a css, we have to perform the url rewriting
            if ((rule.isRewriteEngineEnabled()) && ((contentType.indexOf(ProxymaConstants.HTML_PAGE) >= 0) ||
                    (contentType.indexOf(ProxymaConstants.CSS_STYLESHEET) >= 0))) {
                //read the html or the css data
                BufferedReader reader = null;
                if (encoding.indexOf(ProxymaConstants.GZIP_DATA) >= 0)
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(urlconnection.getInputStream())));
                else
                    reader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));

                char[] buffer = new char[ProxymaConstants.BUFSIZE];
                StringBuffer responseData = new StringBuffer();
                int count;
                while ((count = reader.read(buffer, 0, ProxymaConstants.BUFSIZE)) > -1)
                    responseData.append(buffer, 0, count);

                //Parsing html or css.
                String originalData = responseData.toString();
                String rewrittenData = null;
                if (contentType.indexOf(ProxymaConstants.HTML_PAGE) >= 0)
                    rewrittenData = parserEngine.rewriteLinks(rule, ruleSet, originalData, encodingCharset);
                else
                    rewrittenData = parserEngine.rewriteStyleLinks(originalData, rule, ruleSet);

                //if debug mode is enabled encapsulate the resource in the debug informations
                if (rule.isDebugModeEnabled()) {
                    httpservletresponse.setContentType(ProxymaConstants.TEXT_PLAIN);
                    ProxyResponseDumper prd = new ProxyResponseDumper();
                    try {
                        prd.dumpData(originalData, rewrittenData, httpservletresponse, parserEngine, rule);
                    } catch (ServletException e) {
                        e.printStackTrace();
                    }
                } else {
                    //Send the data back to the client
                    bufferedoutputstream = new BufferedOutputStream(httpservletresponse.getOutputStream());

                    inputStream = new BufferedInputStream(new ByteArrayInputStream(rewrittenData.getBytes()));
                    int i;
                    while ((i = ((InputStream) (inputStream)).read()) >= 0)
                        bufferedoutputstream.write(i);
                }
            } else if ((rule.isRewriteEngineEnabled()) && (contentType.indexOf(ProxymaConstants.JAVASCRIPT_LIBRARY) >= 0)) {
                //read the javascript library only if the rewrite for javascript is enabled
                if (rule.getJavascriptHandlingMode() == RewriterConstants.REWRITE) {
                    BufferedReader reader = null;
                    if (encoding.indexOf(ProxymaConstants.GZIP_DATA) >= 0)
                        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(urlconnection.getInputStream())));
                    else
                        reader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));

                    char[] buffer = new char[ProxymaConstants.BUFSIZE];
                    StringBuffer responseData = new StringBuffer();
                    int count;
                    while ((count = reader.read(buffer, 0, ProxymaConstants.BUFSIZE)) > -1)
                        responseData.append(buffer, 0, count);

                    //Parsing js.
                    String originalData = responseData.toString();
                    String rewrittenData = parserEngine.rewriteJavaScriptUrls(originalData, rule, ruleSet);

                    //if debug mode is enabled encapsulate the resource in the debug informations
                    if (rule.isDebugModeEnabled()) {
                        httpservletresponse.setContentType(ProxymaConstants.TEXT_PLAIN);
                        ProxyResponseDumper prd = new ProxyResponseDumper();
                        try {
                            prd.dumpData(originalData, rewrittenData, httpservletresponse, parserEngine, rule);
                        } catch (ServletException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Send the data back to the client
                        bufferedoutputstream = new BufferedOutputStream(httpservletresponse.getOutputStream());

                        inputStream = new BufferedInputStream(new ByteArrayInputStream(rewrittenData.getBytes()));
                        int i;
                        while ((i = ((InputStream) (inputStream)).read()) >= 0)
                            bufferedoutputstream.write(i);
                    }
                }
            } else {
                //if the resource is not html, css, js or the rewriter engine is disabled send the data back to the client with no modifications.
                if (encoding.indexOf(ProxymaConstants.GZIP_DATA) >= 0)
                    inputStream = new GZIPInputStream(urlconnection.getInputStream());
                else
                    inputStream = new BufferedInputStream(urlconnection.getInputStream());

                bufferedoutputstream = new BufferedOutputStream(httpservletresponse.getOutputStream());
                if (rule.isDebugModeEnabled()) {
                    httpservletresponse.setContentType(ProxymaConstants.TEXT_PLAIN);
                    bufferedoutputstream.write("############## UNTOUCHED DATA FOLLOWS ############\n\n".getBytes());
                }
                int i;
                while ((i = ((InputStream) (inputStream)).read()) >= 0)
                    bufferedoutputstream.write(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    ((InputStream) (inputStream)).close();
                if (bufferedoutputstream != null) {
                    bufferedoutputstream.flush();
                    bufferedoutputstream.close();
                }
                if (urlconnection != null)
                    urlconnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                exitStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }

        return exitStatus;
    }

    // Private variables.
    Pattern charsetReplaceEncodingPattern = null;
    RewriterEngine parserEngine = null;
}
