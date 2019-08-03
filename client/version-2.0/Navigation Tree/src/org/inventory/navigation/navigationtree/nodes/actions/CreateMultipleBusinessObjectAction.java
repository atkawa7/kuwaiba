/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.RootObjectNode;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action that requests multiple business objects creation
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public final class CreateMultipleBusinessObjectAction extends GenericObjectNodeAction 
    implements Presenter.Popup {
    
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    private static CreateMultipleBusinessObjectAction instance;
    
    private CreateMultipleBusinessObjectAction() {
        putValue(NAME, "New Multiple");
    }
    
    public static CreateMultipleBusinessObjectAction getInstance() {
        return instance == null ? instance = new CreateMultipleBusinessObjectAction() : instance;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField txtNamePattern = new JTextField();
        txtNamePattern.setName("txtNamePattern"); //NOI18N
        txtNamePattern.setColumns(20);
        
        JSpinner spinnerNumberOfObjects = new JSpinner();
        spinnerNumberOfObjects.setName("spinnerNumberOfObjects"); //NOI18N
        spinnerNumberOfObjects.setValue(1);
        
        JComplexDialogPanel saveDialog = new JComplexDialogPanel(
            new String[] {"Name Pattern", "Number of Objects"}, new JComponent[] {txtNamePattern, spinnerNumberOfObjects});
        
        if (JOptionPane.showConfirmDialog(null, saveDialog, 
                "New Multiple Objects of [" + ((JMenuItem) e.getSource()).getName() + "]", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String namePattern = ((JTextField)saveDialog.getComponent("txtNamePattern")).getText();
            int numberOfObjects = 0;
            Object spinnerValue= ((JSpinner)saveDialog.getComponent("spinnerNumberOfObjects")).getValue();
            if (spinnerValue instanceof Integer) {
                numberOfObjects = (Integer) spinnerValue;
                if (numberOfObjects <= 0) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, "The number of objects must be greater than 0");
                    return;
                }
            }
            
            AbstractNode node = Utilities.actionsGlobalContext().lookup(RootObjectNode.class);
            if (node == null)
                node = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
            
            List<LocalObjectLight> newObjects = com.createBulkObjects(
                ((JMenuItem) e.getSource()).getName(), 
                node instanceof RootObjectNode ? null : ((ObjectNode) node).getObject().getClassName(), 
                node instanceof RootObjectNode ? "-1" : ((ObjectNode) node).getObject().getId(), 
                numberOfObjects, 
                namePattern);
            
            if (newObjects == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                if (node.getChildren() instanceof AbstractChildren) //Some nodes are created on the fly and do not have children. For those cases, let's avoid refreshing their children lists
                    ((AbstractChildren) node.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "Elements created successfully");
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New Multiple");
        
        //Since this action is not only available for ObjectNodes, but also for RootObjectNode instances, we can't just use setEnable(isEnabled())
        //All object creation methods will behave the same way
        if (Utilities.actionsGlobalContext().lookupResult(AbstractNode.class).allInstances().size() > 1) {
            mnuPossibleChildren.setEnabled(false);
            return mnuPossibleChildren;
        }
        
        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
        List<LocalClassMetadataLight> items = com.getPossibleChildren(selectedObject.getClassName(), false);

        if (items == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE,
                com.getError());
            mnuPossibleChildren.setEnabled(false);
        }
        else {
            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }

            MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
        }
                
        return mnuPossibleChildren;
    }

    @Override
    public LocalValidator[] getValidators() {
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
