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

package org.kuwaiba.apis.web.gui.navigation.events;

import com.vaadin.event.CollapseEvent;
import com.vaadin.event.ExpandEvent;
import com.vaadin.server.Page;
import java.util.List;
import org.kuwaiba.apis.web.gui.navigation.DynamicTree;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A standard tree expand listener that retrieves the immediate children and attach them to the node 
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class RemoteObjectStandardCollapseListener implements CollapseEvent.CollapseListener<RemoteObjectLight>{
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    /**
     * Current user session
     */
    private RemoteSession session;
    public RemoteObjectStandardCollapseListener(WebserviceBean wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
    }

    @Override
    public void itemCollapse(CollapseEvent<RemoteObjectLight> event) {
        //((DynamicTree)event.getComponent()).getTreeData().
    }
    
    
}
