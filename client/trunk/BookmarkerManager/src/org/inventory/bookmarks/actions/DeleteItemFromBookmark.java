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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.bookmarks.nodes.BookmarkNode;
import org.inventory.bookmarks.nodes.BookmarkNode.BookmarkChildren;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBookmark;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class DeleteItemFromBookmark extends GenericObjectNodeAction implements Presenter.Popup {
    
    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (JOptionPane.showConfirmDialog(null, 
                "Are you sure you want remove this bookmark?", "Warning", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        
            Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
            
            List<String> objClass = new ArrayList();
            List<Long> objId = new ArrayList();
            
            boolean success = true;
            while (selectedNodes.hasNext()) {
                ObjectNode selectedNode = selectedNodes.next();
                
                objClass.add(selectedNode.getObject().getClassName());
                objId.add(selectedNode.getObject().getOid());
                
                if (CommunicationsStub.getInstance().releaseObjectsFromBookmark(
                    objClass, 
                    objId, 
                    Long.valueOf(((JMenuItem)e.getSource()).getName()))) {
                    
                    if (selectedNode.getParentNode() instanceof BookmarkNode)
                        ((BookmarkChildren) selectedNode.getParentNode().getChildren()).addNotify();
                    

                } else {
                    success = false;
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            }

            if (success)
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The selected resources were released from the bookmark");
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuServices = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/bookmarks/Bundle").getString("LBL_REMOVE_BOOKMARK"));
        mnuServices.setEnabled(false);
        
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (selectedNodes.hasNext()) {
        
            ObjectNode selectedNode = selectedNodes.next(); //Uses the last selected only
            
            List<LocalBookmark> bookmarks = CommunicationsStub.getInstance().objectIsBookmarkItemIn(
                selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getOid());
            
            if (bookmarks != null) {

                if (!bookmarks.isEmpty()) {
                    for (LocalBookmark bookmark : bookmarks){
                        JMenuItem smiServices = new JMenuItem(bookmark.toString());
                        smiServices.setName(String.valueOf(bookmark.getId()));
                        smiServices.addActionListener(this);
                        mnuServices.add(smiServices);
                    }
                    mnuServices.setEnabled(true);
                }
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        
        return mnuServices;
    }
}
