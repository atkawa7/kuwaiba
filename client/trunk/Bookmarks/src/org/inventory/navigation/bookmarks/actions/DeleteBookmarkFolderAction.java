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
package org.inventory.navigation.bookmarks.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.bookmarks.nodes.BookmarkFolderNode;
import org.inventory.navigation.bookmarks.nodes.BookmarkFolderRootNode.BookmarkFolderRootChildren;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.openide.util.Utilities;

/**
 * Action to delete a Bookmark folder
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteBookmarkFolderAction extends GenericInventoryAction {
    private static DeleteBookmarkFolderAction instance;
    
    private DeleteBookmarkFolderAction() {
        putValue(NAME, ResourceBundle.getBundle("org/inventory/navigation/bookmarks/Bundle")
            .getString("ACTION_NAME_DELETE_BOOKMARK"));
    }
    
    public static DeleteBookmarkFolderAction getInstance() {
        return instance == null ? instance = new DeleteBookmarkFolderAction() : instance;        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this Bookmark folder?", 
            "Warning", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            Iterator<? extends BookmarkFolderNode> selectedNodes = Utilities.actionsGlobalContext()
                .lookupResult(BookmarkFolderNode.class).allInstances().iterator();

            if (!selectedNodes.hasNext())
                return;
            
            BookmarkFolderNode selectedNode = selectedNodes.next();
            
            List<Long> ids = new ArrayList();
            ids.add(selectedNode.getLocalBookmark().getId());
            
            if (CommunicationsStub.getInstance().deleteBookmarkFolders(ids)) {
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, 
                    "The selected Bookmark folder was deleted successfully");
                
                ((BookmarkFolderRootChildren) selectedNode.getParentNode().getChildren()).addNotify();
            } else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_BOOKMARKS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
