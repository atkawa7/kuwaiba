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

package org.kuwaiba.apis.web.gui.navigation;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.IconGenerator;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * A simple IconGenerator implementation to be used in trees displaying inventory object nodes
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class SimpleIconGenerator implements IconGenerator<AbstractNode>{
    /**
     * Backend bean reference
     */
    private WebserviceBean wsBean;
    /**
     * Reference to the current session
     */
    private RemoteSession session;

    public SimpleIconGenerator(WebserviceBean wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
    }
    
    @Override
    public Resource apply(AbstractNode item) {

        if (item instanceof InventoryObjectNode) { //It's not the root node
            RemoteObjectLight businessObject = (RemoteObjectLight)item.getObject();
            if (ResourceFactory.getInstance().isSmallIconCached(businessObject.getClassName()))
                return ResourceFactory.getInstance().getSmallIcon(businessObject.getClassName());
            else {
                try {
                    RemoteClassMetadata classMetadata = wsBean.getClass(businessObject.getClassName(), Page.getCurrent().getWebBrowser().getAddress(),
                            session.getSessionId());
                    return ResourceFactory.getInstance().getSmallIcon(classMetadata);
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                    return ResourceFactory.DEFAULT_SMALL_ICON;
                }
            }
        } else
            return VaadinIcons.STAR;


    }
}
