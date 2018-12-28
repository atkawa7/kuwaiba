/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import com.neotropic.inventory.modules.sync.LocalSyncFinding;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import com.neotropic.inventory.modules.sync.AbstractRunnableSyncFindingsManager;
import com.neotropic.inventory.modules.sync.AbstractRunnableSyncResultsManager;
import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.LocalSyncProvider;
import com.neotropic.inventory.modules.sync.LocalSyncResult;
import com.neotropic.inventory.modules.sync.nodes.SyncDataSourceConfigurationNode;
import com.neotropic.inventory.modules.sync.nodes.actions.windows.SyncResultsFrame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * This action launches the synchronization process for a given sync group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class RunSynchronizationProcessAction extends GenericInventoryAction{
    
    private static RunSynchronizationProcessAction instace;
        
    public RunSynchronizationProcessAction() {
        putValue(NAME, I18N.gm("run_sync_process"));
    }
    
    public static RunSynchronizationProcessAction getInstance(){
        return instace == null ? instace = new RunSynchronizationProcessAction() : instace;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        LocalSyncProvider[] availableProviders = {
            new LocalSyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.reference.ReferenceSnmpSyncProvider", "Physical / Virtual Interfaces", true),
            new LocalSyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.mpls.SnmpMplsSyncProvider", "General MPLS Information", true),
            new LocalSyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.ip.IPAddressesSyncProvider", "IP Addresses", true),
            new LocalSyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.vlan.SnmpCiscoVlansSyncProvider", "VLANs", true),
            new LocalSyncProvider("com.neotropic.kuwaiba.sync.connectors.ssh.bdi.BridgeDomainSyncProvider", "Bridge Domains", true),
            new LocalSyncProvider("com.neotropic.kuwaiba.sync.connectors.snmp.bgp.BgpSyncProvider", "Border Gateway Protocol", true)};
         
        Iterator<? extends AbstractNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(AbstractNode.class).allInstances().iterator();
        
        List<LocalSyncDataSourceConfiguration> syncDataSources = new ArrayList<>();
        
        if (!selectedNodes.hasNext())
            return;
        
        final LocalSyncGroup localSyncGroup;
        
        List<AbstractNode> nodes = new ArrayList<>();
        while(selectedNodes.hasNext()){
            nodes.add(selectedNodes.next());
        }
            
        if(nodes.size() == 1 &&  nodes.get(0) instanceof SyncGroupNode){
            localSyncGroup = ((SyncGroupNode)nodes.get(0)).getLookup().lookup(LocalSyncGroup.class);
            for (Node child : ((SyncGroupNode)nodes.get(0)).getChildren().getNodes())
                syncDataSources.add(child.getLookup().lookup(LocalSyncDataSourceConfiguration.class));
            
            localSyncGroup.setDataSourceConfig(syncDataSources);
        }
        else{
            for(AbstractNode selectedNode : nodes){
                 if(selectedNode instanceof SyncDataSourceConfigurationNode)
                     syncDataSources.add(((SyncDataSourceConfigurationNode)selectedNode).getLookup().lookup(LocalSyncDataSourceConfiguration.class));
            }
            localSyncGroup = new LocalSyncGroup(-1, "adhocSyncGroup", syncDataSources);
        }        

        final JList<LocalSyncProvider> lstProvidres = new JList<>(availableProviders);
        
        final JFrame frame = new JFrame("Available Syncrhonization Providers");
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 350);
        frame.setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel("Select the providers (click + crtl for multiple selection)");
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.add(lblInstructions, BorderLayout.NORTH);
        frame.add(lstProvidres, BorderLayout.CENTER);
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Run Synchronization");
        pnlButtons.add(btnRelate);
        
        btnRelate.addActionListener(new ActionListener(){
        
            @Override
            public void actionPerformed(ActionEvent e) {
                if (localSyncGroup.getDataSourceConfig().isEmpty()) 
                    JOptionPane.showMessageDialog(null, I18N.gm("sync_no_configs"), I18N.gm("error"), JOptionPane.WARNING_MESSAGE);
                else{
                    //we check every single datasource configuration
                    for (LocalSyncDataSourceConfiguration dsConfig : localSyncGroup.getDataSourceConfig()) {
                        if (dsConfig != null) {
                            HashMap<String, String> parameters = dsConfig.getParameters();
                            Long deviceId = parameters.containsKey("deviceId") ? Long.valueOf(parameters.get("deviceId")) : null;
                            String deviceClass = parameters.containsKey("deviceClass") ? parameters.get("deviceClass") : null;
                            if (deviceClass != null && deviceId != null) {
                                LocalObjectLight deviceObj = CommunicationsStub.getInstance().getObjectInfoLight(deviceClass, deviceId);
                                if (deviceObj == null) {
                                    JOptionPane.showMessageDialog(null,
                                        String.format("The inventory synchronization cannot be run because the device for the data source configuration %s is not assigned or was removed", dsConfig.toString()), 
                                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            } else {
                                JOptionPane.showMessageDialog(null,
                                    String.format("The inventory synchronization cannot be run because the device for the data source configuration %s is not assigned or was removed", dsConfig.toString()), 
                                    I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                    
                    frame.dispose();
                    DefaultSyncResultsManager dsfm = new DefaultSyncResultsManager(localSyncGroup, lstProvidres.getSelectedValuesList());
                    dsfm.runFirst();
                    dsfm.getProgressHandle().start(lstProvidres.getSelectedValuesList().size());
                    dsfm.getProgressHandle().progress(lstProvidres.getSelectedValuesList().get(0).getDisplayName(), 0);
                }
            }
        });
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        pnlButtons.add(btnClose);
        frame.add(pnlButtons, BorderLayout.SOUTH);
        frame.setVisible(true);
        
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    /**
     * Gets the list of findings and shows a dialog to allow the user to choose what actions will be performed
     */
    private class DefaultSyncFindingsManager extends AbstractRunnableSyncFindingsManager {

        LocalSyncGroup syncGroup; 
        
        public DefaultSyncFindingsManager() {
            setProgressHandle(ProgressHandleFactory.createHandle(
                String.format(I18N.gm("running_sync_process"), 
                syncGroup.getName())));
            RequestProcessor.getDefault().post(this);
        }
        
        @Override
        public void handleSyncFindings() {
            List<LocalSyncFinding> findings = getFindings();
            if (findings == null)//can't connect to the router
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else {
                if(findings.isEmpty())
                    JOptionPane.showMessageDialog(null, I18N.gm("sync_no_findings"), I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                else{
                    SyncActionsFrame syncWizard = new SyncActionsFrame(syncGroup, findings);
                    syncWizard.setVisible(true);
                }
            }
        }
    }
    
    private class DefaultSyncResultsManager extends AbstractRunnableSyncResultsManager {
        
        LocalSyncGroup syncGroup; 
        List<LocalSyncProvider> syncProviders;
        List<Long> syncDataSourceConfigIds;
        
        public DefaultSyncResultsManager(LocalSyncGroup syncGroup, List<LocalSyncProvider> syncProviders) {
            setProgressHandle(ProgressHandleFactory.createHandle(
                String.format(I18N.gm("running_sync_process"), 
                syncGroup.getName())));
            RequestProcessor.getDefault().post(this);
            this.syncGroup = syncGroup;
            this.syncProviders = syncProviders;
        }
        
        public void runFirst() {
            if(!syncProviders.isEmpty()){
                LocalSyncProvider provider = syncProviders.get(0);

                if (provider.isAutomated() && syncGroup.getId() != -1)
                    CommunicationsStub.getInstance().launchAutomatedSynchronizationTask(syncGroup, provider.getId(), syncProviders.size(), this);
                else
                    CommunicationsStub.getInstance().launchAdHocAutomatedSynchronizationTask(syncGroup.getDataSourceConfig(), provider.getId(), syncProviders.size(), this); 
            }
        }

        @Override
        public void handleSyncResults() {
            List<LocalSyncResult> results = getSyncResults();
            if (results == null) //Can't connect to the device
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else {
                if(results.isEmpty())
                    JOptionPane.showMessageDialog(null, I18N.gm("sync_no_findings"), I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                else{
                    SyncResultsFrame frmSyncResults = new SyncResultsFrame(syncGroup, results);
                    frmSyncResults.setVisible(true);
                }
            }
            
            syncProviders.remove(0);
            if(syncProviders.isEmpty())
                getProgressHandle().finish();
            else{
                getProgressHandle().progress(syncProviders.get(0).getDisplayName(), 1);
                runFirst();
            }
        }
    }
}
