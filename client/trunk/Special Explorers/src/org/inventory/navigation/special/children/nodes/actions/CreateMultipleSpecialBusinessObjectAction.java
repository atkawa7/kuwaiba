/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action that requests multiple business special objects creation
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateMultipleSpecialBusinessObjectAction extends GenericObjectNodeAction 
    implements Presenter.Popup {
    private CommunicationsStub com;
    private static CreateMultipleSpecialBusinessObjectAction instance;
    
    private CreateMultipleSpecialBusinessObjectAction() {
        putValue(NAME, "New Special Multiple");
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
        ObjectNode node = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        
        JTextField txtNamePattern = new JTextField();
        txtNamePattern.setName("txtNamePattern"); //NOI18N
        txtNamePattern.setColumns(20);
        
        JSpinner spinnerNumberOfObjects = new JSpinner();
        spinnerNumberOfObjects.setName("spinnerNumberOfObjects"); //NOI18N
        spinnerNumberOfObjects.setValue(1);
        
        JComplexDialogPanel saveDialog = new JComplexDialogPanel(
            new String[] {"Name Pattern", "Number of Special Objects"}, new JComponent[] {txtNamePattern, spinnerNumberOfObjects});
        
        if (JOptionPane.showConfirmDialog(null, saveDialog, "New Special Multiple", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String namePattern = ((JTextField)saveDialog.getComponent("txtNamePattern")).getText();
            int numberOfSpecialObjects = 0;
            Object spinnerValue= ((JSpinner)saveDialog.getComponent("spinnerNumberOfObjects")).getValue();
            if (spinnerValue instanceof Integer) {
                numberOfSpecialObjects = (Integer) spinnerValue;
                if (numberOfSpecialObjects <= 0) {
                    JOptionPane.showMessageDialog(null, "The number of objects to create must be greater than 0", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            List<LocalObjectLight> newSpecialObjects = com.createBulkSpecialObjects(((JMenuItem)e.getSource()).getName(), node.getObject().getClassName(), node.getObject().getId(), numberOfSpecialObjects, namePattern);
                
            if (newSpecialObjects == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((AbstractChildren)node.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "Special objects created successfully");
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (!isEnabled())
            return null;
        JMenu mnuPossibleChildren = new JMenu("New Special Multiple");
        AbstractNode node = Utilities.actionsGlobalContext().lookup(AbstractNode.class);
        if (node != null) {
            List<LocalClassMetadataLight> items = com.getPossibleSpecialChildren(node.getLookup().lookup(LocalObjectLight.class).getClassName(), 
                    false);

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
