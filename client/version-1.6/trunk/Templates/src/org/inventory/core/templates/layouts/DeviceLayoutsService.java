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

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;

/**
 * Service of existing device layouts
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceLayoutsService {
    
    public DeviceLayoutsService() {
    }
    
    public List<LocalObjectLight> getDevices() {
        List<LocalClassMetadataLight> classes = CommunicationsStub.getInstance().getLightSubclasses(Constants.CLASS_INVENTORYOBJECT, false, false);
        
        if (classes == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;            
        }
        
        List<LocalObjectLight> result = new ArrayList();
                
        for (LocalClassMetadataLight classMetadata : classes) {
            boolean classMayHaveDeviceLayout = false;
            
            try {
                classMayHaveDeviceLayout = Utils.classMayHaveDeviceLayout(classMetadata.getClassName());
            } catch (Exception ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return null;
            }
            if (classMayHaveDeviceLayout) {
                List<LocalObjectLight> templates = CommunicationsStub.getInstance().getTemplatesForClass(classMetadata.getClassName(), true);

                if (templates == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return null;
                }
                for (LocalObjectLight template : templates) {
                    LocalObject templateElement = CommunicationsStub.getInstance().getTemplateElement(template.getClassName(), template.getOid());
                    
                    if (templateElement == null) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        return null;
                    }
                    LocalObjectListItem model = (LocalObjectListItem) templateElement.getAttribute(Constants.ATTRIBUTE_MODEL);
                    
                    if (model != null) {
                        List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance()
                            .getListTypeItemRelatedViews(model.getId(), model.getClassName());
                        
                        if (relatedViews != null) {
                            
                            if (!relatedViews.isEmpty()) {
                                
                                LocalObjectView relatedView = CommunicationsStub.getInstance().getListTypeItemRelatedView(
                                    model.getId(), model.getClassName(), relatedViews.get(0).getId());

                                if (relatedView != null)
                                    result.add(template);
                                else {
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                                    return null;
                                }
                            }
                        } else {
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                            return null;
                        }
                    } 
                }
            }
        }
        return result;
    }
}
