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
package org.inventory.bookmarks.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.bookmarks.nodes.BookmarkNode;
import org.inventory.bookmarks.nodes.BookmarkRootNode.BookmarkRootChildren;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteBookmarkCategoryAction extends GenericInventoryAction {
    
    public DeleteBookmarkCategoryAction() {
        putValue(NAME, "Delete Bookmark Category");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this bookmark category?", 
            "Warning", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            Iterator<? extends BookmarkNode> selectedNodes = Utilities.actionsGlobalContext()
                .lookupResult(BookmarkNode.class).allInstances().iterator();

            if (!selectedNodes.hasNext())
                return;
            
            BookmarkNode selectedNode = selectedNodes.next();
            
            List<Long> ids = new ArrayList();
            ids.add(selectedNode.getLocalBookmark().getId());
            
            if (CommunicationsStub.getInstance().deleteBookmark(ids)) {
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, 
                    "The selected bookmark category was deleted successfully");
                
                ((BookmarkRootChildren) selectedNode.getParentNode().getChildren()).addNotify();
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
