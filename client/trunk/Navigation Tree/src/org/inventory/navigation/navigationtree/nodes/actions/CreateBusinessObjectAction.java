/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.nodes.actions;

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
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.RootObjectNode;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.Presenter.Popup;

/**
 * Action that requests a business object creation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class CreateBusinessObjectAction extends AbstractAction implements Popup {
    
    private AbstractNode node;
    private CommunicationsStub com;
    

    public CreateBusinessObjectAction(ObjectNode node) {
        putValue(NAME, "New");
        com = CommunicationsStub.getInstance();
        this.node = node;
    }

    public CreateBusinessObjectAction(RootObjectNode node) {
        putValue(NAME, "New");
        com = CommunicationsStub.getInstance();
        this.node = node;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        final LocalAttributeMetadata[] mandatoryObjectAttributes = com.getMandatoryAttributesInClass(((JMenuItem)ev.getSource()).getName());
        HashMap<String, Object> attributes = new HashMap<>();
        if(mandatoryObjectAttributes.length > 0){
            attributes = createNewObjectForm(mandatoryObjectAttributes);
            if(!attributes.isEmpty()) //the createNewObject form is closed, and the ok button is never clicked 
                createObject(((JMenuItem)ev.getSource()).getName(), attributes);
        } 
        else
            createObject(((JMenuItem)ev.getSource()).getName(), attributes);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New");
        List<LocalClassMetadataLight> items;
        if (node instanceof RootObjectNode) //For the root node
            items = com.getPossibleChildren(Constants.DUMMYROOT, false);
        else
            items = com.getPossibleChildren(((ObjectNode)node).getObject().getClassName(), false);

        if (items == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE,
                com.getError());
            mnuPossibleChildren.setEnabled(false);
        }
        else {
            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }

            MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
        }
        return mnuPossibleChildren;
    }
        
    //helpers
    /**
     * Invokes the JOptionpane and also creates all the listeners for every field created
     * @param mandatoryObjectAttributes the object's mandatory attributes
     * @return the mandatory attributes with values, if the form is closed returns an empty HashMap
     */
    private HashMap<String, Object> createNewObjectForm(final LocalAttributeMetadata[] mandatoryObjectAttributes){
        final HashMap<String, Object> attributes =  new HashMap<>();
        final HashMap<String, Boolean> mandatoryAttrtsState =  new HashMap<>();
        
        for (LocalAttributeMetadata mandatoryObjectAttribute : mandatoryObjectAttributes){ 
            //date and boolean has state non empty since the begining
            if(mandatoryObjectAttribute.getType().equals(Boolean.class) || mandatoryObjectAttribute.getMapping() == Constants.MAPPING_DATE)
                mandatoryAttrtsState.put(mandatoryObjectAttribute.getName(), true);
            else
                mandatoryAttrtsState.put(mandatoryObjectAttribute.getName(), false);
        }
        
        if(mandatoryObjectAttributes.length > 0){
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
                            mandatoryAttrtsState.put(comboBox.getName(), ((LocalObjectListItem)e.getItem()).getId() != 0);
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
    private JComplexDialogPanel createFields(LocalAttributeMetadata[] mandatoryObjectAttributes){
        String[]  labels = new String[mandatoryObjectAttributes.length];
        JComponent[] jComponents = new JComponent[mandatoryObjectAttributes.length];
        JComplexDialogPanel pnlMyDialog;
        
        for (int i = 0; i < mandatoryObjectAttributes.length; i++) {
            labels[i] = mandatoryObjectAttributes[i].getName();
            switch (mandatoryObjectAttributes[i].getMapping()) {
                case Constants.MAPPING_MANYTOONE:
                    List<LocalObjectListItem> list = com.getList(mandatoryObjectAttributes[i].getListAttributeClassName(), true, false);
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
                    if (mandatoryObjectAttributes[i].getType().equals(Boolean.class)){ //boolean fields
                        JCheckBox checkBox = new JCheckBox();
                        checkBox.setName(labels[i]);
                        jComponents[i] = checkBox;
                    }
                    else {
                        final JTextField attributeField = new JTextField();
                        attributeField.setName(mandatoryObjectAttributes[i].getName());

                        if (mandatoryObjectAttributes[i].getType().equals(Float.class)
                                || mandatoryObjectAttributes[i].getType().equals(Integer.class)
                                || mandatoryObjectAttributes[i].getType().equals(Long.class)) {
                            labels[i] = mandatoryObjectAttributes[i].getName() + "#";
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
          double d = Double.parseDouble(str);  
        }catch(NumberFormatException nfe) {  
          return false;  
        }  
        return true;  
    }

    /**
     * Call the communication stub and create the object 
     * @param objectClass the object's class
     * @param attributes the attribute list of the object
     */
    private void createObject(String objectClass, HashMap<String, Object> attributes) {
        LocalObjectLight myLol = com.createObject(
                        objectClass,
                        node instanceof RootObjectNode ? null : ((ObjectNode)node).getObject().getClassName(),
                        node instanceof RootObjectNode? -1 : ((ObjectNode)node).getObject().getOid(), attributes, -1);

        if (myLol == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (node.getChildren() instanceof AbstractChildren) //Some nodes are created on the fly and does not have children. For those cases, let's avoid refreshing their children lists
                ((AbstractChildren)node.getChildren()).addNotify();

            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Element created successfully");
        }
    }
}