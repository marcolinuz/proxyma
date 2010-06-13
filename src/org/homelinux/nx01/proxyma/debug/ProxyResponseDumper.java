package org.homelinux.nx01.proxyma.debug;

/**
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 *
 * This class writes into the client response the response obtained from the proxed-resource.
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 *
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 */

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.RewriterConstants;
import org.homelinux.nx01.proxyma.core.RewriterEngine;
import org.homelinux.nx01.proxyma.core.RewriterPatterns;

import java.net.HttpURLConnection;

/**
 * This servlet simply echos back the response line and
 * headers that were obtained from the remote resource.
 */
public class ProxyResponseDumper {

    public void dumpResponse(HttpURLConnection urlc, StringBuffer out) {
        out.append("UrlConnection Parameters:\n");
        appendString(out, "Connection content-encoding (getContentEncoding)", urlc.getContentEncoding());
        appendString(out, "Connection content-type (getContentType)", urlc.getContentType());
        apppendInteger(out, "Connection content-length (getContentLength)", urlc.getContentLength());
        if (urlc.getInstanceFollowRedirects())
            appendString(out, "Follow rediredirects (igetInstanceFollowRedirects)", "true" );
        else
            appendString(out, "Follow rediredirects (igetInstanceFollowRedirects)", "false" );

        out.append("\n");
    }

    public void dumpData(String originalHtml, String rewrittenHtml, StringBuffer out, RewriterEngine parserEngine, ProxymaRuleBean config) {

        RewriterPatterns parserPatterns = config.getParserPatterns();

        appendString(out, "New context for URL Rewriting (getNewContext)", parserPatterns.getNewContext());
        appendString(out, "Host to Match for rewriting (getProxedHost)", parserPatterns.getProxedHost());
        appendString(out, "Synonim for Host to Match for rewriting (getSynonimProxedHost)", parserPatterns.getSynonimProxedHost());
        appendString(out, "Base Path to Match for rewriting (getBasePath)", parserPatterns.getProxedBasePath());

        if (config.getJavascriptHandlingMode() == RewriterConstants.REWRITE)
            appendString(out, "Javascript Handling Mode (getJavascriptHandlingMode)", ProxymaConstants.REWRITE);
        else
            appendString(out, "Javascript Handling Mode (getJavascriptHandlingMode)", ProxymaConstants.REMOVE);

        out.append("\n");
        if (parserEngine.getRewrittenUrls() != null) {
            out.append("################## REWRITTEN URLS ##################\n");
            out.append(parserEngine.getRewrittenUrls());
            out.append("\n");
        }
        if (parserEngine.getNonRewrittenUrls() != null) {
            out.append("################ NON REWRITTEN URLS ################\n");
            out.append(parserEngine.getNonRewrittenUrls());
            out.append("\n");
        }
        if ((rewrittenHtml != null) && (!"".equals(rewrittenHtml.trim()))) {
            out.append("################## REWRITTEN DATA ##################\n");
            out.append(rewrittenHtml);
            out.append("\n");
        }
        if ((originalHtml != null) && (!"".equals(originalHtml.trim()))) {
            out.append("################## ORIGINAL DATA ###################\n");
            out.append(originalHtml);
            out.append("\n");
        }
        out.append("####################################################");

        out.append("\n");
    }

    private void appendString(StringBuffer out, String name, String value) {
        out.append("\t" + name + ": ");
        out.append(value == null ? "\n" : value + "\n");
    }

    private void apppendInteger(StringBuffer out, String name, int value) {
        out.append("\t" + name + ": ");
        if (value == -1) {
            out.append("\n");
        } else {
            out.append(value + "\n");
        }
    }

    public String toString() {
        return "A class that shows the response that is coming from the masqueraded resource.";
    }
}



