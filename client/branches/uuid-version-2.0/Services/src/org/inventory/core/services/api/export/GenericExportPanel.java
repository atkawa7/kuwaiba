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
package org.inventory.core.services.api.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.inventory.core.services.api.export.filters.TextExportFilter;
import org.inventory.core.services.api.export.filters.XMLExportFilter;
import org.inventory.core.services.i18n.I18N;
import org.openide.DialogDescriptor;

/**
 * panel to export a simple 'xml' file
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class GenericExportPanel extends javax.swing.JPanel implements ActionListener {

    private final String defaultFileName;
    private final XMLExportFilter[] filters;

    /**
     * default constructor
     *
     * @param filters
     * @param defaultFileName
     */
    public GenericExportPanel(XMLExportFilter[] filters, String defaultFileName) {
        this.defaultFileName = defaultFileName;
        this.filters = filters;
        initComponents();
        initCustomComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtOutputFile = new javax.swing.JTextField();
        lblOutputFile = new javax.swing.JLabel();
        btnChooseFile = new javax.swing.JButton();
        lblExportTo = new javax.swing.JLabel();
        cmbExportTo = new javax.swing.JComboBox();

        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(349, 120));

        org.openide.awt.Mnemonics.setLocalizedText(lblOutputFile, "Export To");

        org.openide.awt.Mnemonics.setLocalizedText(btnChooseFile, "...");
        btnChooseFile.setToolTipText("Select an output file");
        btnChooseFile.setPreferredSize(new java.awt.Dimension(24, 24));
        btnChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseFileActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblExportTo, "Format");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblExportTo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbExportTo, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblOutputFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputFile)
                    .addComponent(txtOutputFile, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(btnChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbExportTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblExportTo))
                .addGap(41, 41, 41))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Initialization of custom properties such as 'xml' filter
     */
    private void initCustomComponents() {

        for (XMLExportFilter filter : filters) {
            cmbExportTo.addItem(filter);
        }
        cmbExportTo.addItemListener((ItemEvent e) -> {
            updateExtension(e.getSource());
        });
    }
    
    /**
     * Action performed for choose file button
     * 
     * @param evt 
     */
    private void btnChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseFileActionPerformed

        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setDialogTitle(I18N.gm("select_directory"));

        if (fChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtOutputFile.setText(fChooser.getSelectedFile().getAbsolutePath()
                    + File.separator
                    + this.defaultFileName
                    + "_" + Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    +//NOI18N
                    "-" + Calendar.getInstance().get(Calendar.MINUTE)
                    + //NOI18N
                    (((XMLExportFilter) cmbExportTo.getSelectedItem()).getExtension())); //NOI18N
        }
    }//GEN-LAST:event_btnChooseFileActionPerformed

    /**
     * Add filter inside combobox element
     * 
     * @param source 
     */
    private void updateExtension(Object source) {
        if (!txtOutputFile.getText().trim().isEmpty()) {
            txtOutputFile.setText(
                    txtOutputFile.getText().substring(0, txtOutputFile.getText().lastIndexOf('.'))
                    + ((TextExportFilter) ((JComboBox) source).getSelectedItem()).getExtension());
        }
    }
    
    /**
     * Default action for 'OK' button
     * 
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == DialogDescriptor.OK_OPTION) {
            try {
                if (txtOutputFile.getText().trim().equals("")) {//NOI18N
                    JOptionPane.showMessageDialog(this, I18N.gm("invalid_file_name"), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                XMLExportFilter selectedFilter = XMLExportFilter.getInstance();
                if (selectedFilter.export(txtOutputFile.getText())) {
                    JOptionPane.showMessageDialog(this, I18N.gm("save_file_successful"), I18N.gm("save_file_successful"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, I18N.gm("save_file_error"), I18N.gm("save_file_error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseFile;
    private javax.swing.JComboBox cmbExportTo;
    private javax.swing.JLabel lblExportTo;
    private javax.swing.JLabel lblOutputFile;
    private javax.swing.JTextField txtOutputFile;
    // End of variables declaration//GEN-END:variables

}
