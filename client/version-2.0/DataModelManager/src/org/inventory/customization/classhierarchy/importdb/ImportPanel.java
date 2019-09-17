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
package org.inventory.customization.classhierarchy.importdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.core.services.api.export.filters.XMLExportFilter;

/**
 * A simple window used to import an xml file that describes a data model to be uploaded. 
 * A sample of the file format can usually be found in the server installation package.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class ImportPanel extends javax.swing.JPanel implements ActionListener {

    private File selectedFile;
    private List<LocalClassMetadata> rootElements;
    private boolean fileCorrect;

    /**
     * Creates new form ImportPanel
     */
    public ImportPanel() {
        this.fileCorrect = false;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFile = new javax.swing.JLabel();
        btnBrowseFile = new javax.swing.JButton();
        txtFileName = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(lblFile, "Choose a File:");

        org.openide.awt.Mnemonics.setLocalizedText(btnBrowseFile, "Browse");
        btnBrowseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseFileActionPerformed(evt);
            }
        });

        txtFileName.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBrowseFile)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseFile))
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Reads file path and set extension
     *
     * @param evt
     */
    private void btnBrowseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseFileActionPerformed
        JFileChooser fChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        //file chooser properties
        XMLExportFilter xMLExportFilter = XMLExportFilter.getInstance();
        fChooser.setDialogTitle("Select a file");
        fChooser.setAcceptAllFileFilterUsed(false);
        fChooser.addChoosableFileFilter(new FileNameExtensionFilter(xMLExportFilter.getDisplayName(), xMLExportFilter.getExtensionFileChooser()));

        if (fChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

            setSelectedFile(fChooser.getSelectedFile());
            txtFileName.setText(getSelectedFile().getName());
        }
    }//GEN-LAST:event_btnBrowseFileActionPerformed

    /**
     * Override in DataModelManagerTopComponent, default behavior open a new
     * windows where it show the progress saving in database
     *
     * @param ae
     */
    @Override
    public void actionPerformed(ActionEvent ae) {

    }

    //getters and setters
    /**
     * @return the rootElements
     */
    public List<LocalClassMetadata> getRootElements() {
        return rootElements;
    }

    /**
     * @param rootElements the rootElements to set
     */
    public void setRootElements(List<LocalClassMetadata> rootElements) {
        this.rootElements = rootElements;
    }

    /**
     * @return the fileCorrect
     */
    public boolean isFileCorrect() {
        return fileCorrect;
    }

    /**
     * @param fileCorrect the fileCorrect to set
     */
    public void setFileCorrect(boolean fileCorrect) {
        this.fileCorrect = fileCorrect;
    }

    /**
     * @return the selectedFile
     */
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * @param selectedFile the selectedFile to set
     */
    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowseFile;
    private javax.swing.JLabel lblFile;
    private javax.swing.JTextField txtFileName;
    // End of variables declaration//GEN-END:variables

}
