<!-- Created By Marco Casavecchia Morganti (marcolinuz) -->
<!-- (ICQ UIN: 245662445) -->
<%@ page import="org.homelinux.nx01.proxyma.ConfigurationConstants"%>
<html>
    <head>
        <title>Welcome to Proxyma!</title>
	    <meta http-equiv="Content-Type" content="text/html;">
	    <link href="pages/stile.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <jsp:include page="/pages/header.html" />
        <div id="centrale">
	        <div id="titolo">Welcome to Proxyma!</div>
	        <div id="gruppo">
                <br/>
                    This is a webapp that implements <a href="fetch/">here</a> a multiple
                    reverse-proxy with basic url-rewriting capabilities..
                <br/><br/>
                    Now, all you have to do is to go to the <a href="config?<%=ConfigurationConstants.method%>=<%=ConfigurationConstants.refreshPage%>&<%=ConfigurationConstants.target%>=null&<%=ConfigurationConstants.action%>=<%=ConfigurationConstants.ruleList%>">Configuration Console</a>
                    and add some simple proxy rules.. ;O)
                <br/>
            </div>
        </div>
        <jsp:include page="/pages/footer.html" />
    </body>
</html>
