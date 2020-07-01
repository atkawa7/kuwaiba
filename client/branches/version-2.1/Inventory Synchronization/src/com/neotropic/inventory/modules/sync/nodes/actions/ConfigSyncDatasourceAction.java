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

import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import com.neotropic.inventory.modules.sync.nodes.SyncDataSourceConfigurationNode;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
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
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Creates/edits the data source configuration of the object
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.DEVICE_CONFIGURATION)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConfigSyncDatasourceAction extends GenericObjectNodeAction implements ComposedAction {
 
    SyncDataSourceConfigurationNode syncDataSourceConfigurationNode;

    public ConfigSyncDatasourceAction() {
        putValue(NAME, "Configure Sync Datasource");
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
              
        if (syncGroups ==  null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else {
            if (syncGroups.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no sync groups created. Create at least one using the Sync Manager", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                LocalSyncDataSourceConfiguration syncDataSourceConfiguration = CommunicationsStub.getInstance().getSyncDataSourceConfiguration(selectedObjects.get(0).getId());
                if(syncDataSourceConfiguration == null) {    
                    SelectValueFrame frame = new SelectValueFrame(
                        "Available Sync Groups",
                        "Search",
                        "Add to Sync Group", syncGroups);
                    frame.addListener(this);
                    frame.setVisible(true);
                } else {
                    syncDataSourceConfigurationNode = new SyncDataSourceConfigurationNode(syncDataSourceConfiguration);
                    
                    SyncDatasourceConfigEditorTopComponent component = new SyncDatasourceConfigEditorTopComponent(syncDataSourceConfigurationNode);
                    component.open();
                    component.requestActive();
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
            
            else{
                HashMap<String, String> parameters = new HashMap();
                            parameters.put("deviceId", String.valueOf((selectedObjects.get(0).getId())));
                            parameters.put("deviceClass", (selectedObjects.get(0).getClassName()));

                LocalSyncDataSourceConfiguration newSyncConfig = CommunicationsStub.getInstance().
                                        createSyncDataSourceConfiguration(
                                                selectedObjects.get(0).getId(),
                                                ((LocalSyncGroup)selectedSyncGroup).getId(), 
                                                selectedObjects.get(0).getName(), parameters);

                if (newSyncConfig != null) {
                    syncDataSourceConfigurationNode = new SyncDataSourceConfigurationNode(newSyncConfig);
                    ObjectEditorTopComponent component = new ObjectEditorTopComponent(syncDataSourceConfigurationNode);
                    component.open();
                    component.requestActive();
                } else 
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }
    
    public class SyncDatasourceConfigEditorTopComponent extends TopComponent {

        private PropertySheetView editor;
        private Node node;

        public SyncDatasourceConfigEditorTopComponent(Node node) {
            editor = new PropertySheetView();
            this.node = node;
            this.setDisplayName(node.getDisplayName());
            setLayout(new BorderLayout());
            add(editor);
            //This requires that CoreUI to be enable in the project
            Mode myMode = WindowManager.getDefault().findMode("properties");
            myMode.dockInto(this);
        }

        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

        @Override
        public void componentOpened() {
            //This is important. If setNodes is called in the constructor, it won't work!
            editor.setNodes(new Node[]{ node });
        }
    }
}
