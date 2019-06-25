/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts;

import java.awt.Color;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;

/**
 * The service class associated to the main device layout edition component
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutEditorService {
    private final LocalObjectListItem model;
    private final DeviceLayoutScene scene;
    private LocalObjectView deviceLayoutView;
        
    public DeviceLayoutEditorService(LocalObjectListItem model) {
        this.model = model;
        this.scene = new DeviceLayoutScene(model);
    }
    
    public LocalObjectListItem getModel() {
        return model;
    }
    
    public DeviceLayoutScene getScene() {
        return scene;
    }
    
    public LocalObjectView getDeviceLayoutView() {
        return deviceLayoutView;
    }
    
    public void renderLayout() {
        List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(model.getId(), model.getClassName());
        if (relatedViews != null) {
            if (relatedViews.isEmpty()) {
                deviceLayoutView = null;
                scene.render((byte[]) null);
            } else {
                deviceLayoutView = CommunicationsStub.getInstance().getListTypeItemRelatedView(model.getId(), model.getClassName(), relatedViews.get(0).getId());
                scene.render(deviceLayoutView.getStructure());
            }            
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }    
    
    public boolean saveLayout() {
        byte[] structure = scene.getAsXML();
        if (deviceLayoutView == null) {
            long viewId = CommunicationsStub.getInstance().createListTypeItemRelatedView(
                model.getId(), model.getClassName(), "DeviceLayoutView", " ", null, structure, scene.getBackgroundImage()); //NOI18N
            
            if (viewId != -1) { //Success
                deviceLayoutView = new LocalObjectView(viewId, "DeviceLayoutView", null, null, structure, scene.getBackgroundImage()); //NOI18N
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("device_layout_saved_successfully"));
                
                LayoutOutputManager.getInstance().getLayoutOutput(getModel())
                    .printLine(I18N.gm("device_layout_saved_successfully"), Color.GREEN);
                return true;
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return false;
            }
        } else {
            if (CommunicationsStub.getInstance().updateListTypeItemRelatedView(model.getId(), model.getClassName(), 
                deviceLayoutView.getId(), null, null, structure, scene.getBackgroundImage())) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, I18N.gm("device_layout_saved_successfully"));
                
                LayoutOutputManager.getInstance().getLayoutOutput(getModel())
                    .printLine(I18N.gm("device_layout_saved_successfully"), Color.GREEN);
                return true;
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return false;
            }
        }
    }
    
    public boolean deleteLayout() {
        if (deviceLayoutView == null)
            return false;
        if (model == null)
            return false;
        boolean deleted = CommunicationsStub.getInstance().deleteListTypeItemRelatedView(model.getId(), model.getClassName(), deviceLayoutView.getId());
        
        if (deleted)
            deviceLayoutView = null;
        return deleted;
    }
}
