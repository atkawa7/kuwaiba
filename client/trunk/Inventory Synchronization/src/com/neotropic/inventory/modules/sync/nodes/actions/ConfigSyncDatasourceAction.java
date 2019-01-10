/*
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.inventory.modules.sync.nodes.actions;

import com.neotropic.inventory.modules.sync.*;
import com.neotropic.inventory.modules.sync.nodes.SyncDataSourceConfigurationNode;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.navigation.navigationtree.windows.ObjectEditorTopComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates/edits the data source configuration of the object
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.HAS_CONFIGURATION)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConfigSyncDatasourceAction extends GenericObjectNodeAction implements ComposedAction{
 
    SyncDataSourceConfigurationNode syncDataSourceConfigurationNode;

    public ConfigSyncDatasourceAction() {
        putValue(NAME, "Config Datasource");
    }
    
    private JComponent setSize(JComponent component) {
        Dimension size = new Dimension(200, 20);
        
        component.setMinimumSize(size);
        component.setMaximumSize(size);
        component.setPreferredSize(size);
        component.setSize(size);
        return component;
    }
  
    @Override
    public String[] appliesTo() {
         return new String[] {Constants.CLASS_GENERICNETWORKELEMENT};
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        List<LocalSyncGroup> syncGroups = CommunicationsStub.getInstance().getSyncGroups();
        
        LocalSyncDataSourceConfiguration syncDataSourceConfiguration = CommunicationsStub.getInstance().getSyncDataSourceConfiguration(selectedObjects.get(0).getId());
        
         if (syncGroups ==  null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (syncGroups.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no sync gropus created. Create at least one using the Sync Manager", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                if(syncDataSourceConfiguration == null){    
                    SelectValueFrame frame = new SelectValueFrame(
                        "Available Sync Groups",
                        "Search",
                        "Create Relationship", syncGroups);
                    frame.addListener(this);
                    frame.setVisible(true);
                }
                else{
                    syncDataSourceConfigurationNode = new SyncDataSourceConfigurationNode(syncDataSourceConfiguration);
                    
                    ObjectEditorTopComponent component = new ObjectEditorTopComponent(syncDataSourceConfigurationNode);
                    component.open();
                    component.requestActive();
            
                    NotificationUtil.getInstance().showSimplePopup("Datasource is already created", 
                               NotificationUtil.INFO_MESSAGE, "Edit in property sheet");
                }
            }
        }
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame){
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedSyncGroup =  frame.getSelectedValue();
            
            if (selectedSyncGroup  == null)
                JOptionPane.showMessageDialog(null, "Select a sync group from the list");

            HashMap<String, String> parameters = new HashMap();
                        parameters.put("deviceId", String.valueOf((selectedObjects.get(0).getId())));
                        parameters.put("deviceClass", (selectedObjects.get(0).getClassName()));
        
            LocalSyncDataSourceConfiguration newSyncConfig = CommunicationsStub.getInstance().
                                    createSyncDataSourceConfiguration(
                                            selectedObjects.get(0).getId(),
                                            ((LocalSyncGroup) selectedSyncGroup ).getId(), 
                                            selectedObjects.get(0).getName() + " [Datasource config]", parameters);
            
            syncDataSourceConfigurationNode = new SyncDataSourceConfigurationNode(newSyncConfig);

            ObjectEditorTopComponent component = new ObjectEditorTopComponent(syncDataSourceConfigurationNode);
            component.open();
            component.requestActive();
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }
}