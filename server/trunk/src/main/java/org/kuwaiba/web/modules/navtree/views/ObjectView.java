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

package org.kuwaiba.web.modules.navtree.views;

import com.vaadin.ui.Panel;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.servmanager.views.EndToEndViewScene;

/**
 * TThe embeddable component that displays an object view.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectView extends Panel {
    public ObjectView(RemoteObjectLight businessObject, WebserviceBean wsBean) {
//        ObjectViewScene scene = new ObjectViewScene(businessObject, wsBean);
//        
//        try {
//            List<RemoteViewObjectLight> objectViews = wsBean.getObjectRelatedViews(service.getId(), service.getClassName(), 10, -1, ipAddress, sessionId);
//            RemoteViewObject theSavedView = null;
//            for (RemoteViewObjectLight serviceView : objectViews) {
//                if (EndToEndViewScene.VIEW_CLASS.equals(serviceView.getViewClassName())) {
//                    theSavedView = wsBean.getObjectRelatedView(service.getId(), service.getClassName(), serviceView.getId(), ipAddress, sessionId); 
//                    break;
//                }
//            }
//            scene.render(service); //First we render the default view with all the resources associated to the service
//            if (theSavedView != null) //if there's a saved view already, change the location of the nodes and connections created using the default render method
//                scene.render(theSavedView.getStructure());
//            
//        } catch (ServerSideException ex) {
//            Notifications.showError(ex.getMessage());
//        }
//        
//        this.setContent(scene);
    }
}
