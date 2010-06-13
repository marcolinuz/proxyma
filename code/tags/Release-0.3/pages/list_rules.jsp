<!-- Created By Marco Casavecchia Morganti (marcolinuz) -->
<!-- (ICQ UIN: 245662445) -->
<%@ page import="org.homelinux.nx01.proxyma.beans.ProxymaRuleBean,
                 org.homelinux.nx01.proxyma.ConfigurationConstants,
                 java.util.Collection,
                 java.util.Iterator" %>

                 <%
                     String proxymaContextPath = (String)request.getAttribute(ConfigurationConstants.proxymaContext);
                     String reverseProxyServletSubPath = (String)request.getAttribute(ConfigurationConstants.reverseProxyServletSubPath);
                 %>
<html>
    <head>
        <title>Welcome to Proxyma Configuration Console</title>
	    <meta http-equiv="Content-Type" content="text/html;">
	    <link href="<%=proxymaContextPath%>/pages/stile.css" rel="stylesheet" type="text/css">
    </head>
    <body>
    <jsp:include page="/pages/header.html" />
    <div id="path" class="percorso">
        You are Here: <a href="<%=proxymaContextPath%>/config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.refreshPage%>&<%=ConfigurationConstants.target%>=null&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.ruleList%>">List Rules</a>
    </div>
    <div id="centrale">
	    <div id="titolo">Proxyma Ruleset List.</div>
        <%
            String message = (String)request.getAttribute(ConfigurationConstants.message);
            if (message != null) {
        %>
           <div class="message">
                <font color="red"><b><center><%=message%></center></b></font>
           </div>
        <%
            }
        %>
        <br/>
        <div id="gruppo">
            <table width="98%" align="center" >
                <tr class="labels">
                    <td width="20%"><b>Proxy Folder</b></td>
                    <td width="50%"><b>Remote Resource URL</b></td>
                    <td align="center"  width="10%"><b>Status</b></td>
                    <td align="center"  width="10%"><b>Modify</b></td>
                    <td align="center"  width="10%"><b>Remove</b></td>
                </tr>
<%
    Collection rules = (Collection)request.getAttribute(ConfigurationConstants.ruleCollection);
    Iterator iter = rules.iterator();
    boolean even = true;
    String rowClass=null;
    while (iter.hasNext()) {
        ProxymaRuleBean rule = (ProxymaRuleBean)iter.next();
        if (even)
            rowClass="even";
        else
            rowClass="odd";
        even = !even;
%>
                        <tr class="<%=rowClass%>">
                            <td><a href="<%=proxymaContextPath%>/<%=reverseProxyServletSubPath%>/<%=rule.getProxyFolder()%>/"><%=rule.getProxyFolder()%></a></td>
                            <td><a href="<%=rule.getProxyPassHost()%>"><%=rule.getProxyPassHost()%></a></td>
                            <td align="center" >
                            <% if (rule.isRuleEnabled()) { %>
                                    <a href="<%=proxymaContextPath%>/config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.updateRule%>&<%=ConfigurationConstants.target%>=<%=rule.getProxyFolder()%>&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.disable%>"><img  alt="enabled" src="<%=proxymaContextPath%>/img/running.png" /></a>
                            <% } else { %>
                                    <a href="<%=proxymaContextPath%>/config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.updateRule%>&<%=ConfigurationConstants.target%>=<%=rule.getProxyFolder()%>&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.enable%>"><img  alt="disabled" src="<%=proxymaContextPath%>/img/locked.png" /></a>
                            <% } %>
                            </td>
                            <td align="center">
                                    <a href="<%=proxymaContextPath%>/config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.updateRule%>&<%=ConfigurationConstants.target%>=<%=rule.getProxyFolder()%>&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.edit%>"><img  alt="Edit Rule" src="<%=proxymaContextPath%>/img/modify.png" /></a>
                            </td>
                            <td align="center">
                                    <a href="<%=proxymaContextPath%>/config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.updateRule%>&<%=ConfigurationConstants.target%>=<%=rule.getProxyFolder()%>&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.delete%>" onClick="return confirm('Are you sure you want to Remove this rule?');"><img alt="Remove Rule"  src="<%=proxymaContextPath%>/img/remove.png" /></a>
                            </td>
                        </tr>
<%
    }
%>
            </table>
            <br>
            <div align="center" class="submit">
                <form action="<%=proxymaContextPath%>/config" method="post">
                    <input type="hidden" name="<%=ConfigurationConstants.method%>" value="<%=ConfigurationConstants.addRule%>" />
                    <input type="hidden" name="<%=ConfigurationConstants.target%>" value="null" />
                    <input type="hidden" name="<%=ConfigurationConstants.action%>" value="null" />
                    <input type="submit" value="Create new Rule"/>
                </form>
            </div>
            <div align="center" class="endlinks" >
                <table  width="98%">
                    <tr>
                        <td width="33%" align="right">
                            <form action="<%=proxymaContextPath%>/config" method="post">
                                <input type="hidden" name="<%=ConfigurationConstants.method%>" value="<%=ConfigurationConstants.importRules%>" />
                                <input type="hidden" name="<%=ConfigurationConstants.target%>" value="null" />
                                <input type="hidden" name="<%=ConfigurationConstants.action%>" value="null" />
                                <input type="submit" value="Import Rules"/>
                            </form>
                        </td>
                    <td width="34%">&nbsp</td>
                        <td width="33%" align="left">
                            <form action="<%=proxymaContextPath%>/config" method="post">
                                <input type="hidden" name="<%=ConfigurationConstants.method%>" value="<%=ConfigurationConstants.exportRules%>" />
                                <input type="hidden" name="<%=ConfigurationConstants.target%>" value="null" />
                                <input type="hidden" name="<%=ConfigurationConstants.action%>" value="null" />
                                <input type="submit" value="Export Rules"/>
                            </form>
                        </td>
                    </tr>
                </table>
            <div>
        </div>
	</div>
	<jsp:include page="/pages/footer.html" />
    </body>
</html>

