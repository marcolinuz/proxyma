package org.homelinux.nx01.proxyma.debug;

/**
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 *
 * This class writes into the client response the proxyma request.
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 *
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 */

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class ProxyRequestDumper {

    public void dumpRequest(Hashtable requestHeaders, StringBuffer postData, String proxyRequestUrl, String proxymaBasePath, StringBuffer out)
            throws ServletException, IOException {

        out.append("Proxy Request Data:\n");
        out.append("\tRequest to perform (proxyRequestUrl): " + proxyRequestUrl + "\n");
        out.append("\tProxyma base url for Rewriting (proxymaBasePath): " + proxymaBasePath + "\n\n");

        Enumeration e = requestHeaders.keys();
        if (e.hasMoreElements()) {
            out.append("Request Headers to send:\n");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.append("\t" + name + ": " + requestHeaders.get(name) + "\n");
            }
            out.append("\n");
        }

        if (postData != null) {
            out.append("PostData: " + postData.toString() + "\n\n");
            out.append("Post Data Content length: " + postData.length() + "\n\n");
        }
    }


    public String toString() {
        return "A class that shows the request headers sent to the masqueraded server";
    }
}



