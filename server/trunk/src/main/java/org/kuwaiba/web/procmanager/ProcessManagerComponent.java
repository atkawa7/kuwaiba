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

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.IndexUI;
/**
 * The main component of the Process Manager module.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("procmanager")
public class ProcessManagerComponent extends AbstractTopComponent {
    public static String VIEW_NAME = "procmanager";
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("processmanager");
        addStyleName("misc");
        addStyleName("darklayout");
        
        setSizeFull();
        
        MenuBar mainMenu = ((IndexUI)getUI()).getMainMenu();
        
        addComponent(mainMenu);
        setExpandRatio(mainMenu, 0.5f);
                
        RemoteProcessDefinition processDefinition = (RemoteProcessDefinition) getSession().getAttribute("selectedProcessDefinition");
        
        try {
            List<RemoteProcessInstance> processInstances = wsBean.getProcessInstances(
                processDefinition.getId(), 
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) getSession().getAttribute("session")).getSessionId());

            ProcessInstancesView processInstancesView = new ProcessInstancesView(processDefinition, processInstances, wsBean, ((RemoteSession) getSession().getAttribute("session")));
            addComponent(processInstancesView);
            setExpandRatio(processInstancesView, 9.5f);

        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }

    @Override
    public void registerComponents() {
    }

    @Override
    public void unregisterComponents() {
    }
    
}
