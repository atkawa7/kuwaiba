/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.Page;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * This component implements the End to End view for SDH/MPLS services
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public final class EndToEndView extends Div {
    
    public EndToEndView(RemoteObjectLight service, WebserviceBean wsBean) {
        RemoteSession session = ((RemoteSession) UI.getCurrent().getSession().getAttribute(RemoteSession.class));
        EndToEndViewScene scene = new EndToEndViewScene(service, wsBean, session);
        try {
            List<RemoteViewObjectLight> objectViews = wsBean.getObjectRelatedViews(service.getId(), 
                    service.getClassName(), 10, -1, session.getIpAddress(), session.getSessionId());
            RemoteViewObject theSavedView = null;
            
            //in order to create the end to end view we need the service resoruces
            List<String> linkClasses = new ArrayList<>();
            List<String> linkIds = new ArrayList<>();
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), session.getIpAddress(), session.getSessionId());
            for (RemoteObjectLight resource : serviceResources) {
                linkClasses.add(resource.getClassName());
                linkIds.add(resource.getId());
            }
            //first we must check if the service already has a saved view
            for (RemoteViewObjectLight serviceView : objectViews) {
                if (EndToEndViewScene.VIEW_CLASS.equals(serviceView.getViewClassName())) {
                    theSavedView = wsBean.getObjectRelatedView(service.getId(), 
                            service.getClassName(), serviceView.getId(), session.getIpAddress(), session.getSessionId()); 
                   
                    if(theSavedView != null){
                        RemoteViewObject savedE2EView = wsBean.validateSavedE2EView(linkClasses, linkIds, theSavedView, session.getIpAddress(), session.getSessionId());
                        if (savedE2EView != null)
                            scene.render(savedE2EView.getStructure());
                        break;
                    }
                }
            }
            
            if(theSavedView == null){//if the service has no view we must create one from scracth
                RemoteViewObject unsavedE2EView = wsBean.getE2EMap(linkClasses, linkIds, true, true, true, true, true, session.getIpAddress(), session.getSessionId());
                if (unsavedE2EView != null) //if there's a saved view already, change the location of the nodes and connections created using the default render method
                    scene.render(unsavedE2EView.getStructure());
            }
        } catch (ServerSideException ex) {
//            Notifications.showError(ex.getMessage());
        }
        setSizeFull();
        
        this.add(scene);
    }
}
