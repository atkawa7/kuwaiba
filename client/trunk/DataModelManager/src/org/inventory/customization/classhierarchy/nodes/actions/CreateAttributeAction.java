/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.classhierarchy.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;

/**
 *  Creates an attribute metadata
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class CreateAttributeAction extends GenericInventoryAction {
    
    private ClassMetadataNode classNode;

    public CreateAttributeAction() {
        putValue(NAME, I18N.gm("new_attribute"));
    }

    public CreateAttributeAction(ClassMetadataNode classNode) {
        this();
        this.classNode = classNode;
    }
   
    @Override
    public void actionPerformed(ActionEvent ae) {
        
        List<LocalClassMetadataLight> instanceableListTypes = CommunicationsStub.getInstance().getInstanceableListTypes();
        
        ArrayList<String> attributeTypeslist = new ArrayList<>();
        
        //Primitive types
        attributeTypeslist.addAll(Arrays.asList(Constants.ATTRIBUTE_TYPES));
        
        //List types
        for(LocalClassMetadataLight listType : instanceableListTypes)
            attributeTypeslist.add(listType.getClassName());
        
        JTextField txtName = new JTextField();
        txtName.setName("txtName");
        JTextField txtDisplayName = new JTextField();
        txtDisplayName.setName("txtDisplayName");
        JTextField txtDescription = new JTextField();
        txtDescription.setName("txtDescription");
        JComboBox lstType = new JComboBox(attributeTypeslist.toArray());
        lstType.setName("lstType");
                
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                new String[]{I18N.gm("name"), I18N.gm("display_name"), 
                    I18N.gm("description"), I18N.gm("type")},
                new JComponent []{txtName, txtDisplayName, txtDescription, lstType});
        if (JOptionPane.showConfirmDialog(null,
                pnlMyDialog,
                I18N.gm("new_attribute"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
                    if (CommunicationsStub.getInstance().createAttribute(classNode.getClassMetadata().getOid(), 
                            ((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                            ((JTextField)pnlMyDialog.getComponent("txtDisplayName")).getText(), 
                            ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), 
                            (String)((JComboBox)pnlMyDialog.getComponent("lstType")).getSelectedItem(), 
                            false, false, true, false, false, false)){
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, I18N.gm("attributed_added_successfully"));
                        Cache.getInstace().resetAll();
                        classNode.refresh();
                    }
                    else
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
