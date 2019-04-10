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

import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.nodes.actions.SyncManagerActionFactory;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;

/**
 * The root node of the sync groups tree.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SyncGroupRootNode extends AbstractNode {
    public static final String ICON_PATH = "com/neotropic/inventory/modules/sync/res/root.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    public SyncGroupRootNode() {
        super(new SyncGroupRootChildren());
        setDisplayName(I18N.gm("sync_groups"));
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action pasteAction = SystemAction.get(PasteAction.class);
        pasteAction.putValue(Action.NAME, I18N.gm("lbl_paste_action"));
            
        return new Action[] {
            SyncManagerActionFactory.getNewSyncGroupAction()
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
        if (paste != null)
            s.add(paste);
    }
        
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain a SyncGroupNode
        if (!(dropNode instanceof SyncGroupNode))
            return null;
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
                
        return new PasteType() {

            @Override
            public Transferable paste() throws IOException {
                LocalSyncGroup localSyncGroup = dropNode.getLookup().lookup(LocalSyncGroup.class);
                if (action == DnDConstants.ACTION_COPY) {
                    List<LocalSyncGroup> syncGroups = CommunicationsStub.getInstance().copySyncGroup(new LocalSyncGroup[] {localSyncGroup});
                    if (syncGroups != null) {
                        if (getChildren() instanceof SyncGroupRootChildren)
                            ((SyncGroupRootChildren) getChildren()).addNotify();
                    } else
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
                return null;
            }
        };
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    
    public static class SyncGroupRootChildren extends Children.Keys<Object> {
        
        @Override
        public void addNotify() {
            List<LocalSyncGroup> syncGroups = CommunicationsStub.getInstance().getSyncGroups();
            
            if (syncGroups == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"),
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());                                
            } else {
                Collections.sort(syncGroups);
                setKeys(syncGroups);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }

        @Override
        protected Node[] createNodes(Object key) {
            if(key instanceof LocalSyncGroup)
                return new Node[] { new SyncGroupNode((LocalSyncGroup)key) };
            if(key instanceof LocalSyncDataSourceConfiguration)
                return new Node[] { new SyncDataSourceConfigurationNode((LocalSyncDataSourceConfiguration)key) };
            return null;
        }
    }
}
