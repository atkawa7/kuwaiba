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
package org.kuwaiba.apis.web.gui.miniapps.sync;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationProvider;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MiniAppSyncRunner extends AbstractMiniApplication<Component, Component> {
    public MiniAppSyncRunner(Properties inputParameters) {
        super(inputParameters);        
    }    

    @Override
    public String getDescription() {
        return "Launch AdHoc Automated Synchronization Task";
    }

    @Override
    public Component launchDetached() {
        final RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        
        if (!inputParameters.containsKey("deviceId")) { //NOI18N
            Notifications.showError("Missing input parameter deviceId");
            return null;
        }
        if (!inputParameters.containsKey("deviceClass")) { //NOI18N
            Notifications.showError("Missing input parameter deviceClass");
            return null;
        }
        try {
            String deviceId = inputParameters.getProperty("deviceId"); //NOI18N
            String deviceClass = inputParameters.getProperty("deviceClass"); //NOI18N

            RemoteObjectLight remoteObject = wsBean.getObjectLight(
                deviceClass, 
                deviceId, 
                remoteSession.getIpAddress(), 
                remoteSession.getSessionId());

            RemoteSynchronizationConfiguration syncConfig = wsBean.getSyncDataSourceConfiguration(
                remoteObject.getId(), 
                remoteSession.getIpAddress(), 
                remoteSession.getSessionId());

            Window window = new Window();
            window.setModal(true);
            window.setWidth(80, Unit.PERCENTAGE);
            window.setHeight(80, Unit.PERCENTAGE);

            VerticalLayout vly = new VerticalLayout();
            vly.setSizeFull();

            GridLayout gly = new GridLayout();
            gly.setSpacing(true);
            gly.setColumns(2);
            gly.setRows(2);
            
            List<RemoteSynchronizationProvider> syncProviders = wsBean.getSynchronizationProviders(remoteSession.getIpAddress(), remoteSession.getSessionId());
            
            List<RemoteSynchronizationProvider> automated = new ArrayList();
            for (RemoteSynchronizationProvider syncProvider : syncProviders) {
                if (syncProvider.isAutomated())
                    automated.add(syncProvider);
            }
            Grid<RemoteSynchronizationProvider> grdProviders = new Grid();                
            grdProviders.setItems(automated);
            grdProviders.setSelectionMode(Grid.SelectionMode.MULTI);
            
            grdProviders.addColumn(RemoteSynchronizationProvider::getDisplayName).setCaption("Providers");

            Button btnRun = new Button("Run");
            btnRun.setWidth(70, Unit.PIXELS);
            
            Button btnCancel = new Button("Cancel");
            btnCancel.setWidth(70, Unit.PIXELS);

            btnRun.addClickListener(new ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<RemoteSynchronizationProvider> selectedsyncProviders = new ArrayList();
                    for (RemoteSynchronizationProvider syncProvider : grdProviders.getSelectedItems())
                        selectedsyncProviders.add(syncProvider);
                                        
                    if (!selectedsyncProviders.isEmpty())
                        new SyncRunnerService().launchAdHocAutomatedSynchronizationTask(window, wsBean, remoteSession, selectedsyncProviders, syncConfig);
                }
            });

            btnCancel.addClickListener(new ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    window.close();                        
                }
            });

            gly.addComponent(grdProviders, 0, 0, 1, 0);
            gly.addComponent(btnRun);
            gly.addComponent(btnCancel);

            gly.setComponentAlignment(grdProviders, Alignment.MIDDLE_CENTER);
            gly.setComponentAlignment(btnRun, Alignment.MIDDLE_RIGHT);
            gly.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);

            vly.addComponent(gly);
            vly.setComponentAlignment(gly, Alignment.MIDDLE_CENTER);

            window.setContent(vly);
            return window;

        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
            return null;
        }
    }
    
    @Override
    public Component launchEmbedded() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
}
