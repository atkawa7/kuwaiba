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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to add object to a favorite folder
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AddObjectToFavoritesFolderAction extends GenericObjectNodeAction implements ComposedAction {
        
    public AddObjectToFavoritesFolderAction() {
        putValue(NAME, I18N.gm("add_to_favorite_folder"));
    }
            
    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalFavoritesFolder> favoritesFolders = CommunicationsStub.getInstance().getFavoritesFoldersForUser();
                
        if (favoritesFolders == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            if (favoritesFolders.isEmpty()) {
                JOptionPane.showMessageDialog(null, I18N.gm("no_favorite_folders_created_create_one_to_use_favorites_module"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame frame = new SelectValueFrame(I18N.gm("favorites_folder"), I18N.gm("search"), I18N.gm("create_relationship"), favoritesFolders);
                frame.addListener(this);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_FAVORITES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, I18N.gm("select_a_favorites_folder"));
            else {
                List<String> objectsClassName = new ArrayList<>();
                List<String> objectsId = new ArrayList<>();
                
                for (LocalObjectLight selectedObject : selectedObjects) {
                    objectsClassName.add(selectedObject.getClassName());
                    objectsId.add(selectedObject.getId());
                    
                    if (CommunicationsStub.getInstance()
                        .addObjectsToFavoritesFolder(objectsClassName, objectsId, ((LocalFavoritesFolder) selectedValue).getId())) {
                        
                        JOptionPane.showMessageDialog(null, String.format("%s " + I18N.gm("added_to_favorites_folder") + " %s", selectedObject, selectedValue));
                        frame.dispose();
                    } else
                        JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                    
                    objectsClassName.clear();
                    objectsId.clear();
                }
            }
        }
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
