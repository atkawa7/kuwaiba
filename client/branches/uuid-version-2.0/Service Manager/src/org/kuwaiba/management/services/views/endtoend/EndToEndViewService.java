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
package org.kuwaiba.management.services.views.endtoend;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.configuration.ObjectViewConfigurationObject;
import org.inventory.core.visual.scene.AbstractScene;

/**
 * Implements the logic necessary to control what's shown in the associated TC
 * @author Adrian Martinez <adrian.martinez@neotropic.co>
 */
public class EndToEndViewService {
    private AbstractScene scene;
    private LocalObjectView currentView;

    public EndToEndViewService(AbstractScene scene) {
        this.scene = scene;
    }
    
    public void renderView() {
        ObjectViewConfigurationObject configObject = scene.getConfigObject();
        LocalObjectLight object = (LocalObjectLight) configObject.getProperty("currentObject");

        List<LocalObjectViewLight> views = CommunicationsStub.getInstance().getObjectRelatedViews(object.getId(), object.getClassName());

        if (views != null) {
            if (views.isEmpty()) {
                currentView = null;
                configObject.setProperty("currentView", currentView);
                scene.render((byte[]) null);
            } else {
                currentView = CommunicationsStub.getInstance().getObjectRelatedView(object.getId(), object.getClassName(), views.get(0).getId());
                configObject.setProperty("currentView", currentView);
                scene.render(currentView.getStructure());
            }
        } else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }

    public void saveView() {
        ObjectViewConfigurationObject configObject = scene.getConfigObject();
        LocalObjectLight currentService = (LocalObjectLight) configObject.getProperty("currentObject");

        if (currentService != null) {
            byte[] viewStructure = scene.getAsXML();
            if (currentView == null) {
                long viewId = CommunicationsStub.getInstance().createObjectRelatedView(currentService.getId(), currentService.getClassName(), null, null, "EndToEndView", viewStructure, scene.getBackgroundImage()); //NOI18N

                if (viewId != -1) { //Success
                    currentView = new LocalObjectView(viewId, "EndToEndView", null, null, viewStructure, scene.getBackgroundImage());
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
                    configObject.setProperty("saved", true);
                } else {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            } else {
                if (!CommunicationsStub.getInstance().updateObjectRelatedView(currentService.getId(),
                         currentService.getClassName(), currentView.getId(),
                        null, null,viewStructure, scene.getBackgroundImage()))

                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                else {
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
                    configObject.setProperty("saved", true);
                }
            }
        }
    }
}
