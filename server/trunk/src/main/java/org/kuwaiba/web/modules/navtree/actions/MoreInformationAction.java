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
package org.kuwaiba.web.modules.navtree.actions;

import org.kuwaiba.apis.web.gui.notifications.MessageBoxShowObjectId;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Shows the object id, location and class.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MoreInformationAction extends AbstractAction {
    
    public MoreInformationAction() {
        super("More Information");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
//        InventoryObjectNode node = (InventoryObjectNode) targetObject;
//        
//        MessageBoxShowObjectId messageBox = new MessageBoxShowObjectId(
//            node.getTree().getTopComponent(), 
//            (RemoteObjectLight) node.getObject());
//        messageBox.open();
    }
}
