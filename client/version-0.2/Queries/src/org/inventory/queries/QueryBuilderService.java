/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.util.Lookup;

/**
 * Implements the business logic for the related TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class QueryBuilderService implements ListSelectionListener,ItemListener{

    private QueryBuilderFrame qtf;
    private CommunicationsStub com;
    private NotificationUtil nu;
    private LocalClassMetadata currentLocalClassMetadata = null;
    private List<JCheckBox> enablers;

    QueryBuilderService(QueryBuilderFrame _qtf) {
        this.qtf = _qtf;
        com = CommunicationsStub.getInstance();
    }

    public void initComponents(){
        LocalClassMetadataLight[] lcml = com.getAllLightMeta();

        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        if (lcml == null)
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/queries/Bundle").getString("LBL_TITLE_CREATION"),
                    NotificationUtil.ERROR, com.getError());
        else
            qtf.getList().setListData(lcml);
        qtf.getList().addListSelectionListener(this);
    }

    public void valueChanged(ListSelectionEvent e) {
        LocalClassMetadataLight item = (LocalClassMetadataLight)((JList)e.getSource()).getSelectedValue();
        this.currentLocalClassMetadata = com.getMetaForClass(item.getClassName(),false);

        if (this.enablers == null)
            this.enablers = new ArrayList<JCheckBox>();
        else
            this.enablers.removeAll(enablers);

        //Clean up the panel
        qtf.getLeftPanel().removeAll();

        //For more information on how to use GroupLayouts refer to http://java.sun.com/javase/6/docs/api/javax/swing/GroupLayout.html
        SequentialGroup hGroup = ((GroupLayout)qtf.getLeftPanel().getLayout()).createSequentialGroup();
        SequentialGroup vGroup = ((GroupLayout)qtf.getLeftPanel().getLayout()).createSequentialGroup();
        ((GroupLayout)qtf.getLeftPanel().getLayout()).setAutoCreateGaps(true);
        ((GroupLayout)qtf.getLeftPanel().getLayout()).setAutoCreateContainerGaps(true);

        ParallelGroup labels = ((GroupLayout)qtf.getLeftPanel().getLayout()).createParallelGroup();
        ParallelGroup values = ((GroupLayout)qtf.getLeftPanel().getLayout()).createParallelGroup();
        ParallelGroup checkboxes = ((GroupLayout)qtf.getLeftPanel().getLayout()).createParallelGroup();

        for (LocalAttributeMetadata lam : currentLocalClassMetadata.getAttributes()){
            if (lam.getIsVisible()){
                JLabel lblAttribute = new JLabel(lam.getDisplayName());

                if(lam.getIsAdministrative())
                    lblAttribute.setForeground(Color.red);

                JComponent component=null;
                if (lam.getType().equals(Boolean.class)){
                    JCheckBox chkValue = new JCheckBox();
                    component = chkValue;
                }
                else{
                    if(lam.getIsMultiple()){
                        LocalObjectListItem[] list = com.getList(lam.getListAttributeClassName(),false);

                        JComboBox cmbValue = new JComboBox(list);

                        //Set the null vale by default
                        cmbValue.setSelectedIndex(0);
                        cmbValue.setMaximumSize(new Dimension (400,10));
                        component = cmbValue;
                    }
                    else{
                         //The second constructor that uses the max lenghtSi se usa el segundo constructor del textfield, especificado el número de caracteres, la vaina se totea, y empieza  acolocar el texfield centrado y el panel queda inmenso
                        JTextField txtValue = new JTextField();
                        txtValue.setMaximumSize(new Dimension (400,10));
                        component = txtValue;
                    }
                }

                component.setToolTipText(lam.getDescription());
                
                component.setName(lam.getName());
                component.setEnabled(false);

                JCheckBox newCheckBox = new JCheckBox();

                //This way will be possible to retrieve only the enabled parameters
                newCheckBox.putClientProperty("component", component);
                newCheckBox.addItemListener(this);

                labels.addComponent(lblAttribute);
                values.addComponent(component);
                checkboxes.addComponent(newCheckBox);
                enablers.add(newCheckBox);
                
                vGroup.addGroup(((GroupLayout)qtf.getLeftPanel().getLayout()).createParallelGroup(Alignment.BASELINE).
                        addComponent(newCheckBox).addComponent(lblAttribute).addComponent(component));
            }
        }
        
        hGroup.addGroup(checkboxes);
        hGroup.addGroup(labels);
        hGroup.addGroup(values);
        //Vertical *AND* Horizontal groups must be created, or an IllegalStateException will be raised
        ((GroupLayout)qtf.getLeftPanel().getLayout()).setHorizontalGroup(hGroup);
        ((GroupLayout)qtf.getLeftPanel().getLayout()).setVerticalGroup(vGroup);
        qtf.getLeftPanel().revalidate();
        qtf.getLeftPanel().repaint();
    }

    public void itemStateChanged(ItemEvent e) {
        if(((JCheckBox)e.getSource()).isSelected())
            ((JComponent)((JCheckBox)e.getSource()).getClientProperty("component")).setEnabled(true);
        else
            ((JComponent)((JCheckBox)e.getSource()).getClientProperty("component")).setEnabled(false);
    }

    public void search() {
        if (enablers == null){
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/queries/Bundle").getString("LBL_QUERY_RESULT"), NotificationUtil.WARNING, java.util.ResourceBundle.getBundle("org/inventory/queries/Bundle").getString("LBL_QUERY_RESULT_TEXT"));
            return;
        }

        List<String> types,atts,values;
        types= new ArrayList<String>();
        atts= new ArrayList<String>();
        values= new ArrayList<String>();
        LocalClassMetadataLight selectedClass = (LocalClassMetadataLight)qtf.getList().getSelectedValue();

        for (JCheckBox checkbox : enablers){
            if (checkbox.isSelected()){
                JComponent component = (JComponent)checkbox.getClientProperty("component");
                atts.add(component.getName());
                if (component instanceof JTextField){
                    //Adds single quotes so the server has just to concatenate the tokens.  This is a temporal workaround
                    //since the server should check for these characters in order to escape them
                    //to avoid SQL injection
                    values.add(((JTextField)component).getText());
                    types.add(com.getMetaForClass(selectedClass.getClassName(),false).getTypeForAttribute(component.getName()));
                    continue;
                }
                if (component instanceof JCheckBox){
                    values.add(String.valueOf(((JCheckBox)component).isEnabled()));
                    types.add("Boolean");
                    continue;
                }
                if (component instanceof JComboBox){
                    LocalObjectListItem item =(LocalObjectListItem)((JComboBox)component).getSelectedItem();
                    //LocalClassMetadata itemClass = com.getMetaForClass(item.getClassName());
                    values.add(item.getId().toString());
                    //types.add(itemClass.getPackageName()+"."+itemClass.getClassName());
                    types.add(item.getClassName());
                    continue;
                }
            }
        }

        LocalObjectLight[] found = com.searchForObjects(selectedClass.getClassName(),atts,types, values);

        if (found == null){
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/queries/Bundle").getString("LBL_QUERY RESULT"), 
                    NotificationUtil.ERROR, com.getError());
            return;
        }

        if (found.length == 0){
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/queries/Bundle").getString("LBL_QUERY_RESULT"),
                    NotificationUtil.INFO, java.util.ResourceBundle.getBundle("org/inventory/queries/Bundle").getString("LBL_QUERY_RESULT_EMPTY"));
            return;
        }


        QueryResultTopComponent qrtc = new QueryResultTopComponent(found,selectedClass.getClassName());
        qrtc.open();
        qrtc.requestActive();
    }
}