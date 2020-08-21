/*
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
 */
package com.neotropic.inventory.modules.warehouses.nodes.actions;

import com.neotropic.inventory.modules.warehouses.nodes.WarehouseChildren;
import com.neotropic.inventory.modules.warehouses.nodes.WarehouseNode;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.MenuScroller;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Creates a pool to place spare parts of the same type.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewSparePoolAction extends GenericInventoryAction implements Presenter.Popup {
    private static NewSparePoolAction instance;
        
    private NewSparePoolAction() {
        putValue(NAME, "New Spare Pool");
    }
    
    public static NewSparePoolAction getInstance() {
        return instance == null ? instance = new NewSparePoolAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_WAREHOUSES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Iterator<? extends WarehouseNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(WarehouseNode.class).allInstances().iterator();
            
        if (!selectedNodes.hasNext())
            return;
        
        WarehouseNode customerNode = selectedNodes.next();
        
        JTextField txtName = new JTextField(), txtDescription =  new JTextField();
        txtName.setName("txtName"); //NOI18N
        txtName.setPreferredSize(new Dimension(120, 18));
        txtDescription.setName("txtDescription"); //NOI18N
        
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                new String[]{"Name", "Description"},
                new JComponent []{txtName, txtDescription});
        
        if (JOptionPane.showConfirmDialog(null, pnlMyDialog,
                "Create Spare Pool",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
            
            LocalObjectLight newPool = CommunicationsStub.getInstance().createPoolInObject(customerNode.getObject().getClassName(), 
                                            customerNode.getObject().getId(), 
                                            ((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                                            ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), 
                                            ((JMenuItem)e.getSource()).getName(), LocalPool.POOL_TYPE_MODULE_COMPONENT);
                    
            if (newPool ==  null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else {
                ((WarehouseChildren)customerNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "Spare Pool Created");
            }
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New Spare Pool");
        List<LocalClassMetadataLight> items = CommunicationsStub.getInstance().getLightSubclasses(Constants.CLASS_CONFIGURATIONITEM, true, true);

        if (items == null) {
            mnuPossibleChildren.setEnabled(false);
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else {
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }
                MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
            }
        }
		
        return mnuPossibleChildren;
    }
    
}
