/*
 * Copyright (c) 2019 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.mpls.views;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.configuration.ObjectViewConfigurationObject;
import org.inventory.core.visual.scene.AbstractScene;

/**
 * Implements the logic necessary to control what's shown in the associated
 * TopComponentMlsplinks endpoints view
 * @author Adrian Martinez <adrian.martinez@neotropic.co>
 */
public class MplsLinkEndpointsViewService {
    private AbstractScene scene;
    private LocalObjectView currentView;

    public MplsLinkEndpointsViewService(AbstractScene scene) {
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
        LocalObjectLight currentMplsLinkView = (LocalObjectLight) configObject.getProperty("currentObject");

        if (currentMplsLinkView != null) {
            byte[] viewStructure = scene.getAsXML();
            if (currentView == null) {
                long viewId = CommunicationsStub.getInstance().createObjectRelatedView(currentMplsLinkView.getId(), currentMplsLinkView.getClassName(), null, null, "PlainChildrenView", viewStructure, scene.getBackgroundImage()); //NOI18N

                if (viewId != -1) { //Success
                    currentView = new LocalObjectView(viewId, "ServiceSimpleView", null, null, viewStructure, scene.getBackgroundImage());
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
                    configObject.setProperty("saved", true);
                } else {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            } else {
                if (!CommunicationsStub.getInstance().updateObjectRelatedView(currentMplsLinkView.getId(),
                         currentMplsLinkView.getClassName(), currentView.getId(),
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
