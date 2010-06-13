package org.homelinux.nx01.proxyma.debug;

/**
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 *
 * This class simply echos back the request line and
 * headers that were sent by the client, plus any HTTPS information
 * which is accessible.
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 *
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 */

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;

public class ServletRequestDumper {

    public void dumpRequest(HttpServletRequest req, StringBuffer out)
            throws ServletException, IOException {
        //ServletOutputStream out = res.getOutputStream();
        out.append("\n----------> NEW Request Received at " + new Date() + " <----------\n\n");

        out.append("Requested URL:\n");
        out.append("\t" + getRequestUrl(req) + "\n\n");

        out.append("Request informations:\n");
        out.append("\tRequest method (getMethod): " + req.getMethod() + "\n");
        out.append("\tRequest URI (getRequestURI): " + req.getRequestURI() + "\n");
        out.append("\tRequest protocol (getProtocol): " + req.getProtocol() + "\n");
        out.append("\tServlet path (getServletPath): " + req.getServletPath() + "\n");
        out.append("\tPath info (getPathInfo): " + req.getPathInfo() + "\n");
        out.append("\tPath translated (getPathTranslated): " + req.getPathTranslated() + "\n");
        out.append("\tQuery string (getQueryString): " + req.getQueryString() + "\n");
        out.append("\tContent length (getContentLength): " + req.getContentLength() + "\n");
        out.append("\tContent type (getContentType): " + req.getContentType() + "\n");
        out.append("\tServer name (getServerName): " + req.getServerName() + "\n");
        out.append("\tServer port (getServerPort): " + req.getServerPort() + "\n");
        out.append("\tRemote user (getRemoteUser): " + req.getRemoteUser() + "\n");
        out.append("\tRemote address (getRemoteAddr): " + req.getRemoteAddr() + "\n");
        out.append("\tRemote host (getRemoteHost): " + req.getRemoteHost() + "\n");
        out.append("\tAuthorization scheme (getAuthType): " + req.getAuthType() + "\n");
        out.append("\n");

        Enumeration e = req.getHeaderNames();
        if (e.hasMoreElements()) {
            out.append("Request headers:\n");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.append("\t" + name + ": " + req.getHeader(name) + "\n");
            }
            out.append("\n");
        }

        e = req.getParameterNames();
        if (e.hasMoreElements()) {
            out.append("Request parameters (Multiple Value style):\n");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                String vals[] = (String[]) req.getParameterValues(name);
                if (vals != null) {
                    out.append("\t" + name + " = " + vals[0] + "\n");
                    for (int i = 1; i < vals.length; i++)
                        out.append("\t\t" + vals[i] + "\n");
                }
            }
            out.append("\n");
        }


        String cipherSuite = (String)
                req.getAttribute("javax.net.ssl.cipher_suite");

        if (cipherSuite != null) {
            X509Certificate certChain [] = (X509Certificate[])
                    req.getAttribute("javax.net.ssl.peer_certificates");

            out.append("HTTPS Information:\n");
            out.append("\tCipher Suite: " + cipherSuite);

            if (certChain != null) {
                for (int i = 0; i < certChain.length; i++) {
                    out.append("\tClient cert chain [" + i + "]: "
                            + certChain[i].toString() + "\n");
                }
            }

            out.append("\n");
        }
    }

    private String getRequestUrl(HttpServletRequest req) {
        StringBuffer retValue = new StringBuffer();

        retValue.append(req.getMethod());
        retValue.append(" ");
        retValue.append(req.getRequestURL().toString());
        retValue.append(" ");
        retValue.append(req.getProtocol());
        return retValue.toString();
    }

    public String toString() {
        return "A class that shows the request informations sent by the client";
    }
}



