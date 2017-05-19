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
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBookmark;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.bookmarks.nodes.BookmarkRootNode;
import org.inventory.navigation.bookmarks.nodes.BookmarkRootNode.BookmarkRootChildren;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.openide.util.Utilities;

/**
 * Action to create a new Bookmark folder
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NewBookmarkFolderAction extends GenericInventoryAction {
    private static NewBookmarkFolderAction instance;
    
    private NewBookmarkFolderAction() {
        putValue(NAME, ResourceBundle.getBundle("org/inventory/navigation/bookmarks/Bundle")
            .getString("ACTION_NAME_NEW_BOOKMARK"));
    }
    
    public static NewBookmarkFolderAction getInstance() {
        return instance == null ? instance = new NewBookmarkFolderAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends BookmarkRootNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(BookmarkRootNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        BookmarkRootNode selectedNode = selectedNodes.next();
        
        JTextField txtPoolName = new JTextField();
        txtPoolName.setName("txtBookmarkFolderName");
        txtPoolName.setColumns(10);
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(
            new String[] {"Folder Name"}, 
            new JComponent[] {txtPoolName});
        
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, "New Bookmark Folder", 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            LocalBookmark newBookmark = CommunicationsStub.getInstance().createBookmarkFolderForUser(
                ((JTextField) pnlPoolProperties.getComponent("txtBookmarkFolderName")).getText());
            
            if (newBookmark == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                ((BookmarkRootChildren) selectedNode.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup("Information", 
                    NotificationUtil.INFO_MESSAGE, "Bookmark folder created sucessfully");
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_BOOKMARKS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
