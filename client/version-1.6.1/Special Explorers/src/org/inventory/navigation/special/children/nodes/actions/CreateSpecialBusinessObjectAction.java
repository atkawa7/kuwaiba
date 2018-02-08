/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 */
package org.inventory.navigation.special.children.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.AttributesForm;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Creates a new special object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class CreateSpecialBusinessObjectAction extends GenericObjectNodeAction 
            implements Presenter.Popup {
    private CommunicationsStub com;
    private static CreateSpecialBusinessObjectAction instance;
    
    public CreateSpecialBusinessObjectAction() {
        putValue(NAME, "New Special");
        com = CommunicationsStub.getInstance();
    }
    
    public static CreateSpecialBusinessObjectAction getInstance() {
        return instance == null ? instance = new CreateSpecialBusinessObjectAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        ObjectNode node = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        String className = ((JMenuItem)ev.getSource()).getName();
        final LocalAttributeMetadata[] mandatoryObjectAttributes = com.getMandatoryAttributesInClass(className);
        AttributesForm mandatoryAttributesForm = new AttributesForm(mandatoryObjectAttributes);
        HashMap<String, Object> attributes = new HashMap<>();
        if(mandatoryObjectAttributes.length > 0){
            attributes = mandatoryAttributesForm.createNewObjectForm();
            if(!attributes.isEmpty()) //the createNewObject form is closed, and the ok button is never clicked 
                createSpecialObject(className, node, attributes);
        } 
        else
            createSpecialObject(className, node, attributes);

    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New Special");
        ObjectNode node = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        if (node != null) {
            List<LocalClassMetadataLight> items = com.getPossibleSpecialChildren(node.getObject().getClassName(), false);

            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items) {
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }

            MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
        } else
            mnuPossibleChildren.setEnabled(false);
        
        return mnuPossibleChildren;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SPECIAL_EXPLORERS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
        
    private void createSpecialObject(String objectClass, ObjectNode node, HashMap<String, Object> attributes) {
        LocalObjectLight myLol = com.createSpecialObject(
                objectClass, node.getObject().getClassName(), 
                node.getObject().getOid(), attributes, -1);
        if (myLol == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            ((AbstractChildren)node.getChildren()).addNotify();
                
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "The special object was created successfully");
        }
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}