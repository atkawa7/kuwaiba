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

import com.neotropic.kuwaiba.scheduling.BackgroundJob;
import com.neotropic.kuwaiba.scheduling.JobManager;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.LinkedHashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;

/**
 * Runner to launch AdHoc Automated Synchronization Task. Given a set of sync provider
 * related to a data source configuration
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SyncRunner {
    private final UI ui;
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    
    private final RemoteSynchronizationConfiguration syncConfig;
    
    private final Window window = new Window();
    private final TabSheet tabSheet = new TabSheet();
    
    private final LinkedHashMap<SyncProvider, Thread> syncProviderThreads;
    
        
    public SyncRunner(UI ui, WebserviceBean webserviceBean, RemoteSession remoteSession, 
        List<SyncProvider> syncProviders, RemoteSynchronizationConfiguration syncConfig) {
        this.ui = ui;
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
                
        this.syncProviderThreads = new LinkedHashMap<>();
        for (SyncProvider syncProvider : syncProviders) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    ui.access(new Runnable() {
                        @Override
                        public void run() {
                            launchNextAdHocAutomatedSynchronizationTask(syncProvider);                        
                        }
                    });
                }
            };
            this.syncProviderThreads.put(syncProvider, thread);
        }
        this.syncConfig = syncConfig;
        
        tabSheet.setSizeFull();
        window.setModal(true);
        window.setWidth(80, Sizeable.Unit.PERCENTAGE);
        window.setHeight(80, Sizeable.Unit.PERCENTAGE);
        window.setContent(tabSheet);
    }
        
    public void launchAdHocAutomatedSynchronizationTask(SyncProvider syncProvider) {
        if (syncProvider != null && syncProviderThreads.containsKey(syncProvider)) {
            syncProviderThreads.get(syncProvider).start();
        }
    }
        
    public void launchNextAdHocAutomatedSynchronizationTask(SyncProvider syncProvider) {
        if (syncProvider != null) {
            try {
                BackgroundJob managedJob = webserviceBean.launchAdHocAutomatedSynchronizationTask(
                    new long[] {syncConfig.getId()}, 
                    syncProvider.getName(), 
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());                      
                
                int retries = 0;
                while (!managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.FINISHED) && retries < 30) {
                    try {                
                        //For some reason (probably thread-concurrency related), the initial "managedJob" instance is different from the one
                        //updated in the SyncProcessor/Writer, so we have to constantly fetch it again.
                        managedJob = JobManager.getInstance().getJob(managedJob.getId());

                        if (managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.ABORTED)) {
                            Exception exceptionThrownByTheJob = managedJob.getExceptionThrownByTheJob();

                            if (exceptionThrownByTheJob != null) {
                                if (exceptionThrownByTheJob instanceof InventoryException)
                                    throw new ServerSideException(managedJob.getExceptionThrownByTheJob().getMessage());
                                else {
                                    System.out.println("[KUWAIBA] An unexpected error occurred in launchAdHocAutomatedSynchronizationTask: " + exceptionThrownByTheJob.getMessage());
                                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                                }
                            }
                        }
                        Thread.sleep(3000);
                    }catch (Exception ex) {
                        throw new RuntimeException(ex.getMessage());
                    }
                    retries ++;
                }
                if (retries == 30)
                    throw new ServerSideException("The automated synchronization task can no be executed");
                
                VerticalLayout vly = new VerticalLayout();
                vly.setSizeFull();
                
                Grid<SyncResult> grdSyncResult = new Grid<>();                
                grdSyncResult.setItems((List<SyncResult>)managedJob.getJobResult());
                
                grdSyncResult.addColumn(SyncResult::getType).setCaption("Type");
                grdSyncResult.addColumn(SyncResult::getActionDescription).setCaption("Action Description");
                grdSyncResult.addColumn(SyncResult::getResult).setCaption("Result");
                                
                vly.addComponent(grdSyncResult);
                vly.setComponentAlignment(grdSyncResult, Alignment.MIDDLE_CENTER);
                                
                tabSheet.addTab(vly, syncProvider.getValue());
                if (!window.isAttached())
                    ui.addWindow(window);
                
                boolean isNext = false;
                
                for (SyncProvider key : syncProviderThreads.keySet()) {
                    if (isNext) {
                        launchAdHocAutomatedSynchronizationTask(key);
                        break;
                    }
                    if (syncProvider.equals(key))
                        isNext = true;                        
                }
                //Following with the next provider
            } catch(ServerSideException | RuntimeException ex) {
                Notifications.showError(ex.getMessage());
            }            
        }
    }
}
