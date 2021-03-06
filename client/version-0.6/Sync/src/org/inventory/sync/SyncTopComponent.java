/*
 * Copyright (c) 2013 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package org.inventory.sync;

import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.inventory.core.services.utils.Utils;
import org.inventory.sync.graphical.dialogs.ExportSettingsPanel;
import org.inventory.sync.graphical.dialogs.WriteFile;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.inventory.sync//Sync//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "SyncTopComponent",
iconBase = "org/inventory/sync/res/sync.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.sync.SyncTopComponent")
/*@ActionReference(path = "Menu/Tools" /*, position = 333 )*/
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_SyncAction",
preferredID = "SyncTopComponent")
@Messages({
    "CTL_SyncAction=Bulk upload",
    "CTL_SyncTopComponent=Bulk upload",
    "HINT_SyncTopComponent=Create objects from a text file"
})
public final class SyncTopComponent extends TopComponent {
    
    private JFileChooser fChooser;
    private SyncService ss;
    private byte[] choosenFile = null;

    public SyncTopComponent() {
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_SyncTopComponent());
        setToolTipText(Bundle.HINT_SyncTopComponent());

    }

    public final void initCustomComponents(){
        fChooser = new JFileChooser();
        ss = new SyncService(this);
        btnFileChooser.setEnabled(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnFileChooser = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(btnFileChooser, org.openide.util.NbBundle.getMessage(SyncTopComponent.class, "SyncTopComponent.btnFileChooser.text")); // NOI18N
        btnFileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFileChooserActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/sync/res/sync24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SyncTopComponent.class, "SyncTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(btnFileChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnFileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFileChooserActionPerformed
        if (fChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            try {
                choosenFile = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_btnFileChooserActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ss.loadFile(choosenFile);
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.INFO,
                    java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_CREATED"));
        ss.downloadErrors();
        ss.downloadLog();
        if(ss.getWrongLinesResults().length>1 || ss.getLogResults().length>1){
            ExportSettingsPanel esp = new ExportSettingsPanel(ss, this);
            if(JOptionPane.showConfirmDialog(null, esp, "Choose a path", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
                WriteFile wr = new WriteFile();
                try {
                    wr.writeLog(ss.getLogResults(), esp.getPath()+"/"+ss.getLogFileName());
                    wr.writeLog(ss.getWrongLinesResults(), esp.getPath()+"/"+ss.getWrongLinesFileName());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFileChooser;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
