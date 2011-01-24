/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries.graphical.windows;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalResultRecord;
import org.inventory.queries.GraphicalQueryBuilderService;
import org.inventory.queries.graphical.exportfilters.CSVFilter;
import org.inventory.queries.graphical.exportfilters.DOCFilter;
import org.inventory.queries.graphical.exportfilters.ExportFilter;
import org.inventory.queries.graphical.exportfilters.ODTFilter;
import org.inventory.queries.graphical.exportfilters.XMLFilter;

/**
 * Holds the options when exporting a search result
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ExportSettingsFrame extends javax.swing.JFrame {

    /**
     * Array with the particular settings to apply to a given filter. I.e, CSV needs a 
     * separator to be set, and that's done using the "Configure filter" button. When the 
     * separator is selected, this array is set to contain such separator
     */
    private Object[] settings;
    private GraphicalQueryBuilderService qbs;

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

    /** Creates new form ExportSettingsFrame */
    public ExportSettingsFrame(GraphicalQueryBuilderService qbs) {
        initComponents();
        initCustomComponents();
        this.qbs = qbs;
    }

    private void initCustomComponents() {
        cmbRange.addItem(Range.ALL);
        cmbRange.addItem(Range.CURRENT_PAGE);
        cmbExportTo.addItem(new CSVFilter());
        cmbExportTo.addItem(new XMLFilter());
        cmbExportTo.addItem(new DOCFilter());
        cmbExportTo.addItem(new ODTFilter());
        this.setLocationRelativeTo(getRootPane());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblExportTo = new javax.swing.JLabel();
        cmbExportTo = new javax.swing.JComboBox();
        btnExportToSettings = new javax.swing.JButton();
        lblOutputFile = new javax.swing.JLabel();
        txtOutputFile = new javax.swing.JTextField();
        lblRange = new javax.swing.JLabel();
        cmbRange = new javax.swing.JComboBox();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        btnOutputFileSet = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblExportTo.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.lblExportTo.text")); // NOI18N

        btnExportToSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/configure.png"))); // NOI18N
        btnExportToSettings.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.btnExportToSettings.text")); // NOI18N
        btnExportToSettings.setToolTipText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.btnExportToSettings.toolTipText")); // NOI18N
        btnExportToSettings.setPreferredSize(new java.awt.Dimension(24, 24));

        lblOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.lblOutputFile.text")); // NOI18N

        txtOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.txtOutputFile.text")); // NOI18N

        lblRange.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.lblRange.text")); // NOI18N

        btnCancel.setMnemonic('c');
        btnCancel.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.btnCancel.text")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnOK.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.btnOK.text")); // NOI18N
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        btnOutputFileSet.setText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.btnOutputFileSet.text")); // NOI18N
        btnOutputFileSet.setToolTipText(org.openide.util.NbBundle.getMessage(ExportSettingsFrame.class, "ExportSettingsFrame.btnOutputFileSet.toolTipText")); // NOI18N
        btnOutputFileSet.setPreferredSize(new java.awt.Dimension(24, 24));
        btnOutputFileSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOutputFileSetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addComponent(txtOutputFile, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnExportToSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOutputFileSet, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancel)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        ExportFilter selectedFilter = (ExportFilter)cmbExportTo.getSelectedItem();
        String fileName = txtOutputFile.getText().substring(0,txtOutputFile.getText().lastIndexOf(".")); //NOI18N

        if (selectedFilter instanceof CSVFilter)
            ((CSVFilter)selectedFilter).setSeparator((settings == null) ?
                null : (Character)settings[0]);

        if (cmbRange.getSelectedItem().equals(Range.ALL)){
            Object[][] allResults = LocalResultRecord.toMatrix(qbs.executeQuery(0));
            if (allResults != null){
                if (!selectedFilter.export(allResults, fileName))
                   JOptionPane.showMessageDialog(this, "Invalid file name", "Error",JOptionPane.ERROR_MESSAGE);
                else
                   JOptionPane.showMessageDialog(this, "Invalid file name", "Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnOutputFileSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOutputFileSetActionPerformed
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setDialogTitle("Select a directory");
        if (fChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            txtOutputFile.setText(fChooser.getSelectedFile().getAbsolutePath()+
                    +File.pathSeparatorChar+"query_result"+Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+ //NOI18N
                    "-"+Calendar.getInstance().get(Calendar.MINUTE)+ //NOI18N
                    (((ExportFilter)cmbExportTo.getSelectedItem()).getExtension())); //NOI18N
    }//GEN-LAST:event_btnOutputFileSetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnExportToSettings;
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnOutputFileSet;
    private javax.swing.JComboBox cmbExportTo;
    private javax.swing.JComboBox cmbRange;
    private javax.swing.JLabel lblExportTo;
    private javax.swing.JLabel lblOutputFile;
    private javax.swing.JLabel lblRange;
    private javax.swing.JTextField txtOutputFile;
    // End of variables declaration//GEN-END:variables

}
