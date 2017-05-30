/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.updates.windows;

import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import org.inventory.updates.Installer;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;

/**
 * Creates new form dialog for Options in an Update Center
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class UpdateCenterOptionsDialog extends JDialog {
    /**
     * User preference key to get if update center warning messages are enable o disable
     */
    public static final String PREFERENCE_KEY_UC_WARNINGS = "showUpdateCenterWarnings";
    /**
     * User preference key to determine if Kuwaiba was open for first time
     */
    public static final String PREFERENCE_KEY_FIRST_LOAD = "showUpdateCenterWarnings";
    
    private static UpdateCenterOptionsDialog instance;
    
    private final Preferences preferences = Preferences.userRoot().node(UpdateCenterOptionsDialog.class.getName());
    private UpdateUnitProvider updateUnitProvider = null;
    private String oldURL = null;
    
    private UpdateCenterOptionsDialog() {
        super((Frame) null, true);
        initComponents();
        initCustomComponents();
    }
    
    private void initCustomComponents() {
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false);
        
        for (UpdateUnitProvider provider : providers) {
            if (provider.getDisplayName() == null)
                continue;
            
            String displayName = ResourceBundle.getBundle("org/inventory/updates/Bundle")
                .getString("Services/AutoupdateType/org_inventory_updates_update_center.instance");
            
            if (provider.getDisplayName().equals(displayName)) {
                updateUnitProvider = provider;
                oldURL = provider.getProviderURL().toString();
                txtUpdateCenterUrl.setText(oldURL);
            }
        }
        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/updates/res/error.png"))); // NOI18N
        lblError.setAutoscrolls(true);
        lblError.setVisible(false);
                
        boolean isFirstLoad = preferences.getBoolean(PREFERENCE_KEY_FIRST_LOAD, true);
        boolean ckbWarningSelected = true;
        
        if (isFirstLoad) {
            preferences.putBoolean(PREFERENCE_KEY_FIRST_LOAD, false);
            preferences.putBoolean(PREFERENCE_KEY_UC_WARNINGS, true);
        } else {
            ckbWarningSelected = preferences.getBoolean(PREFERENCE_KEY_UC_WARNINGS, true);
        }
        ckbWarning.setSelected(ckbWarningSelected);
    }
        
    public static UpdateCenterOptionsDialog getInstance() {
        return instance == null ? instance = new UpdateCenterOptionsDialog() : instance;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        txtUpdateCenterUrl = new javax.swing.JTextField();
        lblUpdateCenter = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        ckbWarning = new javax.swing.JCheckBox();

        setTitle(org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.title")); // NOI18N
        setLocationByPlatform(true);
        setModal(true);
        setName("UCOptionsDialog"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOK, org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.btnOK.text")); // NOI18N
        btnOK.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnOKMouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.btnCancel.text")); // NOI18N
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
            }
        });

        txtUpdateCenterUrl.setColumns(40);
        txtUpdateCenterUrl.setText(org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.txtUpdateCenterUrl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblUpdateCenter, org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.lblUpdateCenter.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblError, org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.lblError.text")); // NOI18N
        lblError.setToolTipText(org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.lblError.toolTipText")); // NOI18N
        lblError.setMaximumSize(new java.awt.Dimension(128, 15));
        lblError.setMinimumSize(new java.awt.Dimension(128, 15));
        lblError.setPreferredSize(new java.awt.Dimension(128, 15));

        org.openide.awt.Mnemonics.setLocalizedText(ckbWarning, org.openide.util.NbBundle.getMessage(UpdateCenterOptionsDialog.class, "UpdateCenterOptionsDialog.ckbWarning.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(separator)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ckbWarning)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblUpdateCenter)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUpdateCenterUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUpdateCenter)
                    .addComponent(txtUpdateCenterUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ckbWarning)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOK)
                    .addComponent(btnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleParent(this);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOKMouseClicked
        if (updateUnitProvider != null) {
            String newUpdateCenterURL = txtUpdateCenterUrl.getText();
            
            try {
                URL url = new URL(newUpdateCenterURL);
                
                updateUnitProvider.setProviderURL(url);
                
                try {
                    updateUnitProvider.refresh(null, true);
                    
                    if (updateUnitProvider.getUpdateUnits() == null || updateUnitProvider.getUpdateUnits().isEmpty()) {
                        lblError.setText(String.format("The URL specified %s is not an update center", newUpdateCenterURL));
                        lblError.setVisible(true);
                    } else {
                        setVisible(false);
                        Installer.runUpdate();
                    }
                } catch (IOException ex) {
                    if (oldURL.equals(newUpdateCenterURL)) {
                        setVisible(false);
                    } else {
                        lblError.setText("Update Center could not be reached, please contact your administrator");
                        lblError.setVisible(true);
                    }
                }
            } catch (MalformedURLException ex) {
                
                lblError.setText(String.format("Malformed URL %s", newUpdateCenterURL));
                lblError.setVisible(true);
            }
        }
        preferences.putBoolean(PREFERENCE_KEY_UC_WARNINGS, ckbWarning.isSelected());
    }//GEN-LAST:event_btnOKMouseClicked

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        setVisible(false);
    }//GEN-LAST:event_btnCancelMouseClicked
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JCheckBox ckbWarning;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblUpdateCenter;
    private javax.swing.JSeparator separator;
    private javax.swing.JTextField txtUpdateCenterUrl;
    // End of variables declaration//GEN-END:variables
}
