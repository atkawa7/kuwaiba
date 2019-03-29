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
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.web.procmanager.rackview.ComponentDeviceList;
import org.kuwaiba.web.procmanager.rackview.ComponentRackSelector;

/**
 * Mini Application used to show the Select a Rack to set a material location
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MiniAppRackSelector extends AbstractMiniApplication<Component, Component> {

    public MiniAppRackSelector(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "Mini Application used to show the select a rack to ve used in the \"Material Location\" step";
    }

    @Override
    public Component launchDetached() {
        return launchEmbedded();
    }

    @Override
    public Component launchEmbedded() {
        try {
            if (getInputParameters() != null) {
                List<RemoteObject> selectedDevices = new ArrayList();
                                
                for (Object id : getInputParameters().keySet()) {

                    RemoteObject child = wsBean.getObject(
                        getInputParameters().getProperty(String.valueOf(id)), 
                        String.valueOf(id), 
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    selectedDevices.add(child);                
                }
                if (!selectedDevices.isEmpty()) {
                    RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
                    
                    ComponentDeviceList componentDeviceList = new ComponentDeviceList(selectedDevices, wsBean, remoteSession);
                    ComponentRackSelector componentRackSelector = new ComponentRackSelector(componentDeviceList, wsBean);

                    return componentRackSelector;
                }
                else {
                    return new Label("<h3 style=\"color:#f9a825;\">The input parameters can not be empty</h3>", ContentMode.HTML);
                }
            }
        } catch (ServerSideException ex) {
            Notifications.showError("Unexpected input parameter was received in the MiniAppRackSelector");
        }
        return new Label("<h3 style=\"color:#e57373;\">The input parameters can not be null</h3>", ContentMode.HTML);
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
