/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import static javax.swing.Action.NAME;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action to create multiple template elements
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public final class CreateMultipleTemplateElementAction extends CreateTemplateElementAction implements Presenter.Popup {
    
    public CreateMultipleTemplateElementAction() {
        putValue(NAME, I18N.gm("template_element_create_multiple_action")); //NOI18N
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
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
            new String[] {I18N.gm("name_pattern"), I18N.gm("number_of_template_elements")}, new JComponent[] {txtNamePattern, spinnerNumberOfObjects});
        
        if (JOptionPane.showConfirmDialog(null, saveDialog, 
                I18N.gm("template_element_create_multiple_action") + " [" + ((JMenuItem) e.getSource()).getName() + "]", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String namePattern = ((JTextField)saveDialog.getComponent("txtNamePattern")).getText();
            int numberOfObjects = 0;
            Object spinnerValue= ((JSpinner)saveDialog.getComponent("spinnerNumberOfObjects")).getValue();
            if (spinnerValue instanceof Integer) {
                numberOfObjects = (Integer) spinnerValue;
                if (numberOfObjects <= 0) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, I18N.gm("number_greater_than_zero"));
                    return;
                }
            }
            TemplateElementNode selectedNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
            LocalObjectLight selectedObject = selectedNode.getLookup().lookup(LocalObjectLight.class);
            
            List<LocalObjectLight> templateElements = CommunicationsStub.getInstance().
                    createBulkTemplateElement(((JMenuItem)e.getSource()).getName(), selectedObject.getClassName(), selectedObject.getId(), numberOfObjects, namePattern);
                        
            if (templateElements == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                ((AbstractChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("template_element_create_multiple_success"));
            }
        }
    }        
}
