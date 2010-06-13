package org.homelinux.nx01.proxyma.beans;

import org.homelinux.nx01.proxyma.cache.SmartBuffer;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * User: makko
 * Date: 22-lug-2007
 * Time: 16.41.51
 * </p><p>
 * This class is a generalized wrapper for any client response.
 * I need it Serializable because i want a smart way to cache the entire responses if the http
 * headers permits to caching.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 **/
public class ResponseResourceBean implements Serializable {
    /**
     * Builds and initialize the resource.
     *
     * @param url the url of the remote object
     */
    public ResponseResourceBean(String url) {
        this.url = url;
    }

    /**
     * Obtains the URL of the Resource (this is used as key for the cahe subsystem).
     *
     * @return the url of the resource
     */
    public String getUrl() {
        return url;
    }

    /**
     * Obtain the SmartBuffer that countains the binary data of the resource.
     *
     * @return the binary data.
     */
    public SmartBuffer getData() {
        return data;
    }

    /**
     * Obtain an iterator of the header names of the resource.

     * @return the iterator to fetch header names.
     */
    public Iterator getHeadersNames() {
        return headers.keySet().iterator();
    }

    /**
     * Obtain the current value of the passed header (if present)
     *
     * @param name the header name
     * @return the wanted header.
     */
    public String getHeader(String name) {
        return (String) this.headers.get(name);
    }

    /**
     * Add or Update an header to the resource.
     * Note that if a null or an empty string is passed as value the header will be removed.
     *
     * @param name name of the header to set
     * @param value value of the header
     */
    public void setHeader(String name, String value) {
        if ((value != null) && (!(ProxymaConstants.EMPTY_STRING.equals(value.trim())))) {
            this.headers.put(name, value);
        } else {
            this.headers.remove(name);
        }
    }

    /**
     * Obtain a List of the Cookies in the resource.
     *
     * @return a List of Cookies.
     */
    public List getCookies() {
        return cookies;
    }

    /**
     * Add a cookie to the resource
     *
     * @param value the Cookie to add.
     */
    public void addCookie(Cookie value) {
        if (value != null) {
            this.cookies.add(value);
        }
    }

    /**
     * Obtain the return code of the request.
     *
     * @return the original return code.
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * Set the HTTP exit status
     *
     * @param returnCode the exit status
     */
    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * Obtain the number of times that the object was retrived from the chache.
     *
     * @return
     */
    public int getCacheHits() {
        return cacheHits;
    }

    /**
     * Increments the cache hits for this object
     */
    public synchronized void incrementHitsCounter() {
        this.cacheHits++;
    }

    /**
     * Sets the binary data of the resource
     *
     * @param data the SmartBuffer that countains the data of the resource
     */
    public void setData(SmartBuffer data) {
        this.data = data;
    }

    /**
     * Obtain the value of the Content-Length header of the resource.
     *
     * @return the content-length
     */
    public int getContentLength() {
        String value = (String) this.headers.get(ProxymaConstants.CONTENT_LENGTH);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return ProxymaConstants.NULL_VALUE;
        }
    }

    /**
     * Set the value of the Content-Lenght header for this resource.
     *
     * @param contentLength the new value.
     */
    public void setContentLength(int contentLength) {
        if (contentLength >= 0) {
            this.headers.put(ProxymaConstants.CONTENT_LENGTH, Integer.toString(contentLength));
        }
    }

    /**
     * This flag is settedby the ResponseResourceFactory if the inspection of the
     * cache related headers tags this resource as cacheable.
     *
     * @return true if the resource is cacheable
     */
    public boolean isCacheable() {
        return cacheable;
    }

    /**
     * Sets the vale of the flag that reflect the cacheable status of this resource.
     *
     * @param cacheable
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    /**
     * Get the original Encoding (Content-Encoding header) of the resource.
     *
     * @return the value of the original content encoding header
     */
    public String getOriginalEncoding() {
        return originalEncoding;
    }

    /**
     * Set the value of the original content Encoding of the resource.
     *
     * @param originalEncoding the value to set.
     */
    public void setOriginalEncoding(String originalEncoding) {
        if (originalEncoding != null)
            this.originalEncoding = originalEncoding;
    }

    /**
     * Get the original value of the Content-Type header of the resource.
     *
     * @return the original content-type.
     */
    public String getOriginaContentType() {
        return originaContentType;
    }

    /**
     * Set the original content type of this resource and its original Charset.
     *
     * @param contentType the content type that comes from the remote server.
     */
    public void setOriginaContentType(String contentType) {
        if ((contentType != null) && (!(ProxymaConstants.EMPTY_STRING.equals(contentType)))) {
            Pattern charsetReplaceEncodingPattern = Pattern.compile("^.*=");
            encodingCharset = charsetReplaceEncodingPattern.matcher(contentType).replaceFirst(ProxymaConstants.EMPTY_STRING);
            if ((encodingCharset.length() == contentType.length()) && (encodingCharset.indexOf("/") >= 0)) {
                encodingCharset = ProxymaConstants.UNKNOWN_CHARSET;
            }
            this.originaContentType = contentType;
        } else {
            //This is a bad thing! Content-type is a recommended header.. :O(
            if (url != null) {
                if (url.endsWith(".htm") || url.endsWith(".html") ||
                        url.endsWith(".php") || url.endsWith(".jsp") ||
                        url.endsWith(".asp") || url.endsWith("aspx"))
                    this.originaContentType = ProxymaConstants.TEXT_HTML;
                encodingCharset = ProxymaConstants.UNKNOWN_CHARSET;
            }
            this.originaContentType = ProxymaConstants.DEFAULT_CONTENT_TYPE;
        }
    }

    /**
     * Obtain the Charset used in this resource.
     *
     * @return the charset of the resource.
     */
    public String getEncodingCharset() {
        return encodingCharset;
    }

    /**
     * Prepare the resource for the cache by setting the SmartBuffer as Read-Only.
     * So, no more data can be written to it.
     *
     * @throws IOException
     */
    public void prepareForCache () throws IOException {
        if (data != null) {
            data.setNoMoreData();
        }
    }

    //Forwardable attributes
    private String url = null;
    private Hashtable headers = new Hashtable();
    private List cookies = new ArrayList();
    private SmartBuffer data = null;
    private int returnCode = 0;
    private int cacheHits = 0;
    private boolean cacheable = false;

    //Internal service attributes
    private String originalEncoding = ProxymaConstants.EMPTY_STRING;
    private String originaContentType = ProxymaConstants.EMPTY_STRING;
    private String encodingCharset = ProxymaConstants.DEFAULT_CHARSET;
}
