<%-- 
    Document   : index
    Created on : Jul 12, 2012, 2:04:19 PM
    Author     : Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
--%>

<%-- Hell yeah! The good old controller :) --%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Kuwaiba Open Network Inventory - Network Inventory for the masses</title>
        <link rel="stylesheet" type="text/css" href="css/main.css" />
        <link rel="shortcut icon" href="images/favicon.ico" />
    </head>
    <body>

         <div id="wrapper"align="center">
            <div id="header">
                <a href="http://www.kuwaiba.org"><img alt="http://www.kuwaiba.org" src="images/kuwaiba_logo.png" border="0"/></a>
            </div>
            <div id="content" align="center">
                <div id="maincontent">
                    <%
                        int action = 0, tool = 0;
                        try{
                            action = request.getParameter("action") == null ? 0 : Integer.parseInt(request.getParameter("action"));
                        }catch(NumberFormatException mfe){ }
                        try{
                            tool = request.getParameter("tool") == null ? 0 : Integer.parseInt(request.getParameter("tool"));
                        }catch(NumberFormatException mfe){ }
                        switch (action){
                            case 0: //Default action, show home page
                    %>
                                <%@include file="html/index.html" %>
                    <%
                                break;
                            case 1: //Call the Tools bean
                    %>
                                <jsp:include page="Tools?tools=<%=tool%>" flush="true" />
                    <%
                                break;
                            default:
                    %>
                                <h2>Error</h2>
                                <div id="content">Unknown action</div>
                    <%
                        }
                    %>
                </div>
                <div id="infocontent">
                    <p><a href="http://kuwaiba.org/wiki" target="_blank">Wiki</a></p>
                    <p><a href="http://neotropic.co/blogs" target="_blank">Blog</a></p>
                    <p><a href="http://sourceforge.net/projects/kuwaiba/files/Docs/" target="_blank">Documentation</a></p>
                    <p><a href="http://www.twitter.com/kuwaiba" target="_blank">Twitter</a></p>
                    <p><a href="http://webchat.freenode.net/?channels=kuwaiba" target="_blank">Live chat</a></p>
                    <p><a  style="color:#ff6600" href="http://www.neotropic.co/" target="_blank">Commercial Support</a></p>

                </div>
            </div>
            <div id="footer" align="center">
                <div style="text-align:center;"><a href="http://www.neotropic.co"><img alt="http://www.neotropic.co" src="images/neotropic_logo.png"/></a></div>
            </div>
        </div>
    </body>
</html>
