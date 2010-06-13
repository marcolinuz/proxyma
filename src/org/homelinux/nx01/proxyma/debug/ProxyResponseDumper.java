package org.homelinux.nx01.proxyma.debug;

/**
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 *
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 *
 * This class writes into the client response the response obtained from the proxed-resource.
 *
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 */

import org.homelinux.nx01.proxyma.core.RewriterPatterns;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;
import org.homelinux.nx01.proxyma.core.*;
import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;


/**
 * This servlet simply echos back the response line and
 * headers that were obtained from the remote resource.
 */
public class ProxyResponseDumper {

    public void dumpResponse(HttpURLConnection urlc, HttpServletResponse res)
            throws ServletException, IOException {
        ServletOutputStream out = res.getOutputStream();
        out.println("");

        out.println("UrlConnection Parameters:");
        out.println("");
        print(out, "Connection content-encoding (getContentEncoding)", urlc.getContentEncoding());
        print(out, "Connection content-type (getContentType)", urlc.getContentType());
        print(out, "Connection content-length (getContentLength)", urlc.getContentLength());
        if (urlc.getInstanceFollowRedirects())
            print(out, "Follow rediredirects (igetInstanceFollowRedirects)", "true" );
        else
            print(out, "Follow rediredirects (igetInstanceFollowRedirects)", "false" );

        out.println("");
    }

    public void dumpData(String originalHtml, String rewrittenHtml, HttpServletResponse res, RewriterEngine parserEngine, ProxymaRuleBean config)
            throws ServletException, IOException {
        ServletOutputStream out = res.getOutputStream();
        RewriterPatterns parserPatterns = config.getParserPatterns();

        out.println("");
        print(out, "New context for URL Rewriting (getNewContext)", parserPatterns.getNewContext());
        print(out, "Host to Match for rewriting (getProxedHost)", parserPatterns.getProxedHost());
        print(out, "Synonim for Host to Match for rewriting (getSynonimProxedHost)", parserPatterns.getSynonimProxedHost());
        print(out, "Base Path to Match for rewriting (getBasePath)", parserPatterns.getProxedBasePath());

        if (config.getJavascriptHandlingMode() == RewriterConstants.REWRITE)
            print(out, "Javascript Handling Mode (getJavascriptHandlingMode)", ProxymaConstants.REWRITE);
        else
            print(out, "Javascript Handling Mode (getJavascriptHandlingMode)", ProxymaConstants.REMOVE);

        out.println("");
        if (parserEngine.getRewrittenUrls() != null) {
            out.println("################## REWRITTEN URLS ##################");
            out.println(parserEngine.getRewrittenUrls());
        }
        if (parserEngine.getNonRewrittenUrls() != null) {
            out.println("################ NON REWRITTEN URLS ################");
            out.println(parserEngine.getNonRewrittenUrls());
        }
        if ((rewrittenHtml != null) && (!"".equals(rewrittenHtml.trim()))) {
            out.println("################## REWRITTEN DATA ##################");
            out.println(rewrittenHtml);
        }
        if ((originalHtml != null) && (!"".equals(originalHtml.trim()))) {
            out.println("################## ORIGINAL DATA ###################");
            out.println(originalHtml);
        }
        out.println("####################################################");

        out.println("");
    }

    private void print(ServletOutputStream out, String name, String value)
            throws IOException {
        out.print(" " + name + ": ");
        out.println(value == null ? "" : value);
    }

    private void print(ServletOutputStream out, String name, int value)
            throws IOException {
        out.print(" " + name + ": ");
        if (value == -1) {
            out.println("");
        } else {
            out.println(value);
        }
    }

    public String toString() {
        return "A class that shows the request headers sent by the client";
    }
}



