<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<portlet:actionURL var="url" portletMode="view"/>
<FORM method="POST" action="<%= url %>">
<TABLE>
<TR><TD>Greeting</TD>
    <TD><INPUT type="text" name="greeting"
               value="<%= renderRequest.getParameter("greeting") %>"/>
    </TD>
    <TD colspan="2"><INPUT type="submit" value="Save"/></TD>
</TR>
</TABLE>
</FORM>