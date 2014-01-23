/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.queries.graphical.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Calendar;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.inventory.queries.QueryManagerService;
import org.inventory.queries.graphical.ComplexQueryResultTopComponent;
import org.inventory.core.services.api.export.CSVFilter;
import org.inventory.core.services.api.export.ExportFilter;
import org.inventory.core.services.api.export.XMLFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Dialog to export a table to a file
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ExportSettingsPanel extends JPanel implements ActionListener{

    /**
     * Array with the particular settings to apply to a given filter. I.e, CSV needs a
     * separator to be set, and that's done using the "Configure filter" button. When the
     * separator is selected, this array is set to contain such separator
     */
    private Object[] settings;
    private QueryManagerService qbs;
    private ComplexQueryResultTopComponent qrtc;

    private enum Range{
        ALL(0,"All"),
        CURRENT_PAGE(1,"Current Page");
        int id;
        String label;
        Range(int id, String label){
            this.id = id;
            this.label =label;
        }

        public int id(){return id;}
        public String label(){return label;}

        @Override
        public String toString(){
            return label;
        }
    };

    /** Creates new form ExportSettingsPanel */
    public ExportSettingsPanel(QueryManagerService qbs, ComplexQueryResultTopComponent qrtc) {
        initComponents();
        initCustomComponents();
        this.qbs = qbs;
        this.qrtc = qrtc;
    }

    private void initCustomComponents() {
        cmbRange.addItem(Range.ALL);
        cmbRange.addItem(Range.CURRENT_PAGE);
        cmbExportTo.addItem(new CSVFilter());
        cmbExportTo.addItem(new XMLFilter());
        //cmbExportTo.addItem(new DOCFilter());
        //cmbExportTo.addItem(new ODTFilter());
        cmbExportTo.addItemListener(new ItemListener() {

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

        btnExportToSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/configure.png"))); // NOI18N
        btnExportToSettings.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.btnExportToSettings.text")); // NOI18N
        btnExportToSettings.setToolTipText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.btnExportToSettings.toolTipText")); // NOI18N
        btnExportToSettings.setPreferredSize(new java.awt.Dimension(24, 24));
        btnExportToSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportToSettingsActionPerformed(evt);
            }
        });

        txtOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.txtOutputFile.text")); // NOI18N

        lblOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.lblOutputFile.text")); // NOI18N

        lblRange.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.lblRange.text")); // NOI18N

        btnOutputFileSet.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.btnOutputFileSet.text")); // NOI18N
        btnOutputFileSet.setToolTipText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.btnOutputFileSet.toolTipText")); // NOI18N
        btnOutputFileSet.setPreferredSize(new java.awt.Dimension(24, 24));
        btnOutputFileSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOutputFileSetActionPerformed(evt);
            }
        });

        lblExportTo.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.lblExportTo.text")); // NOI18N

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
                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnExportToSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblOutputFile)
                            .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOutputFileSet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblExportTo)
                            .addComponent(cmbExportTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(lblRange))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(cmbRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportToSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportToSettingsActionPerformed
        ExportFilter selectedFilter = (ExportFilter)cmbExportTo.getSelectedItem();
        if (selectedFilter instanceof CSVFilter){
            CSVExportSettingsPanel settingsPanel = new CSVExportSettingsPanel();
            DialogDescriptor dd = new DialogDescriptor(settingsPanel, "CSV Filter Settings", true, settingsPanel);
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            settings = new Object[1];
            settings[0] = settingsPanel.getSelectedCharacter();
        }else JOptionPane.showMessageDialog(this, "No advanced settings required","Exporting",JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_btnExportToSettingsActionPerformed

    private void btnOutputFileSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOutputFileSetActionPerformed
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setDialogTitle("Select a directory");
        if (fChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            txtOutputFile.setText(fChooser.getSelectedFile().getAbsolutePath()+
                    File.separator+"query_result"+Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+ //NOI18N
                    "-"+Calendar.getInstance().get(Calendar.MINUTE)+ //NOI18N
                    (((ExportFilter)cmbExportTo.getSelectedItem()).getExtension())); //NOI18N
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

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == DialogDescriptor.OK_OPTION){
            if (txtOutputFile.getText().trim().equals("")){//NOI18N
                JOptionPane.showMessageDialog(this, "Invalid file name", "Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            ExportFilter selectedFilter = (ExportFilter)cmbExportTo.getSelectedItem();

            String fileName;

            if (txtOutputFile.getText().lastIndexOf(".") != -1)
                fileName = txtOutputFile.getText().substring(0,txtOutputFile.getText().lastIndexOf(".")); //NOI18N
            else fileName = txtOutputFile.getText();

            Object[][] allResults = null;

            if (selectedFilter instanceof CSVFilter)
                ((CSVFilter)selectedFilter).setSeparator((settings == null) ?
                    null : (Character)settings[0]);

            if (cmbRange.getSelectedItem().equals(Range.ALL)){
                LocalResultRecord[] results = qbs.executeQuery(0);
                if (results == null)
                    allResults = null;
                else{
                    if (results.length == 0)
                        allResults = new Object[0][0];
                    else{
                        allResults =new Object[results.length][results[0].getExtraColumns().size() + 1];
                        for (int i = 0; i < results.length; i++){
                            allResults[i][0] = results[i].getObject();
                            for (int j = 0; j < results[i].getExtraColumns().size();j++)
                                allResults[i][j + 1] = results[i].getExtraColumns().get(j);
                        }
                    }
                }
            }
            else
                allResults = qrtc.getCurrentResults();

            if (allResults != null){
                if (!selectedFilter.export(allResults, fileName + selectedFilter.getExtension()))
                    JOptionPane.showMessageDialog(this, "Error exporting file. See log for details", "Error",JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(this, "Results exported successfully", "Success",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void updateExtension(Object source) {
        if (!txtOutputFile.getText().trim().equals(""))
            txtOutputFile.setText(
                    txtOutputFile.getText().substring(0, txtOutputFile.getText().lastIndexOf('.'))+
                    ((ExportFilter)((JComboBox)source).getSelectedItem()).getExtension());
    }
}