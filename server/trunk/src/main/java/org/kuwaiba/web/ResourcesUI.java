 /*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.cdi.server.VaadinCDIServlet;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import static org.kuwaiba.apis.web.gui.resources.ResourceFactory.DEFAULT_ICON_COLOR;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.openide.util.Exceptions;

/**
 * A dummy view used to serve resources like images (class icons, mostly), HTML reports, etc
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
@CDIUI("resources")
public class ResourcesUI extends UI {
    /**
     * Resource image for images
     */
    public static final String TYPE_ICON = "icon";
    @Inject
    private CDIViewProvider viewProvider;
    /**
     * The reference to the back end bean
     */
    @Inject
    WebserviceBean wsBean;

    @Override
    protected void init(VaadinRequest request) {
        VaadinSession.getCurrent().addRequestHandler(new RequestHandler() {
            @Override
            public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException  {
                if (request.getParameter("type") != null) {
                    System.out.println("Entra 1!");
                    switch(request.getParameter("type")) {
                        case ResourcesUI.TYPE_ICON:
                            if (request.getParameter("class") == null) {
                                response.setStatus(400); //Bad request
                                return false;
                            } else {
                                try {
                                    System.out.println("Entra 2!");
//                                    RemoteClassMetadata classMetadata = wsBean.getClass(request.getParameter("class"),
//                                            Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                                    //response.getOutputStream().write(classMetadata.getIcon() == null ? createRectangleIcon(Color.BLACK, 20, 20) : classMetadata.getIcon());
                                    response.getOutputStream().write(createRectangleIcon(Color.ORANGE, 20, 20));
                                } catch (Exception ex) {
                                    response.setStatus(500); //Internal server error
                                    return false;
                                }
                                response.setContentType("image/png");
                            }
                            
                            break;
                        default:
                            
                            return false;
                    }
                } else {
                    response.setStatus(400); //Bad request
                    return false;
                }
                    
                    
                
                return true;
            }
        });
    }
    
    private byte[] createRectangleIcon(Color color, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color == null ? DEFAULT_ICON_COLOR : color);
            graphics.fillRect(0, 0, width, height);
                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    @WebServlet(value = "/resources/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = ResourcesUI.class)
    public static class Servlet extends VaadinCDIServlet {  }
}
