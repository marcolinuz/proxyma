package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.portlet.PortletRequest;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

/**
 * </p>
 * User: makko
 * Date: 4-ago-2007
 * Time: 12.57.51
 * <p/><p>
 * This is a class to build ClientRequestBeans from various input class (servlets or portlets)
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p><p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * </p>
 */
public class ClientRequestFactory {
    /**
     * Builds a ClientRequestBean from the provided HttpServletRequest.
     *
     * @param request the current request
     * @return a ClientRequestBean that can be used to fetch the remote resource.
     */
    public static final ClientRequestBean buldProxyRequest(HttpServletRequest request) {
        InputStream is = null;
        try {
            is = (InputStream) request.getInputStream();
        } catch (IOException e) {
            is = null;
        }

        ClientRequestBean retValue = new ClientRequestBean(request.getServletPath(),
                request.getRequestURI(),
                request.getRequestURL().toString(),
                request.getMethod(),
                request.getRemoteAddr(),
                request.getRemoteUser(),
                request.getPathInfo(),
                request.getProtocol(),
                request.getQueryString(),
                request.getPathTranslated(),
                request.getServerName(),
                request.getServerPort(),
                request.getRemoteHost(),
                request.getAuthType(),
                null,
                getServletHeaders(request),
                getServletCookies(request),
                (String) request.getAttribute("javax.net.ssl.cipher_suite"),
                (X509Certificate[]) request.getAttribute("javax.net.ssl.peer_certificates"),
                is,
                null,
                null);

        return retValue;
    }

    /**
     * Builds a ClientRequestBean from the provided HttpServletRequest.
     *
     * @param request the current request
     * @return a ClientRequestBean that can be used to fetch the remote resource.
     */
    public static final ClientRequestBean buldProxyRequest(PortletRequest request) {
        ClientRequestBean retValue = new ClientRequestBean();

        retValue.setPath(request.getContextPath());
        retValue.setMethod(ProxymaConstants.GET_METHOD);
        retValue.setRemoteUser(request.getRemoteUser());
        retValue.setAuthType(request.getAuthType());
        retValue.setCipherSuite((String) request.getAttribute("javax.net.ssl.cipher_suite"));
        retValue.setCertChain((X509Certificate[]) request.getAttribute("javax.net.ssl.peer_certificates"));
        retValue.setServerName(request.getServerName());
        retValue.setServerPort(request.getServerPort());


        /* todo: Fins substitutes for them into portlet api..
        retValue.setPathInfo(request.getPathInfo());
        retValue.setProtocol(request.getProtocol());
        retValue.setRemoteAddr(request.getRemoteAddr());
        retValue.setRequestURI(request.getRequestURI());
        retValue.setCookies(request.getCookies());
        retValue.setHeaders(getHeaders(request));
        retValue.setContentLength(request.getContentLength());
        retValue.setContentType(request.getContentType());
        retValue.setPathTranslated(request.getPathTranslated());
        retValue.setRemoteHost(request.getRemoteHost());
        retValue.setRequestUrl(request.getRequestURL());
        */

        //Build query string from parameters
        Enumeration parNames = request.getParameterNames();
        StringBuffer queryString = null;
        if (parNames.hasMoreElements()) {
            queryString = new StringBuffer();
            while (parNames.hasMoreElements()) {
                if (queryString.length() > 0)
                    queryString.append("&");
                String parName = (String) parNames.nextElement();
                queryString.append(parName);
                queryString.append("=");
                queryString.append(request.getParameter(parName));
            }
        }
        retValue.setQueryString(queryString.toString());

        return retValue;
    }

    /**
     * Get the request headers and sotres them into an HashTable.
     * note: it removes the "host" header and skips the cookies because it will be processed later
     *
     * @param httpservletrequest the requrest
     * @return the hastable with the stored headers.
     */
    private static final Hashtable getServletHeaders(HttpServletRequest httpservletrequest) {
        Hashtable retValue = new Hashtable();
        String s;
        Enumeration enumeration = httpservletrequest.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            s = (String) enumeration.nextElement();
            if (!(ProxymaConstants.HOST.equalsIgnoreCase(s) || ProxymaConstants.COOKIE.equalsIgnoreCase(s)))
                retValue.put(s, httpservletrequest.getHeader(s));
        }
        return retValue;
    }

    /**
     * Get the request cookies and sotres them into an ArrayList.
     *
     * @param httpservletrequest the requrest
     * @return the List with the stored Cookies.
     */
    private static final List getServletCookies(HttpServletRequest httpservletrequest) {
        ArrayList retValue = new ArrayList();
        Cookie[] cookies = httpservletrequest.getCookies();
        for (int i=0; i<cookies.length; i++)
            retValue.add(cookies[i]);
        return retValue;
    }
}
