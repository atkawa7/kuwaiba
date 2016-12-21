/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.services.api.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.inventory.core.services.api.export.filters.TextExportFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Dialog to export a table to a file
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ExportTablePanel extends JPanel implements ActionListener{

    private TextExportFilter[] filters;
    private ExportableTable exportable;

    /** Creates new form ExportSettingsPanel */
    public ExportTablePanel(TextExportFilter[] filters, ExportableTable exportable) {
        this.filters = filters;
        initComponents();
        initCustomComponents();
        this.exportable = exportable;
    }

    private void initCustomComponents() {
        cmbRange.addItem(ExportableTable.Range.ALL);
        cmbRange.addItem(ExportableTable.Range.CURRENT_PAGE);
        for (TextExportFilter filter : filters)
            cmbExportTo.addItem(filter);
        cmbExportTo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                updateExtension(e.getSource());
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnExportToSettings = new javax.swing.JButton();
        txtOutputFile = new javax.swing.JTextField();
        lblOutputFile = new javax.swing.JLabel();
        lblRange = new javax.swing.JLabel();
        cmbRange = new javax.swing.JComboBox();
        btnOutputFileSet = new javax.swing.JButton();
        lblExportTo = new javax.swing.JLabel();
        cmbExportTo = new javax.swing.JComboBox();

        btnExportToSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/services/res/configure.png"))); // NOI18N
        btnExportToSettings.setText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.btnExportToSettings.text")); // NOI18N
        btnExportToSettings.setToolTipText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.btnExportToSettings.toolTipText")); // NOI18N
        btnExportToSettings.setPreferredSize(new java.awt.Dimension(24, 24));
        btnExportToSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportToSettingsActionPerformed(evt);
            }
        });

        txtOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.txtOutputFile.text")); // NOI18N

        lblOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.lblOutputFile.text")); // NOI18N

        lblRange.setText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.lblRange.text")); // NOI18N

        btnOutputFileSet.setText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.btnOutputFileSet.text")); // NOI18N
        btnOutputFileSet.setToolTipText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.btnOutputFileSet.toolTipText")); // NOI18N
        btnOutputFileSet.setPreferredSize(new java.awt.Dimension(24, 24));
        btnOutputFileSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOutputFileSetActionPerformed(evt);
            }
        });

        lblExportTo.setText(org.openide.util.NbBundle.getMessage(ExportTablePanel.class, "ExportTablePanel.lblExportTo.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblOutputFile)
                    .addComponent(lblExportTo)
                    .addComponent(lblRange))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbRange, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbExportTo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExportToSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOutputFileSet, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnExportToSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtOutputFile, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblOutputFile)
                                .addComponent(btnOutputFileSet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbExportTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblExportTo))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRange))
                .addGap(40, 40, 40))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportToSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportToSettingsActionPerformed
        TextExportFilter selectedFilter = (TextExportFilter)cmbExportTo.getSelectedItem();
        if (selectedFilter.getExportSettingsPanel() != null){
            DialogDescriptor dd = new DialogDescriptor(selectedFilter.getExportSettingsPanel(), 
                    "Export Settings", true, null);
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        }else JOptionPane.showMessageDialog(this, "No advanced settings required","Exporting",JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_btnExportToSettingsActionPerformed

    private void btnOutputFileSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOutputFileSetActionPerformed
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setDialogTitle("Select a directory");
        if (fChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            txtOutputFile.setText(fChooser.getSelectedFile().getAbsolutePath()+
                    File.separator + "results" + Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+ //NOI18N
                    "-"+Calendar.getInstance().get(Calendar.MINUTE)+ //NOI18N
                    (((TextExportFilter)cmbExportTo.getSelectedItem()).getExtension())); //NOI18N
}//GEN-LAST:event_btnOutputFileSetActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportToSettings;
    private javax.swing.JButton btnOutputFileSet;
    private javax.swing.JComboBox cmbExportTo;
    private javax.swing.JComboBox cmbRange;
    private javax.swing.JLabel lblExportTo;
    private javax.swing.JLabel lblOutputFile;
    private javax.swing.JLabel lblRange;
    private javax.swing.JTextField txtOutputFile;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == DialogDescriptor.OK_OPTION){
            if (txtOutputFile.getText().trim().equals("")){//NOI18N
                JOptionPane.showMessageDialog(this, "Invalid file name", "Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            TextExportFilter selectedFilter = (TextExportFilter)cmbExportTo.getSelectedItem();

            Object[][] allResults = exportable.getResults((ExportableTable.Range)cmbRange.getSelectedItem());
            
            try{
                FileOutputStream fos = new FileOutputStream(txtOutputFile.getText());

                if (allResults != null){
                    if (!selectedFilter.export(allResults, fos))
                        JOptionPane.showMessageDialog(this, "Error exporting file", "Error",JOptionPane.ERROR_MESSAGE);
                    else
                        JOptionPane.showMessageDialog(this, "Results exported successfully", "Success",JOptionPane.INFORMATION_MESSAGE);
                }
                fos.close();
            }catch(Exception ex){
                JOptionPane.showMessageDialog(this, String.format("Error exporting file %s", ex.getMessage()), "Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateExtension(Object source) {
        if (!txtOutputFile.getText().trim().isEmpty())
            txtOutputFile.setText(
                    txtOutputFile.getText().substring(0, txtOutputFile.getText().lastIndexOf('.'))+
                    ((TextExportFilter)((JComboBox)source).getSelectedItem()).getExtension());
    }
    
    
}