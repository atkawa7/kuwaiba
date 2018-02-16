/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package com.neotropic.inventory.modules.sync.nodes.actions;

import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode;
import com.neotropic.inventory.modules.sync.nodes.actions.windows.SyncActionsFrame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.communications.core.LocalSyncFinding;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.runnable.AbstractSyncRunnable;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * This action launches the synchronization process for a given sync group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
class RunSynchronizationProcessAction extends GenericInventoryAction {
    private SyncGroupNode selectedNode;
    public RunSynchronizationProcessAction() {
        putValue(NAME, I18N.gm("run_sync_process"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SyncGroupNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        selectedNode = selectedNodes.next();
        
        if (selectedNode.getChildren().getNodes().length == 0) {
            JOptionPane.showMessageDialog(null, I18N.gm("sync_no_configs"), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        }
        else {
            /*
            for (Node child : selectedNode.getChildren().getNodes()) {
                LocalSyncDataSourceConfiguration device = child.getLookup().lookup(LocalSyncDataSourceConfiguration.class);
                if (device != null) {
                    HashMap<String, String> parameters = device.getParameters();
                    Long deviceId = parameters.containsKey("deviceId") ? Long.valueOf(parameters.get("deviceId")) : null;
                    String deviceClass = parameters.containsKey("deviceClass") ? parameters.get("deviceClass") : null;
                    if (deviceClass != null && deviceId != null) {
                        LocalObjectLight deviceObj = CommunicationsStub.getInstance().getObjectInfoLight(deviceClass, deviceId);
                        if (deviceObj == null) {
                            JOptionPane.showMessageDialog(null,
                                String.format("The inventory synchronization cannot be run because the device for the data source configuration %s is not assigned or was removed", device.toString()), 
                                I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                            String.format("The inventory synchronization cannot be run because the device for the data source configuration %s is not assigned or was removed", device.toString()), 
                            I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            */
            SyncRunnable myRun = new SyncRunnable();
            CommunicationsStub.getInstance().launchSupervisedSynchronizationTask(selectedNode.getLookup().lookup(LocalSyncGroup.class), myRun);
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    
    /**
     * Gets the list of findings and shows a dialog to allow the user to choose what actions will be performed
     */
    private class SyncRunnable extends AbstractSyncRunnable {

        public SyncRunnable() {
            setProgressHandle(ProgressHandleFactory.createHandle(
                String.format(I18N.gm("running_sync_process"), 
                selectedNode.getName())));
            RequestProcessor.getDefault().post(this);
        }
        
        @Override
        public void runSync() {
            List<LocalSyncFinding> findings = getFindings();
            if (findings == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else {
                if(findings.isEmpty())
                    JOptionPane.showMessageDialog(null, I18N.gm("sync_no_findings"), I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                else{
                    SyncActionsFrame syncWizard = new SyncActionsFrame(selectedNode.getLookup().lookup(LocalSyncGroup.class), findings);
                    syncWizard.setVisible(true);
                }
            }
        }
    }
}
