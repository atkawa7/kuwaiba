/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kuwaiba.beans.ToolsBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@WebServlet(name="Tools", urlPatterns={"/Tools"})
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
        out.println("<link rel=\"shortcut icon\" href=\"images/favicon.ico\" />");
        out.println("<title>Kuwaiba Management Tools</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<br/>");
        out.println("<div style=\"text-align:center\"><a href=\"http://www.kuwaiba.org\"><img alt=\"http://www.kuwaiba.org\" src=\"images/kuwaiba_logo.png\"/></a></div>");

        if (request.getParameter("tool") != null){
            if (request.getParameter("tool").equals("resetadmin")){
                try {
                    tbr.resetAdmin();
                    out.println("<h1>Success</h1>");
                    out.println("<div>Admin account reset successfully</div>");
                } catch (Exception ex) {
                    Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, ex.getMessage());
                    out.println("<h1>Error</h1>");
                    out.println(ex.getMessage());
                }
            }else{
                if (request.getParameter("tool").equals("default_groups")){
                    try {
                        tbr.createDefaultGroups();
                        out.println("<h1>Success</h1>");
                        out.println("<div>Default groups created successfully</div>");
                    } catch (ServerSideException ex) {
                        Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, ex.getMessage());
                        out.println("<h1>Error</h1>");
                        out.println(ex.getMessage());
                    }
                }else{
                    out.println("<h1>Error</h1>");
                    out.println("<div>Unknown tool</div>");
                }
            }
        } else {
            out.println("<br/>");
            out.println("<div id=\"content\">");
            out.println("<ul>");
//                out.println("<li><a href=\"?tool=backup_metadata\">Backup class metadata and containment information</a></li>");
//                out.println("<li><a href=\"?tool=rebuild_metadata\">Refresh Cache</a></li>");
//                out.println("<li><a href=\"?tool=restore_metadata\">Restore class metadata from file</a></li>");
            out.println("<li><a href=\"?tool=default_groups\">Create default groups</a>: Create the default groups (Administrators and Users). You must create them BEFORE creating the default admin user</li>");
            out.println("<br/>");
            out.println("<li><a href=\"?tool=resetadmin\">Create/Reset admin account</a>: Creates a default account with administrator privileges (<strong>user:</strong>admin, <strong>password:</strong>kuwaiba). The default groups MUST exist prior to call this action</li>");
            out.println("</ul>");
            out.println("</div>");
       }


        out.println("<div style=\"padding-top:300px;\">");
        out.println("<div style=\"text-align:center; padding: 5px 5px 5px 5px\"><a href=\"/kuwaiba/\">Home</a></div>");
        out.println("<div style=\"text-align:center;\"><a href=\"http://www.neotropic.co\"><img alt=\"http://www.neotropic.co\" src=\"images/neotropic_logo.png\"/></a></div>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
        out.close();

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
