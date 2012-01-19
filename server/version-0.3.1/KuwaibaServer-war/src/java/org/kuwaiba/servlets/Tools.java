/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kuwaiba.tools.ToolsBeanRemote;

/**
 * Servlet to execute common tasks such as classmetadata initialization and initial data load
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Tools extends HttpServlet {
    @EJB
    private ToolsBeanRemote tbr;
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/main.css\" />");
        out.println("<title>Kuwaiba Web Management Tools</title>");
        out.println("</head>");
        out.println("<body>");
        try {
            if (request.getParameter("tool") != null){
                if (request.getParameter("tool").equals("diagnostic")){

                    out.println("<h1>Classes with wrong accessors</h1>");
                    for (String myClass : tbr.diagnoseAccessors())
                        out.println(myClass);
                }
                else{
                    if (request.getParameter("tool").equals("rebuild_metadata")){
                        tbr.buildMetaModel();
                        out.println("<h1>Metadata created successfully</h1>");
                    }else{
                        if (request.getParameter("tool").equals("resetadmin")){
                            out.println("<h1>Admin account reset successfully</h1>");
                            tbr.resetAdmin();
                        }else
                            out.println("<h1>Unknown tool</h1>");
                    }
                }
                out.println("<a href=\"/kuwaiba/Tools\">Back</a>");
            }else{
                out.println("<h1>Kuwaiba Management Tools Portal</h1>");
                out.println("<ul>");
                out.println("<li><a href=\"?tool=backup_metadata\">Backup class metadata and containment information</a></li>");
                out.println("<li><a href=\"?tool=rebuild_metadata\">Reset/Build class metadata and containment information</a></li>");
                out.println("<li><a href=\"?tool=restore_metadata\">Restore class metadata from file</a></li>");
                out.println("<li><a href=\"?tool=diagnostic\">Detect missing accessors</a></li>");
                out.println("<li><a href=\"?tool=resetadmin\">Create/Reset admin account </a></li>");
                out.println("</ul>");
            }
        } catch (Exception e){
            out.println("<h1>Oops! Houston, we have a problem</h1>");
            e.printStackTrace();
        }
        finally {
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "This servlet is used to perform basic installation tasks";
    }// </editor-fold>

}
