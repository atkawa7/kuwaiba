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
package org.kuwaiba.web.menucommand;

import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Reloads the process engine folder and update the process/artifacts definitions
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessDefinitionReloader implements MenuBar.Command {
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    
    public ProcessDefinitionReloader(WebserviceBean wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        try {
            wsBean.reloadProcessDefinitions(
                    Page.getCurrent().getWebBrowser().getAddress(),
                    session.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }
    
}
