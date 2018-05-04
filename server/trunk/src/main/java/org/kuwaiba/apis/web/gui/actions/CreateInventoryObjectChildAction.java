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
package org.kuwaiba.apis.web.gui.actions;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.util.List;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;

/**
 * Action that requests an Inventory Object creation
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateInventoryObjectChildAction extends AbstractComposedAction {
    private RemoteObjectLight newObject;
    
    public CreateInventoryObjectChildAction(String caption) {
        super(caption);
    }
    
    public RemoteObjectLight getNewObject() {
        return newObject;
    }

    @Override
    public void finalActionPerformed(Object sourceComponent, Object targetObject, Object selectedOption) {
        try {
            if (!(sourceComponent instanceof EmbeddableComponent))
                return;
            
            if (!(targetObject instanceof RemoteObjectLight))
                return;
            
            TopComponent parentComponent = ((EmbeddableComponent) sourceComponent).getTopComponent();
            RemoteObjectLight object = (RemoteObjectLight) targetObject;
            
            long oid = parentComponent.getWsBean().createObject(
                    ((ClassInfoLight) selectedOption).getClassName(), 
                    "DummyRoot".equals(((ClassInfoLight) selectedOption).getClassName()) ? null : object.getClassName(), //NOI18N
                    object.getOid(),
                    new String[0],
                    new String[0],
                    -1,
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    parentComponent.getApplicationSession().getSessionId());
            
            newObject = parentComponent.getWsBean().getObjectLight(
                    ((ClassInfoLight) selectedOption).getClassName(), 
                    oid, 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    parentComponent.getApplicationSession().getSessionId());
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        try {
            if (!(sourceComponent instanceof EmbeddableComponent))
                return;
            if (!(targetObject instanceof RemoteObjectLight))
                return;
            
            TopComponent parentComponent = ((EmbeddableComponent) sourceComponent).getTopComponent();
            RemoteObjectLight object = (RemoteObjectLight) targetObject;
            
            List<ClassInfoLight> possiblyChildren = parentComponent.getWsBean().getPossibleChildren(
                    object.getClassName(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    parentComponent.getApplicationSession().getSessionId());
            
            if (!possiblyChildren.isEmpty())
                showSubMenu(sourceComponent, targetObject, possiblyChildren);
            
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
}
