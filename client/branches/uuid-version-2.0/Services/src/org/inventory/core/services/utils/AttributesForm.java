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
package org.inventory.core.services.utils;

import com.toedter.calendar.JDateChooser;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Helps to create a form with a list of a given attributes, it is useful for 
 * creating objects with mandatory attributes.
 * @author Adrian Martinez <adrian.martinez@neotropic.co>
 */
public class AttributesForm {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    final List<LocalAttributeMetadata> mandatoryObjectAttributes;

    public AttributesForm(List<LocalAttributeMetadata> mandatoryObjectAttributes) {
        this.mandatoryObjectAttributes = mandatoryObjectAttributes;
    }
    
    /**
     * Invokes the JOptionpane and also creates all the listeners for every field created
     * @return the mandatory attributes with values, if the form is closed returns an empty HashMap
     */
    public HashMap<String, Object> createNewObjectForm(){
        final HashMap<String, Object> attributes =  new HashMap<>();
        final HashMap<String, Boolean> mandatoryAttrtsState =  new HashMap<>();
        
        for (LocalAttributeMetadata mandatoryObjectAttribute : mandatoryObjectAttributes){ 
            //date and boolean has state non empty since the begining
            if(mandatoryObjectAttribute.getType().equals(Boolean.class) || mandatoryObjectAttribute.getMapping() == Constants.MAPPING_DATE)
                mandatoryAttrtsState.put(mandatoryObjectAttribute.getName(), true);
            else
                mandatoryAttrtsState.put(mandatoryObjectAttribute.getName(), false);
        }
        
        if(!mandatoryObjectAttributes.isEmpty()){
            final JButton ok = new JButton("OK");
            ok.setEnabled(false);
            final JComplexDialogPanel pnlMyDialog = createFields(mandatoryObjectAttributes);
            
            ok.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {   //Get the values from the form and asign values for every attribute
                    for (LocalAttributeMetadata mandatoryObjectAttribute : mandatoryObjectAttributes) {
                        if (pnlMyDialog.getComponent(mandatoryObjectAttribute.getName()) instanceof JComboBox)
                            attributes.put(mandatoryObjectAttribute.getName(), String.valueOf(((LocalObjectListItem)((JComboBox)pnlMyDialog.getComponent(mandatoryObjectAttribute.getName())).getSelectedItem()).getId()));
                        else {
                                if (pnlMyDialog.getComponent(mandatoryObjectAttribute.getName())  instanceof JCheckBox)
                                    attributes.put(mandatoryObjectAttribute.getName(), ((JCheckBox)pnlMyDialog.getComponent(mandatoryObjectAttribute.getName())).isSelected());
                                else if (pnlMyDialog.getComponent(mandatoryObjectAttribute.getName()) instanceof JDateChooser)
                                    attributes.put(mandatoryObjectAttribute.getName(), ((JDateChooser)pnlMyDialog.getComponent(mandatoryObjectAttribute.getName())).getDate());
                                else
                                    attributes.put(mandatoryObjectAttribute.getName(), ((JTextField)pnlMyDialog.getComponent(mandatoryObjectAttribute.getName())).getText());
                            }
                    }
                    //close the dialog
                    Window w = SwingUtilities.getWindowAncestor(ok);
                    if(w != null) w.setVisible(false);
                }
            });
            //creates a listener for every type of mandatory attribute in the form to check if they are not empty
            for (LocalAttributeMetadata mandatoryObjectAttribute : mandatoryObjectAttributes){
                JComponent component = pnlMyDialog.getComponent(mandatoryObjectAttribute.getName());
                if(component instanceof JTextField){
                    final JTextField field = (JTextField)component;
                    //create listeners for numeric fields
                    if(mandatoryObjectAttribute.getType().equals(Float.class) ||
                                    mandatoryObjectAttribute.getType().equals(Integer.class) ||
                                    mandatoryObjectAttribute.getType().equals(Long.class)){
                        field.addKeyListener(new KeyListener() {
                            protected void update() {
                                boolean canSave = false;
                                mandatoryAttrtsState.put(field.getName(), isNumeric(field.getText()));
                                for (String name : mandatoryAttrtsState.keySet()){
                                    if(!mandatoryAttrtsState.get(name)){
                                        canSave = false;
                                        break;
                                    }
                                    else
                                        canSave = true;
                                }
                                ok.setEnabled(canSave);
                            }
                            String key = "";
                            @Override
                            public void keyTyped(KeyEvent e) {
                                update();
                            }

                            @Override
                            public void keyPressed(KeyEvent e) {
                                update();
                            }

                            @Override
                            public void keyReleased(KeyEvent e) {
                                update();
                            }
                        });
                    }
                    //listeners for text fields
                    field.getDocument().addDocumentListener(new DocumentListener() {
                        protected void update() {
                            boolean canSave = false;
                            mandatoryAttrtsState.put(field.getName(), field.getText().length() > 0);
                            for (String name : mandatoryAttrtsState.keySet()){
                                if(!mandatoryAttrtsState.get(name)){
                                    canSave = false;
                                    break;
                                }
                                else
                                    canSave = true;
                            }
                            ok.setEnabled(canSave);
                        }
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            update();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            update();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            update();
                        }
                    });
                }
                //create listeners for list fields
                else if(component instanceof JComboBox){
                    final JComboBox comboBox = (JComboBox)component;
                    comboBox.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            boolean canSave = false;
                            mandatoryAttrtsState.put(comboBox.getName(), ((LocalObjectListItem)e.getItem()).getId() != null);
                            for (String name : mandatoryAttrtsState.keySet()){
                                if(!mandatoryAttrtsState.get(name)){
                                    canSave = false;
                                    break;
                                }
                                else
                                    canSave = true;
                            }
                            ok.setEnabled(canSave);
                        }
                    });
                }
            }//end for
            JOptionPane.showOptionDialog(null, pnlMyDialog, 
                    "Fill the Mandatory Attributes for the New Object", 
                    JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, 
                    new JButton[]{ok}, ok);
        }
        return attributes;
    }
    
    /**
     * creates a field for every type of attribute
     * @param mandatoryObjectAttributes the object's mandatory attributes
     * @return the complex panel with all the mandatory fields
     */
    private JComplexDialogPanel createFields(List<LocalAttributeMetadata> mandatoryObjectAttributes){
        String[]  labels = new String[mandatoryObjectAttributes.size()];
        JComponent[] jComponents = new JComponent[mandatoryObjectAttributes.size()];
        JComplexDialogPanel pnlMyDialog;
        
        for (int i = 0; i < mandatoryObjectAttributes.size(); i++) {
            labels[i] = mandatoryObjectAttributes.get(i).getName();
            switch (mandatoryObjectAttributes.get(i).getMapping()) {
                case Constants.MAPPING_MANYTOONE:
                    List<LocalObjectListItem> list = com.getList(mandatoryObjectAttributes.get(i).getListAttributeClassName(), true, false);
                    if (list == null) {
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                        return null;
                    }
                    LocalObjectListItem[] listType = list.toArray(new LocalObjectListItem[list.size()]);
                    JComboBox<LocalObjectListItem> lstType = new JComboBox<>(listType);
                    lstType.setName(labels[i]);
                    jComponents[i] = lstType;
                    break;
                case Constants.MAPPING_DATE:
                    JDateChooser datePicker = new JDateChooser();
                    datePicker.setDate(new Date());
                    datePicker.setName(labels[i]);
                    jComponents[i] = datePicker;
                    break;
                case Constants.MAPPING_PRIMITIVE:
                    if (mandatoryObjectAttributes.get(i).getType().equals(Boolean.class)){ //boolean fields
                        JCheckBox checkBox = new JCheckBox();
                        checkBox.setName(labels[i]);
                        jComponents[i] = checkBox;
                    }
                    else {
                        final JTextField attributeField = new JTextField();
                        attributeField.setName(mandatoryObjectAttributes.get(i).getName());

                        if (mandatoryObjectAttributes.get(i).getType().equals(Float.class)
                                || mandatoryObjectAttributes.get(i).getType().equals(Integer.class)
                                || mandatoryObjectAttributes.get(i).getType().equals(Long.class)) {
                            labels[i] = mandatoryObjectAttributes.get(i).getName() + "#";
                        }
                        jComponents[i] = attributeField;
                    }
                    break;
            }//end switch
        }//end for
        pnlMyDialog = new JComplexDialogPanel(labels, jComponents);
        return pnlMyDialog;
    }
    
    private boolean isNumeric(String str){  
        try {  
          Double.parseDouble(str);  
          return true; 
        }catch(NumberFormatException nfe) {  
          return false;  
        }  
    }
}
