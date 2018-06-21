/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.servmanager.views;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.List;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.export.SceneExporter;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;

/**
 * This component implements the End to End view for SDH/MPLS services
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public final class EndToEndView extends Panel {
     
    public EndToEndView(RemoteObjectLight service, WebserviceBean wsBean, String ipAddress, String sessionId) {
        try {

            List<RemoteViewObjectLight> objectViews = wsBean.getObjectRelatedViews(service.getId(), service.getClassName(), 10, -1, ipAddress, sessionId);
            RemoteViewObject theView = null;
            for (RemoteViewObjectLight serviceView : objectViews) {
                if (EndToEndViewScene.VIEW_CLASS.equals(serviceView.getViewClassName())) {
                    theView = wsBean.getObjectRelatedView(service.getId(), service.getClassName(), serviceView.getId(), ipAddress, sessionId); 
                    break;
                }
            }

            EndToEndViewScene scene = new EndToEndViewScene(wsBean, sessionId, ipAddress);

            scene.render(service); //First we render the default view with all the resources associated to the service
            
            if (theView != null) //Then, if there's a view prevously saved, we modify the position  of the nodes and connections in the default view. This way we manage the new nodes and those that disappeared from the last time the view was saved
                scene.render(theView.getStructure());
            
            scene.render(service);
//            if (scene.getNodes().isEmpty()) {
//                VerticalLayout content = new VerticalLayout(new Label(service.toString() + ": This service does not have any resources associated to"));
//                setContent(content);
//            } else {
//                SceneExporter.createImage(scene,
//                        new File("/programs/glassfish4/glassfish/domains/domain1/docroot/scene/" + service.getId() + ".png"),
//                        SceneExporter.ImageType.PNG,
//                        SceneExporter.ZoomType.ACTUAL_SIZE,
//                        false,
//                        false,
//                        100,
//                        0,  //Not used
//                        0);

            //}
            setSizeUndefined();
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
            //ex.printStackTrace();
        }
        
        
    }
    
}
