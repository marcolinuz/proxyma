<!-- Created By Marco Casavecchia Morganti (marcolinuz) -->
<!-- (ICQ UIN: 245662445) -->
<%@ page import="org.homelinux.nx01.proxyma.beans.RuleBean,
                 org.homelinux.nx01.proxyma.core.RuleSetsPool,
                 org.homelinux.nx01.proxyma.core.ProxymaConstants,
                 java.util.Enumeration,
                 org.homelinux.nx01.proxyma.core.RuleFactory,
                 org.homelinux.nx01.proxyma.core.RewriterConstants,
                 org.homelinux.nx01.proxyma.ConfigurationConstants" %>
                 <%
                     String proxymaContextPath = (String)request.getAttribute(ConfigurationConstants.proxymaContext);
                     String reverseProxyServletSubPath = (String)request.getAttribute(ConfigurationConstants.reverseProxyServletSubPath);
                 %>

<html>
    <head>
        <title>Proxyma Configuration Console</title>
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
                RuleBean rule = (RuleBean) request.getAttribute(ConfigurationConstants.ruleBean);
                RuleBean oldRule = rule;
                String action = ConfigurationConstants.modifyRule;
                if (rule == null) {
                    action = ConfigurationConstants.addNewRule;
                    RuleFactory factory = new RuleFactory();
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
                                NOTE: If not provided, an ending "/" (slash) is automatically added.
                            </td>
                        </tr>
                        <tr class="even">
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
                        <tr class="odd">
                            <td align="center">
                                Max Size for Cached Resource
                            </td>
                            <td align="left">
                                <input type="text" class="risposta" name="<%=ConfigurationConstants.maxCachedResourceSize%>" size="40" maxlength="50" value="<%=rule.getMaxCachedResourceSize()%>" />
                            </td>
                            <td align="left">
                                This option sets the maximum allowed size for
                                a cached resource into the cache subsystem.
                                If the size of an object is grater than this value
                                Proxyma will never store it into its cache.<br/>
                                The default value is 5Mb (5242880 Bytes) and
                                it should be a good choice for any use.<br/>
                                Please note that this parameter could be useful
                                to avoid chache saturation when great files have to be passed to the clients
                                (that can cause high memory and disk consumption).<br/>
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
                                        if (rule.getJavascriptHandlingMode() == RewriterConstants.PROCESS) {
                                    %>
                                        <option value = <%=ProxymaConstants.PROCESS%> selected="selected"><%=ProxymaConstants.PROCESS%></option>
                                        <option value = <%=ProxymaConstants.REMOVE%> ><%=ProxymaConstants.REMOVE%></option>
                                    <%
                                        } else {

                                    %>
                                        <option value = <%=ProxymaConstants.REMOVE%> selected="selected"><%=ProxymaConstants.REMOVE%></option>
                                        <option value = <%=ProxymaConstants.PROCESS%> ><%=ProxymaConstants.PROCESS%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </td>
                            <td align="left">
                                If you enable the Url rewrite engine
                                with this parameter you can control javascript
                                handling.<br/>
                                There are 2 possible values:
                                <ul>
                                <li><%=ProxymaConstants.PROCESS%> -> try to rewrite urls into javascript code.</li>
                                <li><%=ProxymaConstants.REMOVE%> -> remove all the javascript code.</li>
                                </ul>
                            </td>
                        </tr>
                        <tr class="odd">
                            <td align="center" >
                                Enable Cache
                            </td>
                            <td align="left" >
                                <%
                                    if (rule.isCacheEnabled()) {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isCacheEnabled%>" value="true" checked="true"  />
                                <%
                                    } else {
                                %>
                                        <input type="checkbox" class="risposta" name="<%=ConfigurationConstants.isCacheEnabled%>" value="true" />
                                <%
                                    }
                                %>
                            </td>
                            <td align="left">
                                Enables or disables the Cache subsystem for this rule.<br/>
                                If this is NOT set, the rule will not use the cache subsystem, so ALL the requests will follow the same path:<br />
                                * Client Browser --> Proxyma --> Resource Server --> Proxyma --> Client Browser.<br/>
                                If this flag IS SET, Proxyma will search into its cache subsystem a resource that matches the wanted url. If the resource is found the flow of the request will be faster: <br />
                                * Client Brobser --> Proxyma (Memory/Disk Chache) --> Client Browser.
                            </td>
                        </tr>
                        <tr class="even">
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
                        <tr class="odd">
                            <td align="center" >
                                Logging Policy:
                            </td>
                            <td align="left" >
                                <select name="<%=ConfigurationConstants.loggingPolicy%>" class="risposta">
                                    <%
                                        for (int i=0; i<ProxymaConstants.LOGGING_POLICY_STRING.length; i++) {
                                            if (rule.getLoggingPolicy() == i) {
                                    %>
                                                <option value = <%=i%> selected="selected"><%=ProxymaConstants.LOGGING_POLICY_STRING[i]%></option>
                                    <%
                                            } else {

                                    %>
                                                <option value = <%=i%> ><%=ProxymaConstants.LOGGING_POLICY_STRING[i]%></option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                            <td align="left">
                                This parameter sets the Logging Policy. Its value could be:<br/>
                                <ul>
                                    <li>
                                        None -&gt; Nothing will be logged proxyma doesn't keep track of any client request.<br/>Only the error log will be active.
                                    </li>
                                    <li>
                                        Production -&gt; A "proxyma_access.log" file will be created into the "WEB-INF/logs" directory for keeping track of any requested (and served) page.
                                    </li>
                                    <li>
                                        On-Line Audit -&gt; Requested page will be rendered in plain/text with many useful informations.
                                    </li>
                                    <li>
                                        On-File Audit -&gt; Requested page will be sent to the "proxyma_audit.log" into the "WEB-INF/logs" directory (same informations as used in on-line debug) without affecting the user's request/response.
                                    </li>
                                </ul>
                                Please note that any error will be logged into the "proxyma_error.log" file.<br/>
                                Note also that if any of the two "audit" modes is enabled the "proxyma_access.log" will work as well as if the rule is in "Production" mode.
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
                        <tr class="even">
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
                        <tr class="odd">
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
