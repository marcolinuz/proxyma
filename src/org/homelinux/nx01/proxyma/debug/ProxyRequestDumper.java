package org.homelinux.nx01.proxyma.debug;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;
import org.homelinux.nx01.proxyma.core.ProxymaConstants;

import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;

/**
 * </p><p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 * </p><p>
 * This class dumps the proxy request made by proxyma.
 *</p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 *</p><p>
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * </p>
 */
public class ProxyRequestDumper {

    /**
     * Dumps proxy request status
     *
     * @param request the modified ClientRequestBean used by proxyma to obtain the remote resource.
     * @param cookies the cookies to dump
     * @param out the StringBuffer where information are appended.
     */
    public void dumpRequest(ClientRequestBean request, List cookies, StringBuffer out) {

        //dump url and basepath
        out.append("***** Proxyma Request (begin) *****\n\n");
        out.append("Proxyma informations about the URL to fetch:\n");
        out.append("\tURL to fetch (getProxyRequestUrl): " + request.getProxyRequestUrl() + "\n");
        out.append("\tProxyma basepath for rewriter (getProxymaBasePath): " + request.getProxymaBasePath() + "\n");
        out.append("\n");

        //dump hesders to send
        Enumeration e = request.getHeaders().keys();
        if (e.hasMoreElements()) {
            out.append("Request Headers to send:\n");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.append("\t" + name + ": " + request.getHeader(name) + "\n");
            }
            out.append("\n");
        }

        //dump rewritten cookies
        if ((cookies != null) && (!cookies.isEmpty())) {
            Iterator iter = cookies.iterator();
            out.append("Request Cookies:\n");
            while (iter.hasNext()) {
                out.append("\t" + ProxymaConstants.COOKIE + ": " + (String) iter.next() + "\n");
            }
            out.append("\n");
        }

        //dump post data
        StringBuffer postData = request.getPostData();
        if (postData != null) {
            out.append("POST Data:\n");
            out.append("\tCalculated Content length: " + postData.length() + "\n");
            out.append("\t Data: \"" + postData.toString() + "\"\n");
            out.append("\n");
        }
        out.append("***** Proxyma Request (end) *****\n\n");
    }


    public String toString() {
        return "A class that shows the request headers sent to the masqueraded server";
    }
}



