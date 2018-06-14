/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google.actions;

import com.vaadin.ui.Notification;
import org.kuwaiba.apis.web.gui.actions.CreateInventoryObjectChildAction;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerNode;

/**
 * Create a inventory object child for marker node
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NewObjectAction extends CreateInventoryObjectChildAction {

    public NewObjectAction() {
        super("New Object");
    }
    
    @Override
    public void finalActionPerformed(Object sourceComponent, Object targetObject, Object selectedOption) {
        super.finalActionPerformed(sourceComponent, targetObject, selectedOption);
        Notification.show("Object created successfully",  Notification.Type.TRAY_NOTIFICATION);
    }
    
    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        if (!(sourceComponent instanceof EmbeddableComponent))
            return;
        
        if (!(targetObject instanceof MarkerNode))
            return;
        
        RemoteObjectLight object = ((MarkerNode)targetObject).getRemoteObjectLight();
        
        super.actionPerformed(sourceComponent, object);
    }
}
