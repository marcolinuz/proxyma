package org.homelinux.nx01.proxyma.debug;

/**
 * Created by Marco Casavecchia Morganti (marcolinuz)
 * (ICQ UIN: 245662445)
 *
 * User: makko
 * Date: 5-dic-2006
 * Time: 13.23.39
 *
 * This class simply echos back the request line and
 * headers that were sent by the client, plus any HTTPS information
 * which is accessible.
 *
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 */

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class RequestDumper {

    public void dumpPost(ServletContext ctx, HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        //value chosen to limit denial of service
        if (req.getContentLength() > 8 * 1024) {
            res.setContentType("text/html");
            ServletOutputStream out = res.getOutputStream();
            out.println("[title]Too big");
            out.println("[body][h1]Error - content length >8k not ");
            out.println("[/h1][/body][/html]");
        } else {
            dumpGet(ctx, req, res);
        }
    }

    public void dumpGet(ServletContext ctx, HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        ServletOutputStream out = res.getOutputStream();
        out.println("");

        out.println("Requested URL:");
        out.println("");
        out.println(" " + getRequestUrl(req));
        out.println("");

        Enumeration enumeration = ctx.getInitParameterNames();
        if (enumeration != null) {
            boolean first = true;
            while (enumeration.hasMoreElements()) {
                if (first) {
                    out.println("Init Parameters");
                    out.println("");
                    first = false;
                }
                String param = (String) enumeration.nextElement();
                out.println(" " + param + ": " + ctx.getInitParameter(param));
            }
            out.println("");
        }

        out.println("");
        out.print("Config information:");
        out.println("");
        print(out, "Context name (getServletContextName)", ctx.getServletContextName());
        Enumeration e = ctx.getInitParameterNames();
        if (e.hasMoreElements()) {
            out.print("Init Parameters:");
            out.println("");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.println(" " + name + ": " + ctx.getInitParameter(name));
            }
            out.println("");
        }
        out.println("");


        out.println("");
        out.print("Request information:");
        out.println("");
        print(out, "Request method (getMethod)", req.getMethod());
        print(out, "Request URI (getRequestURI)", req.getRequestURI());
        print(out, "Request protocol (getProtocol)", req.getProtocol());
        print(out, "Servlet path (getServletPath)", req.getServletPath());
        print(out, "Path info (getPathInfo)", req.getPathInfo());
        print(out, "Path translated (getPathTranslated)", req.getPathTranslated());
        print(out, "Query string (getQueryString)", req.getQueryString());
        print(out, "Content length (getContentLength)", req.getContentLength());
        print(out, "Content type (getContentType)", req.getContentType());
        print(out, "Server name (getServerName)", req.getServerName());
        print(out, "Server port (getServerPort)", req.getServerPort());
        print(out, "Remote user (getRemoteUser)", req.getRemoteUser());
        print(out, "Remote address (getRemoteAddr)", req.getRemoteAddr());
        print(out, "Remote host (getRemoteHost)", req.getRemoteHost());
        print(out, "Authorization scheme (getAuthType)", req.getAuthType());
        out.println("");

        e = req.getHeaderNames();
        if (e.hasMoreElements()) {
            out.print("Request headers:");
            out.println("");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.println(" " + name + ": " + req.getHeader(name));
            }
            out.println("");
        }

        e = req.getParameterNames();
        if (e.hasMoreElements()) {
            out.println("Request parameters (Single Value style):");
            out.println("");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                out.println(" " + name + " = " + req.getParameter(name));
            }
            out.println("");
        }

        e = req.getParameterNames();
        if (e.hasMoreElements()) {
            out.println("Request parameters (Multiple Value style):");
            out.println("");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                String vals[] = (String[]) req.getParameterValues(name);
                if (vals != null) {
                    out.print(" " + name + " = ");
                    out.print(vals[0]);
                    for (int i = 1; i < vals.length; i++)
                        out.print("|" + vals[i]);
                }

                String cipherSuite = (String)
                        req.getAttribute("javax.net.ssl.cipher_suite");

                if (cipherSuite != null) {
                    X509Certificate certChain [] = (X509Certificate[])
                            req.getAttribute("javax.net.ssl.peer_certificates");

                    out.println("HTTPS Information:");
                    out.println("");

                    out.println("Cipher Suite:  " + cipherSuite);

                    if (certChain != null) {
                        for (int i = 0; i < certChain.length; i++) {
                            out.println("client cert chain [" + i + "] = "
                                    + certChain[i].toString());
                        }
                    }

                    // javax.net.ssl.session --> ssl.Session object
                    // ... has above data plus creation and last used dates

                    out.println("");
                }


                out.println("");
            }

            out.println("");
        }
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
        return "A class that shows the request headers sent by the client";
    }
}



