package org.homelinux.nx01.proxyma.debug;

import org.homelinux.nx01.proxyma.beans.ResponseResourceBean;
import org.homelinux.nx01.proxyma.beans.RuleBean;
import org.homelinux.nx01.proxyma.cache.ByteBufferReader;
import org.homelinux.nx01.proxyma.cache.SmartBuffer;
import org.homelinux.nx01.proxyma.cache.SmartBufferReader;
import org.homelinux.nx01.proxyma.cache.ResourceCache;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.RewriterConstants;
import org.homelinux.nx01.proxyma.core.RewriterEngine;
import org.homelinux.nx01.proxyma.core.RewriterPatterns;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Iterator;

/**
 * </p><p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 * </p><p>
 * This class dumps the response that will be sent to the client.
 * It countains data obtained from the proxed-resource.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p><p>
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * </p>
 */
public class ProxyResponseDumper {

    /**
     * Dumps the response status.
     *
     * @param res the ResponseResourceBean to dump
     * @param rule the rule used to obtain the resource
     * @param out the StringBuffer where information are appended.
     */
    public void dumpResponse(ResponseResourceBean res, RuleBean rule, StringBuffer out) {
        RewriterPatterns parserPatterns = rule.getParserPatterns();

        out.append("***** Response Headers and Status (begin) *****\n\n");
        out.append("Response status:\n");
        out.append("\tRequested URL (getUrl): " + res.getUrl() + "\n");
        out.append("\tOriginal Encoding (getOriginalEncoding): " + res.getOriginalEncoding() + "\n");
        out.append("\tOriginal Content-Type (getOriginalContentType): " + res.getOriginaContentType() + "\n");
        out.append("\tResource Encoding Charset (getEncodingCharset): " + res.getEncodingCharset() + "\n");
        out.append("\tStatus Code (getReturnCode): " + res.getReturnCode() + "\n");
        out.append("\tThe resource is Cacheable (isCacheable): " + (res.isCacheable() ? "True" : "False") + "\n");
        out.append("\tCache Hits for this resource (getCacheHits): " + res.getCacheHits() + "\n");
        out.append("\n");

        appendString(out, "New context for URL Rewriting (getNewContext)", parserPatterns.getNewContext());
        appendString(out, "Host to Match for rewriting (getProxedHost)", parserPatterns.getProxedHost());
        appendString(out, "Synonim for Host to Match for rewriting (getSynonimProxedHost)", parserPatterns.getSynonimProxedHost());
        appendString(out, "Base Path to Match for rewriting (getBasePath)", parserPatterns.getProxedBasePath());
        out.append("\n");


        //dump obtained hesders
        Iterator e = res.getHeadersNames();
        if (e.hasNext()) {
            out.append("Response Headers:\n");
            while (e.hasNext()) {
                String name = (String) e.next();
                out.append("\t" + name + ": " + res.getHeader(name) + "\n");
            }
            out.append("\n");
        }

        //dump obtained cookies
        Iterator iter = res.getCookies().iterator();
        if (iter.hasNext()) {
            out.append("Response Cookies:\n");
            while (iter.hasNext()) {
                Cookie cookie = (Cookie) iter.next();
                out.append("\tName: " + cookie.getName() + "\n");
                if (cookie.getDomain() != null) out.append("\tDomain: " + cookie.getDomain() + "\n");
                if (cookie.getPath() != null) out.append("\tPath: " + cookie.getPath() + "\n");
                if (cookie.getMaxAge() >= 0) out.append("\tMaxAge: " + cookie.getMaxAge() + "\n");
                if (cookie.getSecure()) out.append("\tSecure: true" + "\n");
                out.append("\tValue: " + cookie.getValue() + "\n");
            }
            out.append("\n");
        }

        out.append("***** Response Headers and Status (end) *****\n\n");
    }

    /**
     * Dumps the content of the Resource
     *
     * @param originalData the buffer that countains the original resource data
     * @param responseResource the resource that countains the rewritten data
     * @param parserEngine the rewriter engine used to process the resource data
     * @param rule the rule used to obtain the resource
     * @param out the StringBuffer where information are appended.
     */
    public void dumpData(SmartBuffer originalData, ResponseResourceBean responseResource, RewriterEngine parserEngine, RuleBean rule, StringBuffer out) {
        out.append("***** Response data (begin) *****\n\n");

        if (rule.getJavascriptHandlingMode() == RewriterConstants.PROCESS)
            appendString(out, "Javascript Handling Mode (getJavascriptHandlingMode)", ProxymaConstants.PROCESS);
        else
            appendString(out, "Javascript Handling Mode (getJavascriptHandlingMode)", ProxymaConstants.REMOVE);

        out.append("\n");
        String rewrittenUrls = parserEngine.getRewrittenUrls();
        if (!ProxymaConstants.EMPTY_STRING.equals(rewrittenUrls)) {
            out.append("################## REWRITTEN URLS ##################\n");
            out.append(rewrittenUrls);
            out.append("\n");
        }
        String nonRewrittenUrls = parserEngine.getNonRewrittenUrls();
        if (!ProxymaConstants.EMPTY_STRING.equals(nonRewrittenUrls)) {
            out.append("################ NON REWRITTEN URLS ################\n");
            out.append(nonRewrittenUrls);
            out.append("\n");
        }
        if ((responseResource.getData() != null)) {
            out.append("################## DATA SENT TO THE CLIENT BROWSER ##################\n");

            //Dump out the rewritten page
            String rewrittenData = null;
            try {
                ByteBufferReader data = new SmartBufferReader(responseResource.getData());
                rewrittenData = new String(data.getBytes(), responseResource.getEncodingCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }

            out.append(rewrittenData);
            out.append("\n");
        }
        if (originalData != null) {
            out.append("################## ORIGINAL RESOURCE DATA ###################\n");

            //Dump out the original page
            String originalPage = null;
            try {
                ByteBufferReader data = new SmartBufferReader(originalData);
                originalPage = new String(data.getBytes(), responseResource.getEncodingCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }

            out.append(originalPage);
            out.append("\n");
        }
        out.append("***** Response Data (end) *****\n\n");
    }

    /**
     * Dumps some useful info about the status of the cache.
     *
     * @param theCache the current active cache.
     */
    public void dumpCacheStatus(ResourceCache theCache, StringBuffer debugData) {
        debugData.append("***** Cache status dump (Begin). *****\n\n");
        debugData.append("**************************\n" +
                "*** Cache Match found! ***\n" +
                "**************************\n");
        debugData.append("\nCached URLs:\n");
        Iterator iter = theCache.getCachedURLs();
        String url = null;
        while (iter.hasNext()) {
            url = (String) iter.next();
            debugData.append("\t" + url + "\n");
        }
        debugData.append("\n");
        debugData.append(theCache);
        debugData.append("\n***** Cache status dump (end). *****\n\n");
    }

    private void appendString(StringBuffer out, String name, String value) {
        out.append("\t" + name + ": ");
        out.append(value == null ? "\n" : value + "\n");
    }

    public String toString() {
        return "A class that shows the response that is coming from proxyma.";
    }
}



