package org.homelinux.nx01.proxyma.beans;

import org.homelinux.nx01.proxyma.core.ProxymaConstants;

import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.List;
import java.io.InputStream;

/**
 * <p/>
 * User: makko
 * Date: 4-ago-2007
 * Time: 12.07.36
 * <p/>
 * This class is a generalized wrapper for any client request.
 * I need it because i want to abstract the engine from the specific implementation of request..
 * ..in other words i want a class to use both with Servlets and Portlets.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ClientRequestBean {

    /**
     * This is the defaul constructor.. you have to set all attributes by hand with getters and setters
     */
    public ClientRequestBean() {
    }

    /**
     * Custom constructor (if you have all the values you can provide them at build time.. :O)
     *
     * @param path
     * @param requestURI
     * @param requestUrl
     * @param method
     * @param remoteAddr
     * @param remoteUser
     * @param pathInfo
     * @param protocol
     * @param queryString
     * @param pathTranslated
     * @param serverName
     * @param serverPort
     * @param remoteHost
     * @param authType
     * @param postData
     * @param headers
     * @param cookies
     * @param cipherSuite
     * @param certChain
     * @param inputStream
     * @param proxyRequestUrl
     * @param proxymaBasePath
     */
    public ClientRequestBean(String path, String requestURI, String requestUrl, String method, String remoteAddr, String remoteUser, String pathInfo, String protocol, String queryString, String pathTranslated, String serverName, int serverPort, String remoteHost, String authType, StringBuffer postData, Hashtable headers, List cookies, String cipherSuite, X509Certificate[] certChain, InputStream inputStream, String proxyRequestUrl, String proxymaBasePath) {
        this.path = path;
        this.requestURI = requestURI;
        this.requestUrl = requestUrl;
        this.method = method;
        this.remoteAddr = remoteAddr;
        this.remoteUser = remoteUser;
        this.pathInfo = pathInfo;
        this.protocol = protocol;
        this.queryString = queryString;
        this.pathTranslated = pathTranslated;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.remoteHost = remoteHost;
        this.authType = authType;
        this.postData = postData;
        this.headers = headers;
        this.cookies = cookies;
        this.cipherSuite = cipherSuite;
        this.certChain = certChain;
        this.inputStream = inputStream;
        this.proxyRequestUrl = proxyRequestUrl;
        this.proxymaBasePath = proxymaBasePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Hashtable getHeaders() {
        return headers;
    }

    public void setHeaders(Hashtable headers) {
        this.headers = headers;
    }


    public String getHeader(String name) {
        return (String) this.getHeaders().get(name);
    }

    public void setHeader(String name, String value) {
        //check if the header already exists
        if ((value != null) && (!(ProxymaConstants.EMPTY_STRING.equals(value.trim())))) {
            this.headers.put(name, value);
        } else {
            this.headers.remove(name);
        }
    }

    public int getContentLength() {
        String value = (String)this.headers.get(ProxymaConstants.CONTENT_LENGTH);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return ProxymaConstants.NULL_VALUE;
        }
    }

    public void setContentLength(int contentLength) {
        if (contentLength >= 0) {
            this.headers.put(ProxymaConstants.CONTENT_LENGTH, Integer.toString(contentLength));
        }
    }

    public String getContentType() {
        String value = (String)this.headers.get(ProxymaConstants.CONTENT_TYPE);
        if (value != null) {
            return value;
        } else {
            return null;
        }
    }

    public void setContentType(String value) {
        if ((value != null) && (!(ProxymaConstants.EMPTY_STRING.equals(value.trim())))) {
            this.headers.put(ProxymaConstants.CONTENT_TYPE, value);
        } else {
            this.headers.remove(ProxymaConstants.CONTENT_TYPE);
        }
    }

    public StringBuffer getPostData() {
        return postData;
    }

    public void setPostData(StringBuffer postData) {
        this.postData = postData;
    }

    public List getCookies() {
        return cookies;
    }

    public void setCookies(List cookies) {
        this.cookies = cookies;
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public X509Certificate[] getCertChain() {
        return certChain;
    }

    public void setCertChain(X509Certificate[] certChain) {
        this.certChain = certChain;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(StringBuffer requestURL) {
        this.requestUrl = requestURL.toString();
    }

    public String getPathTranslated() {
        return pathTranslated;
    }

    public void setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getProxyRequestUrl() {
        return proxyRequestUrl;
    }

    public void setProxyRequestUrl(String proxyRequestUrl) {
        this.proxyRequestUrl = proxyRequestUrl;
    }

    public String getProxymaBasePath() {
        return proxymaBasePath;
    }

    public void setProxymaBasePath(String proxymaBasePath) {
        this.proxymaBasePath = proxymaBasePath;
    }

    //input attributes
    private String path = null;
    private String requestURI = null;
    private String requestUrl = null;
    private String method = null;
    private String remoteAddr = null;
    private String remoteUser = null;
    private String pathInfo = null;
    private String protocol = null;
    private String queryString = null;
    private String pathTranslated = null;
    private String serverName = null;
    private int serverPort = -1;
    private String remoteHost = null;
    private String authType = null;
    private StringBuffer postData = null;
    private Hashtable headers = null;
    private List cookies = null;
    private String cipherSuite = null;
    private X509Certificate certChain [] = null;
    private InputStream inputStream = null;

    //proxy attributes
    private String proxyRequestUrl = null;
    private String proxymaBasePath = null;
}
