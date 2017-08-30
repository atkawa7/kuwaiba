package com.neotropic.servlet;

/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

import com.neotropic.job.schedule.RunJobRemote;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet use like client to execute jobs
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@WebServlet(urlPatterns = {"/"})
public class RunJobs extends HttpServlet {
    
    @EJB
    private RunJobRemote runJobSnmpLeaf;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            String host = request.getParameter("host") != null ? request.getParameter("host") : "";
            String community = request.getParameter("community") != null ? request.getParameter("community") : "";
            String oid = request.getParameter("oid") != null ? request.getParameter("oid") : "";
            String job = request.getParameter("job") != null ? request.getParameter("job") : "";
            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>SNMP</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Gets SNMP OIDs</h1>");
            out.println("<form>");
            out.println("<table border = \"yes\">");
            
            out.println("<tr>");
            out.println("<td>Host</td>");
            out.println("<td><input type=\"text\" name=\"host\" value=\"" + host + "\"/></td>");
            out.println("<td>e.g. udp:127.0.0.1/161</td>");
            out.println("</tr>");
            
            out.println("<tr>");
            out.println("<td>Community</td>");
            out.println("<td><input type=\"text\" name=\"community\" value=\"" + community + "\"/></td>");
            out.println("<td>e.g. public </td>");
            out.println("</tr>");
            
            out.println("<tr>");
            out.println("<td>OID</td>");
            out.println("<td><input type=\"text\" name=\"oid\" value=\"" + oid + "\"/></td>");
            out.println("<td>Set a oid or a set of oids, separated by \",\"</td>");
            out.println("</tr>");
            
            out.println("<tr>");
            out.println("<td>Job</td>");
            out.println("<td><input type=\"text\" name=\"job\" value=\"" + job + "\"/></td>");
            out.println("<td>Possible values: snmpLeaf, snmpTable</td>");
            out.println("</tr>");

            out.println("<tr>");
            out.println("<td></td>");
            out.println("<td><input type=\"submit\" name=\"run\" value=\"Run\"/></td>");
            out.println("</tr>");
            
            out.println("</table>");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
            
            Properties properties = new Properties();
            
            if (host != null && !host.isEmpty())
                properties.setProperty("host", host);
            
            if (community != null && !community.isEmpty())
                properties.setProperty("community", community);
            
            if (oid != null && !oid.isEmpty())
                properties.setProperty("oid", oid);
            
//            if (job != null && !job.isEmpty())
//                properties.setProperty("job", job);           
           
            if ("snmpLeaf".equals(job))
                runJobSnmpLeaf.runSnmpLeafJob(properties);
            if ("snmpTable".equals(job))
                runJobSnmpLeaf.runSnmpTableJob(properties);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
