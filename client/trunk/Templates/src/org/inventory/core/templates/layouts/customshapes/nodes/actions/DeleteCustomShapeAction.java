/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts.customshapes.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFileObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.core.templates.layouts.customshapes.nodes.CustomShapeNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * This action deletes custom shape items
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteCustomShapeAction extends GenericInventoryAction implements Presenter.Popup {
    private final JMenuItem popupPresenter;
    
    public DeleteCustomShapeAction() {
        putValue(NAME, I18N.gm("action_name_delete_custom_shape"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }    

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CustomShapeNode node = Utilities.actionsGlobalContext().lookup(CustomShapeNode.class);
        if (node == null)
            return;
        LocalObjectListItem customShape = Utilities.actionsGlobalContext().lookup(LocalObjectListItem.class);
        if (customShape == null)
            return;
        
        if (JOptionPane.showConfirmDialog(null, 
            I18N.gm("confirm_dialog_dlt_custom_shape_message"), 
            I18N.gm("confirm_dialog_dlt_custom_shape_title"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            List<LocalFileObjectLight> files = CommunicationsStub.getInstance().getFilesForObject(customShape.getClassName(), customShape.getId());
            if (files != null) {
                files.forEach(file -> {
                    if (file.getTags() != null && file.getTags().contains("icon")) { //NOI18N
                        if (!CommunicationsStub.getInstance().detachFileFromObject(file.getFileOjectId(), customShape.getClassName(), customShape.getId())) {
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        }
                    }
                });

                if (CommunicationsStub.getInstance().deleteListTypeItem(customShape.getClassName(), customShape.getId(), false)) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                        NotificationUtil.INFO_MESSAGE, I18N.gm("custom_shape_deleted_successfully"));
                    ((AbstractChildren) node.getParentNode().getChildren()).addNotify();
                    //Refresh cache
                    CommunicationsStub.getInstance().getList(customShape.getClassName(), false, true);
                } else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
