/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.nodes.actions;

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
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.AttributesForm;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.RootObjectNode;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action that requests a business object creation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class CreateBusinessObjectAction extends GenericObjectNodeAction implements Presenter.Popup {
    private static CreateBusinessObjectAction instance;
    private AbstractNode node;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    private CreateBusinessObjectAction() {
        putValue(NAME, "New");
    }
    
    public static CreateBusinessObjectAction getInstance(AbstractNode node) {
        if (instance == null)
            instance = new CreateBusinessObjectAction();
        instance.setNode(node);
        return instance;                    
    }
    
    public void setNode(AbstractNode node) {
        this.node = node;        
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        String objectClass = ((JMenuItem) ev.getSource()).getName();
            
        final LocalAttributeMetadata[] mandatoryObjectAttributes = com.getMandatoryAttributesInClass(objectClass);
        HashMap<String, Object> attributes = new HashMap<>();
        if(mandatoryObjectAttributes.length > 0){
            AttributesForm mandatoryAttributeForm = new AttributesForm(mandatoryObjectAttributes);
            attributes = mandatoryAttributeForm.createNewObjectForm();
            if(!attributes.isEmpty()) //the createNewObject form is closed, but the ok button is never clicked 
                createObject(objectClass, attributes);
        } 
        else
            createObject(objectClass, attributes);
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New");
        
        //Since this action is not only available for ObjectNodes, but also for RootObjectNode instances, we can't just use setEnable(isEnabled())
        //All object creation methods will behave the same way
        if (Utilities.actionsGlobalContext().lookupResult(AbstractNode.class).allInstances().size() > 1) {
            mnuPossibleChildren.setEnabled(false);
            return mnuPossibleChildren;
        }
        
        List<LocalClassMetadataLight> items;
        if (node instanceof RootObjectNode) //For the root node
            items = com.getPossibleChildren(Constants.DUMMYROOT, false);
        else
            items = com.getPossibleChildren(((ObjectNode)node).getObject().getClassName(), false);

        if (items == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE,
                com.getError());
            mnuPossibleChildren.setEnabled(false);
        }
        else {
            if (items.isEmpty()) 
                mnuPossibleChildren.setEnabled(false);
            else {
                for (LocalClassMetadataLight item : items) {
                    JMenuItem mnuiChildren = new JMenuItem(item.getClassName());
                    mnuiChildren.setName(item.getClassName());
                    mnuiChildren.addActionListener(this);
                    mnuPossibleChildren.add(mnuiChildren);
                }
            }
            MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
        }

        return mnuPossibleChildren;
    }
    
    /**
     * Call the communication stub and create the object 
     * @param objectClass the object's class
     * @param attributes the attribute list of the object
     */
    private void createObject(String objectClass, HashMap<String, Object> attributes) {
        LocalObjectLight myLol = com.createObject(
                        objectClass,
                        node instanceof RootObjectNode ? null : ((ObjectNode)node).getObject().getClassName(),
                        node instanceof RootObjectNode? -1 : ((ObjectNode)node).getObject().getOid(), attributes, -1);

        if (myLol == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (node.getChildren() instanceof AbstractChildren) //Some nodes are created on the fly and does not have children. For those cases, let's avoid refreshing their children lists
                ((AbstractChildren)node.getChildren()).addNotify();

            NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "Element created successfully");
        }
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
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