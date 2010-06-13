package org.homelinux.nx01.proxyma.debug;

import org.homelinux.nx01.proxyma.beans.ClientRequestBean;

import javax.servlet.http.Cookie;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * </p><p>
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 * </p><p>
 * This class simply echos back the client request status.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p><p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 * </p>
 */
public class ClientRequestDumper {

    /**
     * Writes the client request status into the provided StringBuffer.
     * It dumps headers plus any HTTPS information
     * which is accessible.
     *
     * @param req the ClientRequestBean that countains the client request.
     * @param out the StringBuffer where information are appended.
     */
    public void dumpRequest(ClientRequestBean req, StringBuffer out) {
        //ServletOutputStream out = res.getOutputStream();
        out.append("\n----------> NEW Request Received at " + new Date() + " <----------\n\n");

        out.append("***** Client Request (begin) *****\n\n");
        out.append("Requested URL:\n");
        out.append("\t" + getRequestUrl(req) + "\n\n");

        out.append("Request informations:\n");
        out.append("\tRequest method (getMethod): " + req.getMethod() + "\n");
        out.append("\tRequest URI (getRequestURI): " + req.getRequestURI() + "\n");
        out.append("\tRequest protocol (getProtocol): " + req.getProtocol() + "\n");
        out.append("\tPath (getPath): " + req.getPath() + "\n");
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

        Enumeration e = req.getHeaders().keys();
        if (e.hasMoreElements()) {
            out.append("Request headers:\n");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.append("\t" + name + ": " + req.getHeader(name) + "\n");
            }
            out.append("\n");
        }

        Iterator iter = req.getCookies().iterator();
        if (iter.hasNext()) {
            out.append("Request Cookies:\n");
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

/* Disabled statements
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
*/
        String cipherSuite = req.getCipherSuite();

        X509Certificate certChain [] = req.getCertChain();
        if (certChain != null) {
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


        // Dump post data if present
        if (req.getPostData() != null) {
            out.append("POST Data:\n");
            out.append("\t\"" + req.getPostData().toString() + "\"\n");
            out.append("\n");
        }

        out.append("***** Client Request (end) *****\n\n");
    }

    private String getRequestUrl(ClientRequestBean req) {
        StringBuffer retValue = new StringBuffer();

        retValue.append(req.getMethod());
        retValue.append(" ");
        retValue.append(req.getRequestUrl().toString());
        retValue.append(" ");
        retValue.append(req.getProtocol());
        return retValue.toString();
    }

    public String toString() {
        return "A class that shows the request informations sent by the client";
    }
}



