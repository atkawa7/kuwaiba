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

package org.inventory.sync.graphical.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import org.inventory.sync.SyncService;
import org.inventory.sync.SyncTopComponent;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ExportSettingsPanel extends javax.swing.JPanel implements ActionListener{

    /**
     * Array with the particular settings to apply to a given filter. I.e, CSV needs a
     * separator to be set, and that's done using the "Configure filter" button. When the
     * separator is selected, this array is set to contain such separator
     */
    private SyncService ss;
    private SyncTopComponent stc;
    private String path;

    /** Creates new form ExportSettingsPanel */
    public ExportSettingsPanel(SyncService ss, SyncTopComponent stc) {
        initComponents();
        this.ss = ss;
        this.stc = stc;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtOutputFile = new javax.swing.JTextField();
        lblOutputFile = new javax.swing.JLabel();
        btnOutputFileSet = new javax.swing.JButton();
        lblOutputFile1 = new javax.swing.JLabel();
        lblOutputFile2 = new javax.swing.JLabel();

        txtOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.txtOutputFile.text")); // NOI18N

        lblOutputFile.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.lblOutputFile.text")); // NOI18N

        btnOutputFileSet.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.btnOutputFileSet.text")); // NOI18N
        btnOutputFileSet.setToolTipText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.btnOutputFileSet.toolTipText")); // NOI18N
        btnOutputFileSet.setPreferredSize(new java.awt.Dimension(24, 24));
        btnOutputFileSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOutputFileSetActionPerformed(evt);
            }
        });

        lblOutputFile1.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.lblOutputFile1.text")); // NOI18N

        lblOutputFile2.setText(org.openide.util.NbBundle.getMessage(ExportSettingsPanel.class, "ExportSettingsPanel.lblOutputFile2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblOutputFile)
                        .addGap(11, 11, 11)
                        .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOutputFileSet, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblOutputFile2, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOutputFile1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOutputFile1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblOutputFile2)
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputFile)
                    .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOutputFileSet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOutputFileSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOutputFileSetActionPerformed
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setDialogTitle("Select a directory");
        if (fChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            txtOutputFile.setText(fChooser.getSelectedFile().getAbsolutePath()); //NOI18N
            path = fChooser.getSelectedFile().getAbsolutePath();
        }
}//GEN-LAST:event_btnOutputFileSetActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOutputFileSet;
    private javax.swing.JLabel lblOutputFile;
    private javax.swing.JLabel lblOutputFile1;
    private javax.swing.JLabel lblOutputFile2;
    private javax.swing.JTextField txtOutputFile;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
    }
    
    public String getPath(){
        return path;
    }
    
}