/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.inventory.modules.cpe.nodes.actions;

import java.awt.event.ActionEvent;
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
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToEvlanAction extends GenericObjectNodeAction implements ComposedAction {
    
    public RelateToEvlanAction() {
        putValue(NAME, I18N.gm("modules.cpe.nodes.actions.RelateToEvlanAction.name"));
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
        return -1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> evlans = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_EVLAN);

        if (evlans ==  null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        else {
            if (evlans.isEmpty()) {
                JOptionPane.showMessageDialog(null, I18N.gm("modules.cpe.nodes.actions.RelateToEvlanAction.message"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame frame = new SelectValueFrame(I18N.gm("modules.cpe.nodes.actions.RelateToEvlanAction.available"), I18N.gm("modules.cpe.nodes.actions.RelateToEvlanAction.select"), I18N.gm("modules.cpe.nodes.actions.RelateToEvlanAction.create"), evlans);
                frame.addListener(this);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, I18N.gm("modules.cpe.nodes.actions.RelateToEvlanAction.select"));
            else {
                String [] objectsClassName = new String[selectedObjects.size()];
                String [] objectsId = new String[selectedObjects.size()];
                
                for (int i = 0; i < selectedObjects.size(); i += 1) {
                    objectsClassName[i] = selectedObjects.get(i).getClassName();
                    objectsId[i] = selectedObjects.get(i).getId();
                }
                if (CommunicationsStub.getInstance().associateObjectsToEvlan(
                    objectsClassName, objectsId, 
                    ((LocalObjectLight) selectedValue).getClassName(), 
                    ((LocalObjectLight) selectedValue).getId())) {
                    
                    JOptionPane.showMessageDialog(null, String.format(I18N.gm("selected_devices_were_related_to"), selectedValue));
                    frame.dispose();
                } else
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}
