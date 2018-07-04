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

import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.apis.web.gui.notifications.MessageBoxShowObjectId;
import org.kuwaiba.web.modules.osp.google.OSPTopComponent;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerNode;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShowObjectIdAction extends AbstractAction {
    
    public ShowObjectIdAction() {
        super("Show Object Id");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
//        TopComponent parentComponent = ((OSPTopComponent) sourceComponent).getTopComponent();
//        RemoteObjectLight object = null;
//        
//        if (targetObject instanceof ConnectionPolyline)
//            object = ((ConnectionPolyline) targetObject).getConnectionInfo();
//            
//        if (targetObject instanceof MarkerNode)
//            object = ((MarkerNode) targetObject).getRemoteObjectLight();
//        
//        MessageBoxShowObjectId messageBox = new MessageBoxShowObjectId(parentComponent, object);
//        messageBox.open();
    }
}
