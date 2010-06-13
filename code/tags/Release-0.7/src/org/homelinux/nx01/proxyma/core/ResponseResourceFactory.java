package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;
import org.homelinux.nx01.proxyma.beans.ResponseResourceBean;
import org.homelinux.nx01.proxyma.beans.RuleBean;
import org.homelinux.nx01.proxyma.cache.SmartBuffer;

import javax.servlet.http.Cookie;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * <p/>
 * User: makko
 * Date: 5-ago-2007
 * Time: 17.16.42
 * <p/>
 * This class is a Response Resouce beans factory.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ResponseResourceFactory {
    /**
     * Builds a Response Resource Bean from the passed arguments.
     *
     * @param req the client request bean that wants the resource
     * @param urlConnection the connection to the wanted resource
     * @param rule the rule that belongs to the original request
     * @param ruleSet the current ruleset where the rules was taken
     * @param rewriter the current rewriter engine
     * @return a new resource bean to use for send back data to the client (or to be cached).
     */
    public static final ResponseResourceBean buldResponseResource(ClientRequestBean req, HttpURLConnection urlConnection, RuleBean rule, RuleSet ruleSet, RewriterEngine rewriter) {
        ResponseResourceBean retValue = new ResponseResourceBean(req.getProxyRequestUrl());
        SimpleDateFormat dateFormat = new SimpleDateFormat(ProxymaConstants.DATE_FORMAT);

        //set the HTTP return code:
        try {
            retValue.setReturnCode(urlConnection.getResponseCode());
        } catch (IOException e) {
            ProxymaLog.instance.errors("This is an impossible error!!!! Contact the Author :.(.");
            e.printStackTrace();
        }

        //set the original content encoding, content-type and charset
        retValue.setOriginalEncoding(urlConnection.getContentEncoding());
        retValue.setOriginaContentType(urlConnection.getContentType());

        //filter headers  and cookies
        Map headers = urlConnection.getHeaderFields();
        Iterator names = headers.keySet().iterator();
        while (names.hasNext()) {
            String headerName = (String) names.next();

            if ((ProxymaConstants.CONTENT_ENCODING.equalsIgnoreCase(headerName)) ||
                    (ProxymaConstants.CONTENT_LENGTH.equalsIgnoreCase(headerName))) {
                //These headers will be regenerated by the forwarder so we can skip them
            } else if (ProxymaConstants.SET_COOKIE.equalsIgnoreCase(headerName)) {
                //manage cookies
                Iterator cookies = ((List) headers.get(headerName)).iterator();
                while (cookies.hasNext()) {
                    //process cookies one at a time..
                    StringTokenizer st = new StringTokenizer((String) cookies.next(), ProxymaConstants.COOKIE_TOKENS_DELIMITER);
                    Cookie theCookie = null;
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        String cookieNameValue[] = token.split(ProxymaConstants.COOKIE_VALUE_DELIMITER);
                        theCookie = new Cookie(cookieNameValue[0], cookieNameValue[1]);
                    }
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (token.startsWith(ProxymaConstants.COOKIE_DOMAIN)) {
                            //Rewrites the domain of cookie (this will be rewritten soon by the rewriter)
                            theCookie.setDomain(ruleSet.getProxymaDomain());
                        } else if (token.startsWith(ProxymaConstants.COOKIE_PATH)) {
                            //Rewrites the path of cookie (even this will be rewritten soon by the request forwarder)
                            token = token.replaceFirst(ProxymaConstants.COOKIE_PATH, ProxymaConstants.EMPTY_STRING);
                            theCookie.setPath(rewriter.rewriteUrl(token, rule, ruleSet));
                        } else if (token.startsWith(ProxymaConstants.COOKIE_EXPIRES)) {
                            //process path of cookie (even this will be rewritten soon by the request forwarder)
                            token = token.replaceFirst(ProxymaConstants.COOKIE_EXPIRES, ProxymaConstants.EMPTY_STRING);
                            Date now = new Date();
                            try {
                                theCookie.setMaxAge(now.compareTo(dateFormat.parse(token)));
                            } catch (ParseException e) {
                                //do not set the age
                                ;
                            }
                        } else if (token.startsWith(ProxymaConstants.COOKIE_VERSION)) {
                            //process path of cookie (even this will be rewritten soon by the rewriter)
                            token = token.replaceFirst(ProxymaConstants.COOKIE_VERSION, ProxymaConstants.EMPTY_STRING);
                            theCookie.setVersion(Integer.parseInt(token));
                        } else if (token.startsWith(ProxymaConstants.COOKIE_SECURE)) {
                            theCookie.setSecure(true);
                        }
                    }

                    //This is a trick to handle the JSESSIONID cookie of remote tomcat applications
                    if (ProxymaConstants.JSESSIONID_COOKIE.equalsIgnoreCase(theCookie.getName())) {
                        if (theCookie.getPath() == null) {
                            theCookie.setPath(req.getProxymaBasePath());
                        }
                    }

                    retValue.addCookie(theCookie);
                }
            } else {
                //copy other headers as they are.
                if (headerName != null) {
                    Iterator values = ((List) headers.get(headerName)).iterator();
                    while (values.hasNext()) {
                        retValue.setHeader(headerName, (String) values.next());
                    }
                }
            }
        }

        //Load data as binary into a SmartBuffer
        BufferedInputStream reader = null;
        try {
            SmartBuffer theBuffer = new SmartBuffer(ProxymaConstants.MAX_SIZE_OF_RAMBUFFER);
            if (retValue.getOriginalEncoding().indexOf(ProxymaConstants.GZIP_DATA) >= 0)
                reader = new BufferedInputStream(new GZIPInputStream(urlConnection.getInputStream()));
            else
                reader = (new BufferedInputStream(urlConnection.getInputStream()));
            int count;
            byte app[] = new byte[ProxymaConstants.BUFSIZE];
            while ((count = reader.read(app, 0, ProxymaConstants.BUFSIZE)) > -1)
                theBuffer.append(app, count);
            retValue.setData(theBuffer);

            //set cacheable flag
            retValue.setCacheable(isCacheable(rule, urlConnection, theBuffer.getSize()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return retValue;
    }

    /**
     * Try to understand if the remote resource is cacheable using headers inspection.
     * This method could be improved to be "totally" compliant to the RFC, but the difference
     * with it are not so important it works good.. yet.
     *
     * @param rule          the current rule
     * @param urlconnection the connection to the remote resource
     * @return true if the resource is cacheable.
     */
    public static final boolean isCacheable(RuleBean rule, HttpURLConnection urlconnection, long size) {
        Map responseHeaders = urlconnection.getHeaderFields();
        String pragma, cacheControl = null;
        boolean testResult = false;

        //Check size of data
        if (rule.getMaxCachedResourceSize() > size) {
            //the size of the data can be  valid..
            //Check for intresting HTTP headers
            pragma = (String) responseHeaders.get(ProxymaConstants.PRAGMA);
            cacheControl = (String) responseHeaders.get(ProxymaConstants.CACHE_CONTROL);

            //If there are no directives we can cache it
            if ((pragma == null) && (cacheControl == null)) {
                //No directives found: we can cache it!!!
                testResult = true;
            } else {
                // we must check for cache directives..
                if (pragma != null) {
                    //Check the Pragma header..
                    if (pragma.indexOf(ProxymaConstants.NO_CACHE) > 0)
                    //we can't cache this...
                        testResult = false;
                    else
                        testResult = true;
                } else {
                    //check the cache-control header..
                    if (cacheControl != null) {
                        //Check the value
                        if ((cacheControl.indexOf(ProxymaConstants.NO_CACHE) > 0) ||
                                (cacheControl.indexOf(ProxymaConstants.NO_STORE) > 0) ||
                                (cacheControl.indexOf(ProxymaConstants.PRIVATE) > 0)) {
                            //we can't cache this
                            testResult = false;
                        } else if (cacheControl.indexOf(ProxymaConstants.PUBLIC) > 0) {
                            testResult = true;
                        } else {
                            //Ok.. we have to parse the whole string.. :O(
                            //we split the values by "," and search for max-age values.
                            String[] entries = cacheControl.split(",");
                            for (int i=0; i<entries.length; i++) {
                                if (entries[i].indexOf(ProxymaConstants.MAX_AGE) > 0) {
                                    //find maxage value
                                    int value = Integer.parseInt(entries[i].replaceFirst(ProxymaConstants.MAX_AGE, ProxymaConstants.EMPTY_STRING));
                                    if (value > 0)
                                        testResult = true;
                                    else
                                        testResult = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        //we can cache this
                        testResult = true;
                    }
                }
            }
        }

        //return the proper object or null.
        return testResult;
    }
}
