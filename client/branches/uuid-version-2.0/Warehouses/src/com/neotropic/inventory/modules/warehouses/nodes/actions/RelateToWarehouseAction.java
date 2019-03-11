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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows the user relate the current object to a Warehouse as a resource
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToWarehouseAction extends GenericObjectNodeAction implements ComposedAction {
    
    public RelateToWarehouseAction() {
        putValue(NAME, I18N.gm("relate_to_warehouse"));
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> warehouse = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_WAREHOUSE);

        if (warehouse ==  null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (warehouse.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no warehouses created. Create at least one using the Warehouses Manager", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame frame = new SelectValueFrame(
                    I18N.gm("lbl_title_available_warehouses"),
                    I18N.gm("lbl_instructions_select_warehouse"),
                    "Create Relationship", warehouse);
                frame.addListener(this);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_WAREHOUSES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, "Select a warehouse from the list");
            else{
                List<String> classNames = new ArrayList<>();
                List<String> objectIds = new ArrayList<>();
                for(LocalObjectLight selectedObject : selectedObjects){
                    classNames.add(selectedObject.getClassName());
                    objectIds.add(selectedObject.getId());
                }
                
                if (CommunicationsStub.getInstance().associatesPhysicalNodeToWarehouse(
                    classNames, objectIds, 
                    ((LocalObjectLight) selectedValue).getClassName(),
                    ((LocalObjectLight) selectedValue).getId())){
                        JOptionPane.showMessageDialog(null, String.format(selectedObjects.size() > 1 ? 
                                "%s obejcts were related to warehouse %s" : "%s object was related to warehouse %s", selectedObjects.size(), selectedValue));
                        frame.dispose();
                }
                else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String[] appliesTo() {
        return new String [] {"GenericPhysicalNode"};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
