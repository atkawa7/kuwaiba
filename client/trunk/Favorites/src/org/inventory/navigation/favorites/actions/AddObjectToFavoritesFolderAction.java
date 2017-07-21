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
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.favorites.windows.ChooseFavoritesFolderFrame;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to add object to bookmark
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AddObjectToFavoritesFolderAction extends GenericObjectNodeAction {
        
    public AddObjectToFavoritesFolderAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/favorites/Bundle")
            .getString("LBL_ADD_FAVORITE"));
    }
            
    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalFavoritesFolder> favoritesFolders = CommunicationsStub.getInstance().getFavoritesFoldersForUser();
                
        if (favoritesFolders == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            if (favoritesFolders.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no favorites folders created. Create at least one using the Favorites Module", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ChooseFavoritesFolderFrame frame = new ChooseFavoritesFolderFrame(selectedObjects, favoritesFolders);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_FAVORITES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
