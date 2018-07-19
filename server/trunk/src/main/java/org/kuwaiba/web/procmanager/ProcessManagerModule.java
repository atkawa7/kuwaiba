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

import com.google.common.eventbus.EventBus;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import java.util.List;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.util.i18n.I18N;

/**
 * This module allows to manage the process instances for the available process definition previously created
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessManagerModule extends AbstractModule {
    /**
     * The actual component
     */
    private ProcessManagerComponent processManagerComponent;
    
    public ProcessManagerModule(EventBus eventBus, WebserviceBean wsBean, RemoteSession session) {
        super(eventBus, wsBean, session);
    }

    @Override
    public String getName() {
        return "Process Manager";
    }

    @Override
    public String getDescription() {
        return "This module allows to manage the process instances for the available process definition previously created";
    }

    @Override
    public String getVersion() {
        return "0.5";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_COMMERCIAL;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        MenuItem menuProcessManager = menuBar.addItem(I18N.gm("procmanager"), null);
        MenuItem menuItem = menuProcessManager.addItem(I18N.gm("procmanager.processes"), null);

        try {
            List<RemoteProcessDefinition> processDefinitions = wsBean.getProcessDefinitions(
                Page.getCurrent().getWebBrowser().getAddress(), 
                session.getSessionId());

            for (RemoteProcessDefinition processDefinition : processDefinitions) {

                MenuItem processDefItem = menuItem.addItem(processDefinition.getName(), null, new MenuBar.Command() {
                    
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        UI.getCurrent().getSession().setAttribute("selectedProcessDefinition", processDefinition); //NOI18N
                        UI.getCurrent().getNavigator().addView(ProcessManagerComponent.VIEW_NAME, open());
                        UI.getCurrent().getNavigator().navigateTo(ProcessManagerComponent.VIEW_NAME);
                    }
                });
                
                processDefItem.addItem(I18N.gm("new"), null, new MenuBar.Command() {
                    
                    @Override
                    public void menuSelected(MenuItem selectedItem) {
                        UI.getCurrent().getSession().setAttribute("selectedProcessDefinition", processDefinition); //NOI18N
                        UI.getCurrent().getNavigator().addView(ProcessManagerComponent.VIEW_NAME, open());
                        UI.getCurrent().getNavigator().navigateTo(ProcessManagerComponent.VIEW_NAME);
                        
                        ProcessInstancesView.createProcessInstance(processDefinition, wsBean, session);
                    }
                });
            }

        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        MenuItem menuItemOptions = menuProcessManager.addItem(I18N.gm("options"), null);
        menuItemOptions.addItem(I18N.gm("reload"), null, new MenuBar.Command() {
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
        });
    }

    @Override
    public View open() {
        processManagerComponent = new ProcessManagerComponent();
        //Register components in the event bus
        processManagerComponent.registerComponents();
        return processManagerComponent;
    }

    @Override
    public void close() {
        //Unregister components from the event bus
        processManagerComponent.unregisterComponents();
    }
    
}
