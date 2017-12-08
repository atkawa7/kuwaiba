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
package com.neotropic.inventory.modules.sync.nodes;

import com.neotropic.inventory.modules.sync.actions.NewSyncGroupAction;
import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * The root node of the sync groups tree.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncGroupRootNode extends AbstractNode {
    public static final String ICON_PATH = "com/neotropic/inventory/modules/sync/res/root.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    public SyncGroupRootNode() {
        super(new SyncGroupRootChildren());
        setDisplayName(I18N.gm("sync_groups"));
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{ NewSyncGroupAction.getInstance() };
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    
    public static class SyncGroupRootChildren extends Children.Keys<LocalSyncGroup> {
        
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
        protected Node[] createNodes(LocalSyncGroup key) {
            return new Node [] { new SyncGroupNode(key) };
        }
    }
}
