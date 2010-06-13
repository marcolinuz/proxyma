<!-- Created By Marco Casavecchia Morganti (marcolinuz) -->
<!-- (ICQ UIN: 245662445) -->
<%@ page import="org.homelinux.nx01.proxyma.beans.ProxymaRuleBean,
                 org.homelinux.nx01.proxyma.core.ProxymaRuleSetsPool,
                 org.homelinux.nx01.proxyma.core.ProxymaConstants,
                 java.util.Enumeration,
                 org.homelinux.nx01.proxyma.core.ProxymaRuleFactory,
                 org.homelinux.nx01.proxyma.core.RewriterConstants,
                 org.homelinux.nx01.proxyma.ConfigurationConstants" %>
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
        <jsp:include page="/pages/header.html" />
    <div id="path" class="percorso">
        You are Here: <a href="<%=proxymaContextPath%>/config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.refreshPage%>&<%=ConfigurationConstants.target%>=null&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.ruleList%>">List Rules</a> -> Rule Edit
    </div>
        <br />
        <div id="centrale">
            <div id="titolo">
                Proxyma Rule Edit Page
            </div>
<%
    ProxymaRuleBean rule = (ProxymaRuleBean) request.getAttribute(ConfigurationConstants.ruleBean);
    ProxymaRuleBean oldRule = rule;
    String action = ConfigurationConstants.modifyRule;
    if (rule == null) {
        action = ConfigurationConstants.addNewRule;
        ProxymaRuleFactory factory = new ProxymaRuleFactory();
        rule = factory.getNewRuleInstance(ProxymaConstants.DEFAULT_NEW_RULE_FOLDER, ProxymaConstants.DEFAULT_NEW_RULE_PROXYPASSHOST);
    }
%>
            <div id="gruppo">
                <%
                    String message = (String)request.getAttribute(ConfigurationConstants.message);
                    if (message != null) {
                %>
                    <div class="message">
                        <font color="red"><center><b><%=message%></b></center></font>
                    </div>
                <%
                    }
                %>
                <form action="<%=proxymaContextPath%>/config" method="post" >
                        <input type="hidden" name="<%=ConfigurationConstants.method%>" value="<%=ConfigurationConstants.editRule%>" />
                <%
                        if (oldRule == null) {
                %>
                        <input type="hidden" name="<%=ConfigurationConstants.target%>" value="null" />
                <%
                    } else {
                %>
                        <input type="hidden" name="<%=ConfigurationConstants.target%>" value="<%=oldRule.getProxyFolder()%>" />
                <%
                    }
                %>
                        <input type="hidden" name="<%=ConfigurationConstants.action%>" value="<%=action%>" />

                        <table width="95%" align="center">
                        <tr class="labels">
                            <td align="center" width="10%" >
                                <b>Parameter Name</b>
                            </td>
                            <td align="left" width="40%" >
                                <b>Parameter Value</b>
                            </td>
                            <td align="left" width="50%" >
                                <b>Parameter Description</b>
                            </td>
                        </tr>
                        <tr class="even">
                            <td align="center">
                                Proxy Folder <font color=red>(required)</font>
                            </td>
                            <td align="left">
                                <input type="text" class="risposta" name="<%=ConfigurationConstants.proxyFolder%>" size="40" maxlength=60" value="<%=(rule.getProxyFolder()==null)?ProxymaConstants.EMPTY_STRING:rule.getProxyFolder()%>"/>
                            </td>
                            <td align="left">
                                In this field you can set the name and the consequent path of the rule.<br/>
                                It's a required parameter and it must be a valid string, note that it can't countain the "/" character.<br/>
                                The proxed resource will be mapped in the follow path: "<%=proxymaContextPath + "/" + reverseProxyServletSubPath%>/${ProxyFolderValue}".
                            </td>
                        </tr>
                        <tr class="odd">
                            <td align="center">
                                Masquerading Host <font color=red>(required)</font>
                            </td>
                            <td align="left">
                                <input type="text" class="risposta" name="<%=ConfigurationConstants.proxyPassHost%>" size="40" maxlength="255" value="<%=(rule.getProxyPassHost()==null)?ProxymaConstants.EMPTY_STRING:rule.getProxyPassHost()%>" />
                            </td>
                            <td align="left">
                                This a required parameter that countains the resource URL that will be fetched by this rule.<br/>
                                NOTE: If not provided, an endign "/" (slash) is automatically added.
                            </td>
                        </tr>
                        <tr class="even">
                            <td align="center">
                                Proxy User
                            </td>
                            <td align="left">
                                <input type="text" class="risposta" name="<%=ConfigurationConstants.proxyUser%>" size="40" maxlength="50" value="<%=(rule.getProxyUser()==null)?ProxymaConstants.EMPTY_STRING:rule.getProxyUser() %>" />
                            </td>
                            <td align="left">
                                If proxyma needs a proxy authentication to connect to the masqueraded host you must specify the username to use here.<br/>
                                Note: if you don't understand what i mean you don't need to set this.
                            </td>
                        </tr>
                        <tr class="odd">
                            <td align="center">
                                Proxy Password
                            </td>
                            <td align="left">
                                <input type="password" class="risposta" name="<%=ConfigurationConstants.proxyPassword%>" size="40" maxlength="50" value="<%=(rule.getProxyPassword()==null)?ProxymaConstants.EMPTY_STRING:rule.getProxyPassword()%>" />
                            </td>
                            <td align="left">
                                If proxyma needs a proxy authentication to connect to the masqueraded host you must specify the password to use here.<br/>
                                Note: if you don't understand what i mean you don't need to set this.
                            </td>
                        </tr>
                        <tr class="even">
                            <td align="center">
                                New/Forced Context
                            </td>
                            <td align="left">
                                <input type="text" class="risposta" name="<%=ConfigurationConstants.newContext%>" size="40" maxlength="50" value="<%=(rule.getNewContext()==null)?ProxymaConstants.EMPTY_STRING:rule.getNewContext()%>" />
                            </td>
                            <td align="left">
                                This parameter is useful if proxyma is deployed behind another reverse-proxy (like Apache in reverse proxy mode).<br/>
                                With this parameter you can force the url rewrite engine to use the provided URL as source for the rewritten links.<br/>
                                Normally you must leave this blank because proxyma uses its default context path.<br/>
                                Note that this path must end with a "/".
                            </td>
                        </tr>
                        <tr class="odd">
                            <td align="center">
                                Max POST-Size
                            </td>
                            <td align="left">
                                <input type="text" class="risposta" name="<%=ConfigurationConstants.maxPostSize%>" size="40" maxlength="50" value="<%=rule.getMaxPostSize()%>" />
                            </td>
                            <td align="left">
                                This option sets the maximum allowed size for
                                the POST data that comes from the client browser.<br/>
                                The default value is 64Kb (65536 Bytes) and
                                it should be a good choice for any use.<br/>
                                Please note that this parameter could be useful
                                to avoid some kind of DOS attaks based on large
                                POSTS (that cause high memory consumption).<br/>
                                To disable this limit set it to 0 (zero).
                            </td>
                        </tr>
                        <tr class="even">
                            <td align="center">
                                Javascript Handling Mode
                            </td>
                            <td align="left">
                                <select name="<%=ConfigurationConstants.javascriptHandlingMode%>" class="risposta">
                                    <%
                                        if (rule.getJavascriptHandlingMode() == RewriterConstants.REWRITE) {
                                    %>
                                        <option value = <%=ProxymaConstants.REWRITE%> selected="selected"><%=ProxymaConstants.REWRITE%></option>
                                        <option value = <%=ProxymaConstants.REMOVE%> ><%=ProxymaConstants.REMOVE%></option>
                                    <%
                                        } else {

                                    %>
                                        <option value = <%=ProxymaConstants.REMOVE%> selected="selected"><%=ProxymaConstants.REMOVE%></option>
                                        <option value = <%=ProxymaConstants.REWRITE%> ><%=ProxymaConstants.REWRITE%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                            <td align="left">
                                If you enable the Url rewrite engine
                                with this parameter you can control javascript
                                handling.<br/>
                                There are 2 possible values:<br/>
                                <%=ProxymaConstants.REWRITE%> -> try to rewrite urls into javascript code <br/>
                                <%=ProxymaConstants.REMOVE%> -> remove all the javascript code. <br/>
                            </td>
                        </tr>
                        <tr class="odd">
                            <td align="center" >
                                Enable Rewriter Engine
                            </td>
                            <td align="left" >
                                <%
                                    if (rule.isRewriteEngineEnabled()) {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isRewriteEngineEnabled%>" value="true" checked="true"  />
                                <%
                                    } else {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isRewriteEngineEnabled%>" value="true" />
                                <%
                                    }
                                %>
                            </td>
                            <td align="left">
                                Enables or disables the rewrite engine.<br/>
                                If this is set, the links into fetched pages
                                will be rewritten in according to the current proxyma
                                context path (or the "New/Forced Context" value if provided).
                            </td>
                        </tr>
                        <tr class="even">
                            <td align="center" >
                                Enable this Rule
                            </td>
                            <td align="left" >
                                <%
                                    if (rule.isRuleEnabled()) {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isRuleEnabled%>" value="true" checked="true"  />
                                <%
                                    } else {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isRuleEnabled%>" value="true" />
                                <%
                                    }
                                %>
                            </td>
                            <td align="left">
                                This flag sets if the rule should be enabled or disabled.<br/>
                                If is not set the rule will not work and a FORBIDDEN return code is sent back to the client browser.
                            </td>
                        </tr>
                        <tr class="odd">
                            <td align="center" >
                                Enable Debug Mode
                            </td>
                            <td align="left" >
                                <%
                                    if (rule.isDebugModeEnabled()) {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isDebugModeEnabled%>" value="true" checked="true"  />
                                <%
                                    } else {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isDebugModeEnabled%>" value="true" />
                                <%
                                    }
                                %>
                            </td>
                            <td align="left">
                                Enables or disables the debug mode.<br/>
                                If this flag is set, the output page will be
                                rendered in plain/text with many useful informations.
                            </td>
                        </tr>
                    </table>
                    <table width="95%" align="center">
                        <tr class="submit">
                            <td width="100%" align="center" >
                                <input type="submit" value="Add / Modify Rule" />
                            </td>
                        </tr>
                    </table>
                </html:form>
            </div>
        </div>
        <jsp:include page="/pages/footer.html" />
    </body>
</html:html>
