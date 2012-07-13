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
 * Servlet to serve the main requests from the Tools Portal
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
        PrintWriter out = response.getWriter();
        if (!request.getRemoteAddr().equals("127.0.0.1")){ //This servlet can only be called/included from the local server
            out.println("<h1>Error</h1>");
            out.println("You can't access this servlet directly");
            return;
        }

        int tool = 0;
        try{
                tool = request.getParameter("tool") == null ? 0 : Integer.parseInt(request.getParameter("tool"));
        }catch(NumberFormatException mfe){ }
        
        switch (tool){
            case 1:
                try {
                    out.println("<h1>Create default groups</h1>");
                    tbr.createDefaultGroups();
                    out.println("<h2>Success</h2>");
                    out.println("<div id=\"content\">Default groups created successfully</div>");
                } catch (ServerSideException ex) {
                    Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, ex.getMessage());
                    out.println("<h2 class=\"error\">Error</h2>");
                    out.println("<div id=\"content\">"+ex.getMessage()+"</div>");
                }
                break;
            case 2:
                try {
                    out.println("<h1>Create/Reset Admin Account</h1>");
                    tbr.resetAdmin();
                    out.println("<h2>Success</h2>");
                    out.println("<div id=\"content\">Admin account reset successfully</div>");
                } catch (Exception ex) {
                    Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, ex.getMessage());
                    out.println("<h2 class=\"error\">Error</h2>");
                    out.println("<div id=\"content\">"+ex.getMessage()+"</div>");
                }
                break;
            default:
                out.println("<h2 class=\"error\">Error</h2>");
                out.println("<div id=\"content\">Unknown tool</div>");
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
