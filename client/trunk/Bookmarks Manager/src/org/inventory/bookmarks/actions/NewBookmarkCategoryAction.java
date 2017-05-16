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
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBookmark;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.bookmarks.nodes.BookmarkRootNode;
import org.inventory.bookmarks.nodes.BookmarkRootNode.BookmarkRootChildren;
import org.openide.util.Utilities;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NewBookmarkCategoryAction extends AbstractAction {
    
    public NewBookmarkCategoryAction() {
        putValue(NAME, "New Bookmark");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends BookmarkRootNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(BookmarkRootNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        BookmarkRootNode selectedNode = selectedNodes.next();
        
        JTextField txtPoolName = new JTextField();
        txtPoolName.setName("txtBookmarkName");
        txtPoolName.setColumns(10);
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(
            new String[] {"Bookmark Name"}, 
            new JComponent[] {txtPoolName});
        
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, "New Bookmark", 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            LocalBookmark newBookmark = CommunicationsStub.getInstance().createBookmarkForUser(
                ((JTextField) pnlPoolProperties.getComponent("txtBookmarkName")).getText());
            
            if (newBookmark == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                ((BookmarkRootChildren) selectedNode.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup("Information", 
                    NotificationUtil.INFO_MESSAGE, "Bookmark created");
            }
        }
    }
}
