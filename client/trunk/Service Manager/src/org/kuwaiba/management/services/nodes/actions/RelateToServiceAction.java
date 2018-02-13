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
package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows the user relate the current object to a service as a resource
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToServiceAction extends GenericObjectNodeAction implements ComposedAction {
    
    public RelateToServiceAction() {
        putValue(NAME, I18N.gm("relate_to_service"));
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> services = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICSERVICE);

        if (services ==  null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (services.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no services created. Create at least one using the Service Manager", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame frame = new SelectValueFrame(
                    java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_TITLE_AVAILABLE_SERVICES"),
                    java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_INSTRUCTIONS_SELECT_SERVICE"),
                    "Create Relationship", services);
                frame.addListener(this);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, "Select a service from the list");
            else{
                List<String> classNames = new ArrayList<>();
                List<Long> objectIds = new ArrayList<>();
                for(LocalObjectLight selectedObject : selectedObjects){
                    classNames.add(selectedObject.getClassName());
                    objectIds.add(selectedObject.getOid());
                }
                
                if (CommunicationsStub.getInstance().associateObjectsToService(
                    classNames, objectIds, 
                    ((LocalObjectLight) selectedValue).getClassName(),
                    ((LocalObjectLight) selectedValue).getOid())){
                        JOptionPane.showMessageDialog(null, String.format(selectedObjects.size() > 1 ? 
                                "%s obejcts were related to service %s" : "%s object was related to service %s", selectedObjects.size(), selectedValue));
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
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
