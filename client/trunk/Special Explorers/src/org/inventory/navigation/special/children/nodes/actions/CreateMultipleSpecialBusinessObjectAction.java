/**
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
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.navigation.special.children.nodes.SpecialObjectNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action that requests multiple business special objects creation
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateMultipleSpecialBusinessObjectAction extends GenericObjectNodeAction 
    implements Presenter.Popup {
    private CommunicationsStub com;
    private static CreateMultipleSpecialBusinessObjectAction instance;
    
    private CreateMultipleSpecialBusinessObjectAction() {
        putValue(NAME, "New Special (Multiple)");
        com = CommunicationsStub.getInstance();
    }
        
    public static CreateMultipleSpecialBusinessObjectAction getInstance() {
        return instance == null ? instance = new CreateMultipleSpecialBusinessObjectAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SPECIAL_EXPLORERS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SpecialObjectNode node = Utilities.actionsGlobalContext().lookup(SpecialObjectNode.class);
        
        JTextField txtNamePattern = new JTextField();
        txtNamePattern.setName("txtNamePattern"); //NOI18N
        txtNamePattern.setColumns(10);
        
        JSpinner spinnerNumberOfObjects = new JSpinner();
        spinnerNumberOfObjects.setName("spinnerNumberOfObjects"); //NOI18N
        spinnerNumberOfObjects.setValue(0);
        
        JComplexDialogPanel saveDialog = new JComplexDialogPanel(
            new String[] {"Name Pattern", "Number of Special Objects"}, new JComponent[] {txtNamePattern, spinnerNumberOfObjects});
        
        if (JOptionPane.showConfirmDialog(null, saveDialog, "New Special (Multiple)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String namePattern = ((JTextField)saveDialog.getComponent("txtNamePattern")).getText();
            int numberOfSpecialObjects = 0;
            Object spinnerValue= ((JSpinner)saveDialog.getComponent("spinnerNumberOfObjects")).getValue();
            if (spinnerValue instanceof Integer) {
                numberOfSpecialObjects = (Integer) spinnerValue;
                if (numberOfSpecialObjects <= 0) {
                    NotificationUtil.getInstance().showSimplePopup("Error", 
                        NotificationUtil.ERROR_MESSAGE, "The number of special objects must be greater than 0");
                    return;
                }
            }
            
            List<LocalObjectLight> newSpecialObjects = com.createBulkSpecialObjects(((JMenuItem)e.getSource()).getName(), node.getObject().getClassName(), node.getObject().getOid(), numberOfSpecialObjects, namePattern);
                
            if (newSpecialObjects == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((AbstractChildren)node.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "The bulk of special objects was created successfully");
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New Special (Multiple)");
        SpecialObjectNode node = Utilities.actionsGlobalContext().lookup(SpecialObjectNode.class);
        
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
        return mnuPossibleChildren;
    }

    @Override
    public String getValidator() {
        return null;
    }
}
