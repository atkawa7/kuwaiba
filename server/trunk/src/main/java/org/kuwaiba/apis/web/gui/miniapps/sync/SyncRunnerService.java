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
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationProvider;

/**
 * Run the device sync in the web client
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SyncRunnerService {
    
    private Window window;
    private final TabSheet tabSheet = new TabSheet();
    
    public SyncRunnerService() {
        tabSheet.setSizeFull();
    }
        
    public void launchAdHocAutomatedSynchronizationTask(Window window, WebserviceBean webserviceBean, RemoteSession remoteSession, List<RemoteSynchronizationProvider> syncProviders, RemoteSynchronizationConfiguration syncConfig) {
        this.window = window;
        this.window.setContent(tabSheet);
        tabSheet.removeAllComponents();
        launchNextAdHocAutomatedSynchronizationTask(webserviceBean, remoteSession, syncProviders, syncConfig);
    }
    
    private void launchNextAdHocAutomatedSynchronizationTask(WebserviceBean webserviceBean, RemoteSession remoteSession, List<RemoteSynchronizationProvider> syncProviders, RemoteSynchronizationConfiguration syncConfig) {
        if (!syncProviders.isEmpty()) {
            
            try {
                RemoteSynchronizationProvider syncProvider = syncProviders.get(0);
                
                BackgroundJob managedJob = webserviceBean.launchAdHocAutomatedSynchronizationTask(
                    new long[] {syncConfig.getId()},
                    syncProvider.getId(),
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
                        Thread.sleep(2000);
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
                grdSyncResult.setSizeFull();
                grdSyncResult.setItems((List<SyncResult>)managedJob.getJobResult());
                
                grdSyncResult.addColumn(SyncResult::getTypeAsIcon, new HtmlRenderer()).setCaption("Type").setMaximumWidth(65).setWidth(65).setMaximumWidth(65).setDescriptionGenerator(e -> "<b>" + e.getTypeAsString() + "</b>", ContentMode.HTML);;
                grdSyncResult.addColumn(SyncResult::getActionDescription).setCaption("Action Description");
                grdSyncResult.addColumn(SyncResult::getResult).setCaption("Result");
                                
                vly.addComponent(grdSyncResult);
                vly.setComponentAlignment(grdSyncResult, Alignment.MIDDLE_CENTER);
                                
                tabSheet.addTab(vly, syncProvider.getDisplayName());
////                System.out.println(">>>" + syncProvider.getValue());
                syncProviders.remove(0);
                launchNextAdHocAutomatedSynchronizationTask(webserviceBean, remoteSession, syncProviders, syncConfig);
                
            } catch (ServerSideException | RuntimeException ex) {
                Notifications.showError(ex.getMessage());
            }                       
        }
    }
}
