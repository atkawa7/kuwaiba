/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.objectview;

import org.inventory.core.visual.configuration.ObjectViewConfigurationObject;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.views.objectview.scene.ChildrenViewScene;

/**
 * Implements the logic necessary to control what's shown in the associated TC
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectViewService {
    private final ChildrenViewScene scene;
    private LocalObjectView currentView;

    public ObjectViewService(ChildrenViewScene scene) {
        this.scene = scene;
    }
        
    public void renderView() {
        ObjectViewConfigurationObject configObject = scene.getConfigObject();
        LocalObjectLight object = (LocalObjectLight) configObject.getProperty("currentObject"); //NOI18N
        
        List<LocalObjectViewLight> views = CommunicationsStub.getInstance().getObjectRelatedViews(object.getId(), object.getClassName());
        
        if (views != null) {
            if (views.isEmpty()) {
                currentView = null;
                configObject.setProperty("currentView", currentView); //NOI18N
                scene.render((byte[]) null);
            } else {
                currentView = CommunicationsStub.getInstance().getObjectRelatedView(object.getId(), object.getClassName(), views.get(0).getId());
                configObject.setProperty("currentView", currentView); //NOI18N
                scene.render(currentView.getStructure());
            }
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
    
    public void saveView() {
        ObjectViewConfigurationObject configObject = scene.getConfigObject();
        LocalObjectLight currentObject = (LocalObjectLight) configObject.getProperty("currentObject"); //NOI18N
        
        if (currentObject != null) {
            
            byte[] viewStructure = scene.getAsXML();
            if (currentView == null) {
                long viewId = CommunicationsStub.getInstance().createObjectRelatedView(currentObject.getId(), currentObject.getClassName(), null, null, "ObjectView", viewStructure, scene.getBackgroundImage()); //NOI18N
                
                if (viewId != -1) { //Success
                    currentView = new LocalObjectView(viewId, "ObjectViewModule", null, null, viewStructure, scene.getBackgroundImage()); //NOI18N
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
                    configObject.setProperty("saved", true);
                } else {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            } else {
                if (!CommunicationsStub.getInstance().updateObjectRelatedView(currentObject.getId(),
                         currentObject.getClassName(), currentView.getId(),
                        null, null,viewStructure, scene.getBackgroundImage()))
                    
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                else {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
                    configObject.setProperty("saved", true);
                }
            }
        }
    }
}