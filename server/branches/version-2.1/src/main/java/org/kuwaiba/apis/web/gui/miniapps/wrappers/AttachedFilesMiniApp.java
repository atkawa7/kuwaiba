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
package org.kuwaiba.apis.web.gui.miniapps.wrappers;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.dashboards.widgets.AttachedFilesDashboardWidget;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A mini application that wraps the AttachedFilesDashboardWidget
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AttachedFilesMiniApp extends AbstractMiniApplication<Component, Component> {

    public AttachedFilesMiniApp(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "A mini application to wrapper the AttachedFilesDashboardWidget";
    }

    @Override
    public Component launchDetached() {
        if (getInputParameters() != null && 
            getInputParameters().containsKey("id") && 
            getInputParameters().containsKey("className")) {
            try {
                String objectId = getInputParameters().getProperty("id");
                String objectClassName = getInputParameters().getProperty("className");

                RemoteObjectLight rol = wsBean.getObjectLight(
                    objectClassName, 
                    objectId, 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getIpAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                AttachedFilesDashboardWidget attachedFilesDashboardWidget = new AttachedFilesDashboardWidget(rol, wsBean);
                attachedFilesDashboardWidget.createContent();

                attachedFilesDashboardWidget.launch();
                return null;            
            } catch(NumberFormatException | ServerSideException ex) {
            }
        }
        Notifications.showError("Unexpected input parameter was received in the AttachedFilesMiniApp");
        return null;
    }

    @Override
    public Component launchEmbedded() {
        return null;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
