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
package org.kuwaiba.web.modules.services.views;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.export.SceneExporter;

/**
 * This component implements the End to End view for SDH/MPLS services
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public final class EndToEndView extends Panel {
     
    public EndToEndView(RemoteObjectLight service, WebserviceBeanLocal wsBean, String ipAddress, String sessionId) {
        try {
            ViewInfo currentView = null;

            ViewInfoLight[] objectViews = wsBean.getObjectRelatedViews(service.getId(), service.getClassName(), 10, -1, ipAddress, sessionId);

            for (ViewInfoLight serviceView : objectViews) {
                if (EndToEndViewSimpleScene.VIEW_CLASS.equals(serviceView.getViewClassName())) {
                    currentView = wsBean.getObjectRelatedView(service.getId(), service.getClassName(), serviceView.getId(), ipAddress, sessionId); 
                    break;
                }
            }

            EndToEndViewSimpleScene scene = new EndToEndViewSimpleScene(wsBean, sessionId, ipAddress);

            if (currentView != null)
                scene.render(currentView.getStructure());
            
            //scene.render(service);
            if (scene.getNodes().isEmpty()) {
                VerticalLayout content = new VerticalLayout(new Label(service.toString() + ": This service does not have any resources associated to"));
                setContent(content);
            } else {
                SceneExporter.createImage(scene,
                        new File("/programs/glassfish4/glassfish/domains/domain1/docroot/scene/" + service.getId() + ".png"),
                        SceneExporter.ImageType.PNG,
                        SceneExporter.ZoomType.ACTUAL_SIZE,
                        false,
                        false,
                        100,
                        0,  //Not used
                        0);

                VerticalLayout content = new VerticalLayout(new Embedded(service.toString(), new ExternalResource("http://localhost:8080/scene/" + + service.getId() + ".png")));
                setContent(content);
            }
            setSizeUndefined();
        } catch (Exception ex) {
            //NotificationsUtil.showError(ex.getMessage());
            ex.printStackTrace();
        }
        
        
    }
    
}
