/*
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.applicationnodes.classmetadatanodes.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.caching.Cache;
import org.inventory.core.services.utils.Constants;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.openide.util.Lookup;

/**
 *  Creates an attribute metadata
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CreateAttributeAction extends AbstractAction {
    
    private ClassMetadataNode classNode;

    public CreateAttributeAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_ATTRIBUTE"));
    }

    public CreateAttributeAction(ClassMetadataNode classNode) {
        this();
        this.classNode = classNode;
    }
   
    @Override
    public void actionPerformed(ActionEvent ae) {
        
        LocalClassMetadataLight[] instanceableListTypes = CommunicationsStub.getInstance().getInstanceableListTypes();
        
        ArrayList<String> attributeTypeslist = new ArrayList<String>();
        
        //Primitive types
        for(String primitive : Constants.ATTRIBUTE_TYPES)
            attributeTypeslist.add(primitive);
        
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
        
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                new String[]{java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME"), 
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DISPLAYNAME"), 
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION"), 
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_TYPE")},
                new JComponent []{txtName, txtDisplayName, txtDescription, lstType});
        if (JOptionPane.showConfirmDialog(null,
                pnlMyDialog,
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_POOL"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
                    if (CommunicationsStub.getInstance().createAttribute(classNode.getClassMetadata().getOid(), 
                            ((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                            ((JTextField)pnlMyDialog.getComponent("txtDisplayName")).getText(), 
                            ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), 
                            (String)((JComboBox)pnlMyDialog.getComponent("lstType")).getSelectedItem(), 
                            false, false, true, false, false)){
                        nu.showSimplePopup("Class metadata operation", NotificationUtil.INFO, "Attribute added successfully");
                        Cache.getInstace().resetAll();
                        classNode.refresh();
                    }
                    else
                        nu.showSimplePopup("Class metadata operation", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }
}
