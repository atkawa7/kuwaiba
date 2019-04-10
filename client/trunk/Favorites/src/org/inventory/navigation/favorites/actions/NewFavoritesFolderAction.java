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
package org.inventory.navigation.favorites.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.favorites.nodes.FavoritesFolderRootNode;
import org.inventory.navigation.favorites.nodes.FavoritesFolderRootNode.FavoritesFolderRootChildren;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Action to create a new Favorites folder
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewFavoritesFolderAction extends GenericInventoryAction {
    private static NewFavoritesFolderAction instance;
    
    private NewFavoritesFolderAction() {
        putValue(NAME, I18N.gm("new_favorites_folder"));
    }
    
    public static NewFavoritesFolderAction getInstance() {
        return instance == null ? instance = new NewFavoritesFolderAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends FavoritesFolderRootNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(FavoritesFolderRootNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        FavoritesFolderRootNode selectedNode = selectedNodes.next();
        
        JTextField txtPoolName = new JTextField();
        txtPoolName.setName("txtFavoritesFolderName");
        txtPoolName.setColumns(10);
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(
            new String[] {I18N.gm("folder_name")}, 
            new JComponent[] {txtPoolName});
        
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, I18N.gm("new_favorites_folder"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            LocalFavoritesFolder newFavorites = CommunicationsStub.getInstance().createFavoritesFolderForUser(
                ((JTextField) pnlPoolProperties.getComponent("txtFavoritesFolderName")).getText());
            
            if (newFavorites == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                ((FavoritesFolderRootChildren) selectedNode.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("favorites_folder_created_successfully"));
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_FAVORITES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
