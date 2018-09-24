/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.procmanager;

import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.web.procmanager.rackview.ComponentDeviceList;
import org.kuwaiba.web.procmanager.rackview.ComponentRackSelector;

/**
 * Mini Application used to show the Select a Rack to set a material location
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MiniAppRackSelector extends AbstractMiniApplication<Component, Component> {

    public MiniAppRackSelector(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "Mini Application used to show the Select a Rack to set a material location";
    }

    @Override
    public Component launchDetached() {
        return launchEmbedded();
    }

    @Override
    public Component launchEmbedded() {
        List<RemoteObject> selectedDevices = new ArrayList();
        
        try {
            if (getInputParameters() != null) {
                
                for (Object id : getInputParameters().keySet()) {

                    RemoteObject child = getWebserviceBean().getObject(
                        getInputParameters().getProperty(String.valueOf(id)), 
                        Long.valueOf(String.valueOf(id)), 
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    selectedDevices.add(child);                
                }
            }
                        
        } catch (ServerSideException ex) {
            Notification.show("Unexpected Input Parameter was received in the MiniAppRackView", Notification.Type.ERROR_MESSAGE);
        }
        ComponentDeviceList componentDeviceList = new ComponentDeviceList(selectedDevices, getWebserviceBean());
        ComponentRackSelector componentRackSelector = new ComponentRackSelector(componentDeviceList, getWebserviceBean());
                
        return componentRackSelector;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
