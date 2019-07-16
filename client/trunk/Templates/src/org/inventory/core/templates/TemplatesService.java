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
 *  under the License.
 */
package org.inventory.core.templates;

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.nodes.TemplatesModuleClassNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Service class for this module. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TemplatesService  {
    private final TemplatesTopComponent topComponent;
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    public TemplatesService(TemplatesTopComponent topComponent) {
        this.topComponent = topComponent;
    }
    
    public void setRoot() {
        final List<LocalClassMetadataLight> allClasses = com.getLightSubclasses(Constants.CLASS_INVENTORYOBJECT, false, false);
        
        if (allClasses == null) 
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        else 
            topComponent.getExplorerManager().setRootContext(new AbstractNode(new Children.Keys<LocalClassMetadataLight>() {

                {
                    setKeys(allClasses);
                }

                @Override
                protected Node[] createNodes(LocalClassMetadataLight t) {
                    return new Node[] {new TemplatesModuleClassNode(t)};
                }
            }));
    }
}
