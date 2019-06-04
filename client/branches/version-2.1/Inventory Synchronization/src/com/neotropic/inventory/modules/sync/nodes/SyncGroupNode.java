/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sync.nodes;

import com.neotropic.inventory.modules.sync.nodes.actions.DeleteSyncAction;
import com.neotropic.inventory.modules.sync.nodes.actions.SyncManagerActionFactory;
import com.neotropic.inventory.modules.sync.nodes.properties.SyncGroupNativeTypeProperty;
import java.util.Collections;
import org.openide.util.ImageUtilities;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Node representing a sync group
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SyncGroupNode extends AbstractNode implements PropertyChangeListener {
    private static final Image NODE_ICON = ImageUtilities.loadImage("com/neotropic/inventory/modules/sync/res/sync_group.png");
        
    public SyncGroupNode(LocalSyncGroup localSyncGroup) {
        super(new SyncGroupNodeChildren(), Lookups.singleton(localSyncGroup));
    }
    
    @Override
    public void setName(String newName) {
        if (newName != null) {
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put("name", newName);
            if (CommunicationsStub.getInstance().updateSyncGroup(getLookup().lookup(LocalSyncGroup.class).getId(), attributes)) {
                getLookup().lookup(LocalSyncGroup.class).setName(newName);
                propertyChange(new PropertyChangeEvent(getLookup().lookup(LocalSyncGroup.class), Constants.PROPERTY_NAME, "", newName));
                if (getSheet() != null)
                   setSheet(createSheet());
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());                
            }
        }
    }
    
    @Override
    public String getName(){
        return getLookup().lookup(LocalSyncGroup.class).getName();
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalSyncGroup.class).toString();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action copyAction = SystemAction.get(CopyAction.class);
        copyAction.putValue(Action.NAME, I18N.gm("lbl_copy_action"));

        Action cutAction = SystemAction.get(CutAction.class);
        cutAction.putValue(Action.NAME, I18N.gm("lbl_cut_action"));

        Action pasteAction = SystemAction.get(PasteAction.class);
        pasteAction.putValue(Action.NAME, I18N.gm("lbl_paste_action"));
            
        return new Action[] {
            SyncManagerActionFactory.getNewRunSynchronizationProcessAction(),
            null, 
            pasteAction, 
            null, 
            DeleteSyncAction.getInstance()
        };
    }
    
    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        //From the transferable we figure out if it comes from a copy or a cut operation
        PasteType paste = getDropType(t, NodeTransfer.node(t, NodeTransfer.CLIPBOARD_COPY) != null
                ? DnDConstants.ACTION_COPY : DnDConstants.ACTION_MOVE, -1);
        //It's also possible to define many paste types (like "normal paste" and "special paste")
        //by adding more entries to the list. Those will appear as options in the context menu
        if (paste != null) {
            s.add(paste);
        }
    }
    
    @Override
    public Transferable drag() throws IOException {        
        return getLookup().lookup(LocalSyncGroup.class);
    }
    
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
            NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain a SyncConfigurationNode
        if (!(dropNode instanceof SyncDataSourceConfigurationNode))
            return null;
                        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
        
        return new PasteType() {

            @Override
            public Transferable paste() throws IOException {
                LocalSyncDataSourceConfiguration dataSrcConfig = dropNode.getLookup().lookup(LocalSyncDataSourceConfiguration.class);
                LocalSyncGroup oldSyncGroup = dropNode.getParentNode().getLookup().lookup(LocalSyncGroup.class);
                LocalSyncGroup newSyncGroup = getLookup().lookup(LocalSyncGroup.class);
                
                switch(action) {
                    case DnDConstants.ACTION_COPY:
                        if (CommunicationsStub.getInstance().copySyncDataSourceConfiguration(newSyncGroup.getId(), new LocalSyncDataSourceConfiguration[] {dataSrcConfig})) {
                            if (getChildren() instanceof SyncGroupNodeChildren)
                                ((SyncGroupNodeChildren) getChildren()).addNotify();
                        } else
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        break;
                    case DnDConstants.ACTION_MOVE:
                        if (CommunicationsStub.getInstance().moveSyncDataSourceConfiguration(oldSyncGroup.getId(), 
                                newSyncGroup.getId(), new LocalSyncDataSourceConfiguration[] {dataSrcConfig})) {
                            //Refreshes the old parent node
                            if (dropNode.getParentNode().getChildren() instanceof SyncGroupNodeChildren)
                                ((SyncGroupNodeChildren) dropNode.getParentNode().getChildren()).addNotify();
                            //Refreshes the new parent node
                            if (getChildren() instanceof SyncGroupNodeChildren)
                                ((SyncGroupNodeChildren) getChildren()).addNotify();
                        } else
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        break;
                }
                return null;
            }
        };
    }
    
    @Override
    public Image getIcon(int i) {
        return NODE_ICON;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return NODE_ICON;
    }
    
    @Override
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
        
        LocalSyncGroup localSyncGroup = getLookup().lookup(LocalSyncGroup.class);                
        
        PropertySupport.ReadWrite propertyName = new SyncGroupNativeTypeProperty(Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, this, localSyncGroup.getName());
        //PropertySupport.ReadWrite propertySyncProvider = new SyncGroupNativeTypeProperty("syncProvider", String.class, I18N.gm("sync_provider"), "", this, localSyncGroup.getProvider());
                
        generalPropertySet.put(propertyName);
        //generalPropertySet.put(propertySyncProvider);
        
        generalPropertySet.setName(I18N.gm("general_properties"));
        generalPropertySet.setDisplayName(I18N.gm("general_properties"));
        
        sheet.put(generalPropertySet);
        return sheet;    
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SyncGroupNode && 
            ((SyncGroupNode) obj).getLookup().lookup(LocalSyncGroup.class).equals(getLookup().lookup(LocalSyncGroup.class));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(getLookup().lookup(LocalSyncGroup.class));
        return hash;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(getLookup().lookup(LocalSyncGroup.class))) {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, getLookup().lookup(LocalSyncGroup.class).getName());
            }
        }
    }
    
    public static class SyncGroupNodeChildren extends Children.Keys<LocalSyncDataSourceConfiguration> {
        
        @Override
        public void addNotify() {
            LocalSyncGroup selectedSyncGroup = ((SyncGroupNode) getNode()).getLookup().lookup(LocalSyncGroup.class);
            List<LocalSyncDataSourceConfiguration> dataSourceConfigurations = CommunicationsStub.getInstance().
                    getSyncDataSourceConfigurations(selectedSyncGroup.getId());
            
            if (dataSourceConfigurations == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(dataSourceConfigurations);
                setKeys(dataSourceConfigurations);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalSyncDataSourceConfiguration key) {
            return new Node [] { new SyncDataSourceConfigurationNode(key) };
        }
    }
}
