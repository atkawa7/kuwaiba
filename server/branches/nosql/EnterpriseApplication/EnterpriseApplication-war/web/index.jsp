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
        <table class="body" align="center">
            <!-- Header -->
            <tr><td><div style="text-align:center"><a href="http://www.kuwaiba.org"><img alt="http://www.kuwaiba.org" src="images/kuwaiba_logo.png"/></a></div></td></tr>
            <!-- Navigation -->
            <tr>
                <td>
                    <table class="nav_bar">
                        <tr><td><a href="/kuwaiba/">Home</a></td><td><a href="/kuwaiba/?action=1&tool=1">Create default groups</a></td><td><a href="/kuwaiba/?action=1&tool=2">Create/reset admin account</a></td></tr>
                    </table>
                </td>
            </tr>
            <!-- Content -->
            <tr>
                <td class="content">
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
                </td>
            </tr>
            <!-- Footer -->
            <tr>
                <td>
                    <div style="text-align:center; padding-bottom: 5px; padding-top: 5px"><strong><a style="color:cornflowerblue" href="http://www.twitter.com/kuwaiba">Follow us on Twitter</a> | <a href="/kuwaiba/">Home</a> | <a style="color:darkgoldenrod" href="http://webchat.freenode.net/?channels=kuwaiba">Live chat on IRC</a></strong></div>
                    <div style="text-align:center;"><a href="http://www.neotropic.co"><img alt="http://www.neotropic.co" src="images/neotropic_logo.png"/></a></div>
                </td>
            </tr>
        </table>
    </body>
</html>
