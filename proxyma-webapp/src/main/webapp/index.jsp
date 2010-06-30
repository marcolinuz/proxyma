<%@page contentType="text/html" pageEncoding="UTF-8"
        import="m.c.m.proxyma.ProxymaFacade, 
                m.c.m.proxyma.GlobalConstants,
                java.util.Enumeration"%>
<!--
    Document   : index.jsp
    Description:
       This is the welcome page of the proxyma-console.

    NOTE:
       this software is released under GPL License.
       See the LICENSE of this distribution for more informations.

       @author Marco Casavecchia Morganti (marcolinuz) [marcolinuz-at-gmail.com]
       @version $Id$
-->
<html>
    <head>
        <title>Welcome to the new Proxyma 1.0!</title>
        <meta http-equiv="Content-Type" content="text/html;">
        <link href="stile.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <jsp:include page="header.html" />
        <div id="centrale">
            <div id="titolo">Welcome to Proxyma!</div>
            <div id="gruppo">
                <br/>
                    This is the configuration console of the web application that implements the multiple<br/>
                    reverse-proxy with basic url-rewriting capabilities using the proxyma-core library..
                <br/><br/>
                    Now, all you have to do is to select a context and click the "go" button to start to manage the proxy-folders.
                <br/><br/>
                <span class="form">
                    <form action="console" method="post">
                        <input type="hidden" name="<%=GlobalConstants.COMMAND_PARAMETER %>" value="<%=GlobalConstants.RELOAD_PAGE_COMMAND %>"/>
                        <input type="hidden" name="<%=GlobalConstants.ACTION_PARAMETER %>" value="<%=GlobalConstants.RELOAD_OVERVIEW_ACTION %>"/>
                        <input type="hidden" name="<%=GlobalConstants.TARGET_PARAMETER %>" value="none"/>
                        <select class="risposta" name="<%=GlobalConstants.CONTEXT_PARAMETER%>">
                            <%
                            ProxymaFacade proxyma = new ProxymaFacade();
                            Enumeration<String> contextNames = proxyma.getRegisteredContextNames();
                            while (contextNames.hasMoreElements()) {
                                String contextName = contextNames.nextElement();
                            %>
                            <option value="<%=contextName%>"><%=contextName%></option>
                            <%
                            }
                            %>
                        </select>
                        <input type="submit" value="Go!" />
                    </form>
                </span>
            </div>
        </div>
        <jsp:include page="footer.html" />
    </body>
</html>
