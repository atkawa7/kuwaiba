/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.models.physicalconnections.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;

/**
 * GUI components of the first step of the New Link wizard
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public final class NewLinkVisualPanel1 extends JPanel {
    /**
     * Creates new form NewLinkVisualPanel1
     */
    public NewLinkVisualPanel1() {
        initComponents();
        List<LocalClassMetadataLight> linkClasses = CommunicationsStub.getInstance().
                getLightSubclasses(Constants.CLASS_GENERICPHYSICALLINK, false, false);
        
        cmbLinkClass.setModel(linkClasses == null ? new DefaultComboBoxModel() : new DefaultComboBoxModel(linkClasses.toArray()));
        
        if (!linkClasses.isEmpty()) {
            List<LocalObjectLight> linkTemplates = CommunicationsStub.getInstance().getTemplatesForClass(((LocalClassMetadataLight)cmbLinkClass.getItemAt(0)).getClassName(), false);
            cmbLinkTemplate.setModel(new DefaultComboBoxModel(linkTemplates.toArray(new LocalObjectLight[0])));
            
            chkNoTemplate.setSelected(linkTemplates.isEmpty());
            chkNoTemplate.setEnabled(!linkTemplates.isEmpty());
            cmbLinkTemplate.setEnabled(!linkTemplates.isEmpty());
        } else {
            cmbLinkTemplate.setModel(new DefaultComboBoxModel());
            chkNoTemplate.setSelected(true);
            chkNoTemplate.setEnabled(false);
            cmbLinkTemplate.setEnabled(false);
        }
        
        cmbLinkClass.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    LocalClassMetadataLight selectedClass = (LocalClassMetadataLight)cmbLinkClass.getSelectedItem();
                    List<LocalObjectLight> linkTemplates = CommunicationsStub.getInstance().getTemplatesForClass(selectedClass.getClassName(), false);
                    ((DefaultComboBoxModel)cmbLinkTemplate.getModel()).removeAllElements();
                    if (linkTemplates != null) {
                        cmbLinkTemplate.setModel(new DefaultComboBoxModel(linkTemplates.toArray(new LocalObjectLight[0])));
                        chkNoTemplate.setSelected(linkTemplates.isEmpty());
                        chkNoTemplate.setEnabled(!linkTemplates.isEmpty());
                        cmbLinkTemplate.setEnabled(!linkTemplates.isEmpty());
                    }
                }
            }
        });    
    }

    public String getLinkName() {
        return txtLinkName.getText();
    }
    
    public LocalClassMetadataLight getLinkClass() {
        return (LocalClassMetadataLight)cmbLinkClass.getSelectedItem();
    }
    
    public LocalObjectLight getLinkTemplate() {
        return (LocalObjectLight)cmbLinkTemplate.getSelectedItem();
    }
    
    public boolean dontUseTemplate() {
        return chkNoTemplate.isSelected();
    }
    
    @Override
    public String getName() {
        return "Link information";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblLinkName = new javax.swing.JLabel();
        txtLinkName = new javax.swing.JTextField();
        lblLinkClass = new javax.swing.JLabel();
        cmbLinkClass = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cmbLinkTemplate = new javax.swing.JComboBox();
        chkNoTemplate = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(lblLinkName, org.openide.util.NbBundle.getMessage(NewLinkVisualPanel1.class, "NewLinkVisualPanel1.lblLinkName.text")); // NOI18N

        txtLinkName.setText(org.openide.util.NbBundle.getMessage(NewLinkVisualPanel1.class, "NewLinkVisualPanel1.txtLinkName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLinkClass, org.openide.util.NbBundle.getMessage(NewLinkVisualPanel1.class, "NewLinkVisualPanel1.lblLinkClass.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewLinkVisualPanel1.class, "NewLinkVisualPanel1.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkNoTemplate, org.openide.util.NbBundle.getMessage(NewLinkVisualPanel1.class, "NewLinkVisualPanel1.chkNoTemplate.text")); // NOI18N
        chkNoTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNoTemplateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLinkName)
                    .addComponent(lblLinkClass)
                    .addComponent(jLabel1))
                .addGap(44, 44, 44)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkNoTemplate)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtLinkName, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbLinkClass, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(cmbLinkTemplate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLinkName)
                    .addComponent(txtLinkName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLinkClass)
                    .addComponent(cmbLinkClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbLinkTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(chkNoTemplate)
                .addContainerGap(69, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chkNoTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNoTemplateActionPerformed
        cmbLinkTemplate.setEnabled(!chkNoTemplate.isSelected());
    }//GEN-LAST:event_chkNoTemplateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkNoTemplate;
    private javax.swing.JComboBox cmbLinkClass;
    private javax.swing.JComboBox cmbLinkTemplate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblLinkClass;
    private javax.swing.JLabel lblLinkName;
    private javax.swing.JTextField txtLinkName;
    // End of variables declaration//GEN-END:variables

}
