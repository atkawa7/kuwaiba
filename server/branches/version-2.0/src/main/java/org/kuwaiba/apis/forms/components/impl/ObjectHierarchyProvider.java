/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Provides a set of methods to access to a hierarchy of inventory objects
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ObjectHierarchyProvider implements HierarchyProvider<RemoteObjectLight> {
    private static ObjectHierarchyProvider instance;
    private WebserviceBean webserviceBeanLocal;
    private RemoteSession remoteSession;
    private RemoteObjectLight root;
            
    private ObjectHierarchyProvider() {
    }
    
    public static ObjectHierarchyProvider getInstance() {
        return instance == null ? instance = new ObjectHierarchyProvider() : instance;
    }
    
    public WebserviceBean getWebserviceBeanLocal() {
        return webserviceBeanLocal;
    }
    
    public void setWebserviceBeanLocal(WebserviceBean webserviceBeanLocal) {
        this.webserviceBeanLocal = webserviceBeanLocal;
    }
    
    public RemoteSession getRemoteSession() {
        return remoteSession;
    }
    
    public void setRemoteSession(RemoteSession remoteSession) {
        this.remoteSession = remoteSession;
    }

    @Override
    public RemoteObjectLight getRoot() {
        return root;
    }
    
    @Override
    public void setRoot(RemoteObjectLight root) {
        this.root = root;
    }
    
    @Override
    public List<RemoteObjectLight> getChildren(RemoteObjectLight parent) {
        try {
            return webserviceBeanLocal.getObjectChildren(
                parent.getClassName(), 
                parent.getId(), 
                -1, 
                remoteSession.getIpAddress(), 
                remoteSession.getSessionId());
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
            return null;
        }
    }


    
    
}
