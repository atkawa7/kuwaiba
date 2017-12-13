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
import com.neotropic.inventory.modules.sync.nodes.actions.windows.SyncActionWizard;
import com.neotropic.inventory.modules.sync.windows.SyncResultsFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.core.LocalSyncResult;
import org.inventory.communications.runnable.AbstractSyncRunnable;

import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * This action launches the synchronization process for a given sync group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
class RunSynchronizationProcessAction extends GenericInventoryAction {
    
    public RunSynchronizationProcessAction() {
        putValue(NAME, I18N.gm("run_sync_process"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SyncGroupNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        SyncGroupNode selectedNode = selectedNodes.next();
        SyncRunnable myRun = new SyncRunnable(selectedNode);
        CommunicationsStub.getInstance().launchSupervisedSynchronizationTask(selectedNode.getLookup().lookup(LocalSyncGroup.class).getId(), myRun);
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    
    /**
     * Gets the list of findings and show a dialog to allow the user to choose de actions to findings 
     */
    private class SyncRunnable extends AbstractSyncRunnable {

        public SyncRunnable(SyncGroupNode selectedNode) {
            setProgressHandle(ProgressHandleFactory.createHandle(
                String.format(I18N.gm("running_sync_process"), 
                selectedNode.getName())));
            RequestProcessor.getDefault().post(this);
        }
        
        @Override
        public void runSync() {
            final List<Integer> syncActions = new ArrayList<>();
            SyncActionWizard syncWizard = new SyncActionWizard(new LocalSyncGroup(0l, "Sync Group", DEFAULT), getFindings());
            syncWizard.setVisible(true);
        }
    }
}



//            for(LocalSyncFinding find : getFindings()){
//                JTextField txtFindInfo = new JTextField();
//                            txtFindInfo.setName("findInfo");
//                            txtFindInfo.setText(find.getDescription());
//                            txtFindInfo.setColumns(50);
//                String[] options = {"Execute", "Ignore"};
//
//                JLabel lblextra = new JLabel();
//                                lblextra.setName("findExtraInfo");
//                                //lblextra.setColumns(50);
//                                lblextra.setEnabled(false);
//
//                JComboBox<String> cbAction = new JComboBox<>(options);
//                cbAction.setName("cmbDevices");
//
//                JCheckBox jcExecuteAll = new JCheckBox();
//                jcExecuteAll.setText("Yes to all");
//                jcExecuteAll.setName("Answer yes to all findings");
//                
//                JComplexDialogPanel pnlSyncDataSourcePropertie = new JComplexDialogPanel(
//                        new String[] {"Info",
//                            "Extra Info:","Actions","Yes to all"}, 
//                        new JComponent[] {txtFindInfo, lblextra, cbAction, jcExecuteAll});
//                
//                
//                
//                if (JOptionPane.showConfirmDialog(null, pnlSyncDataSourcePropertie, I18N.gm("sync_data_action_select"), 
//                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
//                    syncActions.add(cbAction.getSelectedIndex());
//                    if(jcExecuteAll.isSelected()){
//                        syncActions = new ArrayList<>();
//                        for(LocalSyncFinding x : getFindings()){
//                            System.out.println(x.getDescription());
//                            syncActions.add(1);
//                        }
//                        break;
//                    }
//                }
//            }