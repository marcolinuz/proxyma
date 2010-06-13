package org.homelinux.nx01.proxyma.debug;

/**
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 *
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 *
 * This class writes into the client response the proxyma request.
 *
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 */

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class ProxyRequestDumper {

    public void dumpRequest(Hashtable requestHeaders, StringBuffer postData, String proxyRequestUrl, String proxymaBasePath, HttpServletResponse res)
            throws ServletException, IOException {
        ServletOutputStream out = res.getOutputStream();
        out.println("");

        out.println("Proxy Request Data:");
        out.println("");
        out.println("Request to perform (proxyRequestUrl): " + proxyRequestUrl);
        out.println("Proxyma base url for Rewriting (proxymaBasePath): " + proxymaBasePath);

        out.println("");
        Enumeration e = requestHeaders.keys();
        if (e.hasMoreElements()) {
            out.print("Request Headers to send:");
            out.println("");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.println(" " + name + ": " + requestHeaders.get(name));
            }
            out.println("");
        }

        if (postData != null) {
            out.println("");
            out.println("PostData: " + postData.toString());
            out.println("Post Data Content length: " + postData.length());
        }
    }


    public String toString() {
        return "A class that shows the request headers sent by the client";
    }
}



