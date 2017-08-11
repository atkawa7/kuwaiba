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
package org.inventory.navigation.favorites.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.inventory.navigation.favorites.nodes.FavoritesFolderNode;
import org.inventory.navigation.favorites.nodes.FavoritesFolderNode.FavoritesFolderChildren;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to remove an associated object to a favoritesFolder
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RemoveObjectFromFavoritesFolder extends GenericObjectNodeAction implements ComposedAction {
    
    public RemoveObjectFromFavoritesFolder() {
        putValue(NAME, ResourceBundle.getBundle("org/inventory/navigation/favorites/Bundle").getString("LBL_REMOVE_FAVORITE"));
    }
    
    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0); //Uses the last selected only
        List<LocalFavoritesFolder> favoritesFolder = CommunicationsStub.getInstance()
            .objectIsBookmarkItemIn(selectedObject.getClassName(), selectedObject.getOid());
        
        if (favoritesFolder!= null) {
            if (!favoritesFolder.isEmpty()) {
                List<SubMenuItem> subMenuItems = new ArrayList();
                for (LocalFavoritesFolder favoriteFolder : favoritesFolder) {
                    SubMenuItem subMenuItem = new SubMenuItem(favoriteFolder.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, favoriteFolder.getId());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            } else {
                JOptionPane.showMessageDialog(null, "There are not favorites folders related to the selected object", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
        
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_FAVORITES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want remove this object from the favorites folder?", "Warning", 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();

                List<String> objClass = new ArrayList();
                List<Long> objId = new ArrayList();

                boolean success = true;
                while (selectedNodes.hasNext()) {
                    ObjectNode selectedNode = selectedNodes.next();

                    objClass.add(selectedNode.getObject().getClassName());
                    objId.add(selectedNode.getObject().getOid());

                    if (CommunicationsStub.getInstance().removeObjectsFromFavoritesFolder(
                        objClass, 
                        objId, 
                        (Long) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID))) {

                        if (selectedNode.getParentNode() instanceof FavoritesFolderNode)
                            ((FavoritesFolderChildren) selectedNode.getParentNode().getChildren()).addNotify();


                    } else {
                        success = false;
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                }

                if (success)
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The selected objects were removed from the favorites folder");
            }
        }
    }
}
