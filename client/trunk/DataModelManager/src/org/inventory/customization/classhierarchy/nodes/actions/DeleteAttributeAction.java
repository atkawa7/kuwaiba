/*
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.classhierarchy.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;
import org.openide.util.actions.Presenter;

/**
 * Deletes an attribute
 * @author Adrian Martinez Molina {@literal <charles.bedon@kuwaiba.org>}
 */
public class DeleteAttributeAction extends GenericInventoryAction implements ComposedAction, Presenter.Popup {

    private ClassMetadataNode classNode;
    private CommunicationsStub com;
    private final JMenuItem popupPresenter;

    public DeleteAttributeAction() {
        putValue(NAME, I18N.gm("delete_attribute"));
        com = CommunicationsStub.getInstance();
        
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }

    public DeleteAttributeAction(ClassMetadataNode classNode) {
        this();
        this.classNode = classNode;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        LocalClassMetadata metaForThisClass = com.getMetaForClass(classNode.getClassMetadata().getClassName(), false);
        if (metaForThisClass == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        } else {
            List<SubMenuItem> subMenuItems = new ArrayList<>();
            for (LocalAttributeMetadata anAttribute : metaForThisClass.getAttributes())
                subMenuItems.add(new SubMenuItem(anAttribute.getName()));
            SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
        }
    }
        
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, I18N.gm("confirm_attribute_class_operation"), 
                    I18N.gm("class_metadata_operation"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
                if (com.deleteAttribute(classNode.getClassMetadata().getId(), ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getCaption())) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, I18N.gm("attribute_deleted"));
                    //Force a cache reload
                    Cache.getInstace().resetAll();
                    //Refresh the class node
                    classNode.refresh();
                }
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
