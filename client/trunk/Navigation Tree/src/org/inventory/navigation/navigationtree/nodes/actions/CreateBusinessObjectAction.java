/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>.
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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.rmi.CORBA.Util;
import javax.swing.AbstractAction;
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
        
        LocalAttributeMetadata[] mandatoryObjectAttributes = 
                com.getMandatoryObjectAttributes(((JMenuItem)ev.getSource()).getName());
        
        HashMap<String, Object> attributes =  new HashMap<>();
        
        if(mandatoryObjectAttributes.length>0){
            String[] labels = new String[mandatoryObjectAttributes.length];
            JComponent[] jComponents = new JComponent[mandatoryObjectAttributes.length];
            for (int i = 0; i < mandatoryObjectAttributes.length; i++) {
                if( mandatoryObjectAttributes[i].isMandatory()){
                    labels[i] = mandatoryObjectAttributes[i].getName();
                    if(mandatoryObjectAttributes[i].getMapping() == Constants.MAPPING_MANYTOONE){
                        List<LocalObjectListItem> list = com.getList(mandatoryObjectAttributes[i].getListAttributeClassName(), true, false);
                        if (list == null) {
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                                return;
                        }
                        LocalObjectListItem [] listType = list.toArray(new LocalObjectListItem[list.size()]);
                        JComboBox<LocalObjectListItem> lstType = new JComboBox<>(listType);
                        jComponents[i] = lstType;
                    }
                    else if(mandatoryObjectAttributes[i].getMapping() == Constants.MAPPING_DATE){
                        JDateChooser datePicker = new JDateChooser();
                        datePicker.setDate(new Date());
                        jComponents[i] = datePicker;
                    }
                    else{
                        if(mandatoryObjectAttributes[i].getType().equals(Boolean.class))
                            jComponents[i] = new JCheckBox();
                        else{    
                            final JTextField attributeField = new JTextField();
                            attributeField.setName(mandatoryObjectAttributes[i].getName());
                            
                            if(mandatoryObjectAttributes[i].getType().equals(Float.class) ||
                                    mandatoryObjectAttributes[i].getType().equals(Integer.class) ||
                                    mandatoryObjectAttributes[i].getType().equals(Long.class)){
                                //number filters
                                attributeField.addKeyListener(new KeyListener() {
                                    String key = "";
                                    @Override
                                    public void keyTyped(KeyEvent e) {
                                        key = new StringBuilder().append(e.getKeyChar()).toString();
                                        if(!isNumeric(key))
                                            attributeField.setBackground(Color.red);
                                        else
                                            attributeField.setBackground(Color.white);
                                    }

                                    @Override
                                    public void keyPressed(KeyEvent e) {
                                        key = new StringBuilder().append(e.getKeyChar()).toString();
                                        if(!isNumeric(key))
                                            attributeField.setBackground(Color.red);
                                        else
                                            attributeField.setBackground(Color.white);
                                    }

                                    @Override
                                    public void keyReleased(KeyEvent e) {
                                        key = new StringBuilder().append(e.getKeyChar()).toString();
                                        if(!isNumeric(key))
                                            attributeField.setBackground(Color.red);
                                        else
                                            attributeField.setBackground(Color.white);
                                    }
                                });
                            }
                            
                            //Empty filters
                            attributeField.addFocusListener(new FocusListener() {
                                @Override
                                public void focusGained(FocusEvent e) {
                                    attributeField.setBackground(Color.white);
                                }

                                @Override
                                public void focusLost(FocusEvent e) {
                                    if(((JTextField)e.getComponent()).getText().isEmpty())
                                        attributeField.setBackground(Color.red);
                                    else{
                                        attributeField.setBackground(Color.white);
                                    }
                                }
                            });
                            jComponents[i] = attributeField;
                        }
                    }
                }
            }

            JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(labels, jComponents);

            if (JOptionPane.showConfirmDialog(null,
                    pnlMyDialog,
                    "New Object",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){

                for (int i = 0; i < labels.length; i++) {
                    if (jComponents[i] instanceof JComboBox){
                        attributes.put(labels[i], String.valueOf(((LocalObjectListItem)((JComboBox)jComponents[i]).getSelectedItem()).getId()));
                    }
                    else {
                        if (jComponents[i]  instanceof JCheckBox)
                            attributes.put(labels[i], ((JCheckBox)jComponents[i]).isSelected());
                        else if (jComponents[i]  instanceof JDateChooser)
                            attributes.put(labels[i], ((JDateChooser)jComponents[i]).getDate());
                        else
                            attributes.put(labels[i], ((JTextField)jComponents[i]).getText());
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
    
    private static boolean isNumeric(String str){  
      try {  
        double d = Double.parseDouble(str);  
      } catch(NumberFormatException nfe) {  
        return false;  
      }  
      return true;  
    }
}