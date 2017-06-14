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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        
        LocalAttributeMetadata[] mandatoryObjectAttributes = com.getMandatoryObjectAttributes(((JMenuItem)ev.getSource()).getName());
        HashMap<String, Object> attributes =  new HashMap<>();
        
        if(mandatoryObjectAttributes.length > 0){
            JComplexDialogPanel pnlMyDialog = createFields(mandatoryObjectAttributes);
            
            if(JOptionPane.showConfirmDialog(null,
                pnlMyDialog,
                "New Object",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
                    
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
                    }
        }
        LocalObjectLight myLol = com.createObject(
                    ((JMenuItem)ev.getSource()).getName(),
                    node instanceof RootObjectNode ? null : ((ObjectNode)node).getObject().getClassName(),
                    node instanceof RootObjectNode? -1 : ((ObjectNode)node).getObject().getOid(), attributes, 0);

        if (myLol == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (node.getChildren() instanceof AbstractChildren) //Some nodes are 
                //created on the fly and does not have children. For those cases, 
                //let's avoid refreshing their children lists
                ((AbstractChildren)node.getChildren()).addNotify();

            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Element created successfully");
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu("New");
        List<LocalClassMetadataLight> items;
        if (node instanceof RootObjectNode) //For the root node
            items = com.getPossibleChildren(Constants.DUMMYROOT, false);
        else
            items = com.getPossibleChildren(((ObjectNode)node).getObject().getClassName(),false);

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
    
    private JComplexDialogPanel createFields(LocalAttributeMetadata[] mandatoryObjectAttributes) {
        String[]  labels = new String[mandatoryObjectAttributes.length];
        JComponent[] jComponents = new JComponent[mandatoryObjectAttributes.length];
        JComplexDialogPanel pnlMyDialog;
        
        for (int i = 0; i < mandatoryObjectAttributes.length; i++) {
            if (mandatoryObjectAttributes[i].isMandatory()) {
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
        }
        pnlMyDialog = new JComplexDialogPanel(labels, jComponents);
        System.out.println("aaa");
        return pnlMyDialog;
    }
    
    private class createObjectForm extends JOptionPane{

        HashMap<String, Object> attributes =  new HashMap<>();
        
        protected JOptionPane getOptionPane(JComponent parent) {
            JOptionPane pane = null;
            if (!(parent instanceof JOptionPane))
                pane = getOptionPane((JComponent)parent.getParent());
            else 
                pane = (JOptionPane) parent;
            
            return pane;
        }
    


        public createObjectForm() {
                final JButton okay = new JButton("Ok");
                okay.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
                        pane.setValue(okay);
                    }
                });
                okay.setEnabled(false);
                final JButton cancel = new JButton("Cancel");
                cancel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
                        pane.setValue(cancel);
                    }
                });

                
                
//                for (int i=0; i < jComponents.length; i++) {
//                    if(jComponents[i] instanceof JTextField){
//                        final JTextField field = ((JTextField)jComponents[i]);
//                        if(mandatoryObjectAttributes[i].getType().equals(Float.class) ||
//                                        mandatoryObjectAttributes[i].getType().equals(Integer.class) ||
//                                        mandatoryObjectAttributes[i].getType().equals(Long.class)){
//                            field.addKeyListener(new KeyListener() {
//                            protected void update() {
//                                okay.setEnabled(isNumeric(field.getText()));
//                            }
//                                String key = "";
//                                @Override
//                                public void keyTyped(KeyEvent e) {
//                                    update();
//                                }
//
//                                @Override
//                                public void keyPressed(KeyEvent e) {
//                                    update();
//                                }
//
//                                @Override
//                                public void keyReleased(KeyEvent e) {
//                                    update();
//                                }
//                            });
//                        }
//                        
//                        field.getDocument().addDocumentListener(new DocumentListener() {
//                        protected void update() {
//                            okay.setEnabled(field.getText().length() > 0);
//                        }
//                            @Override
//                            public void insertUpdate(DocumentEvent e) {
//                                update();
//                            }
//
//                            @Override
//                            public void removeUpdate(DocumentEvent e) {
//                                update();
//                            }
//
//                            @Override
//                            public void changedUpdate(DocumentEvent e) {
//                                update();
//                            }
//                        });
//                    }
//                    else if(jComponents[i] instanceof JComboBox){
//                        final JComboBox comboBox = (JComboBox)jComponents[i];
//                        comboBox.addItemListener(new ItemListener() {
//
//                            @Override
//                            public void itemStateChanged(ItemEvent e) {
//                                okay.setEnabled(((LocalObjectListItem)e.getItem()).getId() != 0);
//                            }
//                        });
//                    }
//                }

                
                
            
        }
        
        public HashMap<String, Object> getAttributes(){
            return attributes;
        }
        
        private boolean isNumeric(String str){  
            try {  
              double d = Double.parseDouble(str);  
            }catch(NumberFormatException nfe) {  
              return false;  
            }  
            return true;  
        }
    }//end private class
    
}