/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

/*
 * ConnectionSettingsPanel.java
 *
 * Created on Aug 16, 2010, 4:14:03 PM
 */

package org.inventory.core.authentication;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ConnectionSettingsPanel extends javax.swing.JPanel {

    /** Creates new form ConnectionSettingsPanel */
    public ConnectionSettingsPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnTestConnection = new javax.swing.JButton();
        txtServerPort = new javax.swing.JTextField();
        btnSaveConfiguration = new javax.swing.JButton();
        lblServerPort = new javax.swing.JLabel();
        lblServerAddress = new javax.swing.JLabel();
        txtServerAddress = new javax.swing.JTextField();

        btnTestConnection.setText(org.openide.util.NbBundle.getMessage(ConnectionSettingsPanel.class, "ConnectionSettingsPanel.btnTestConnection.text")); // NOI18N

        txtServerPort.setText(org.openide.util.NbBundle.getMessage(ConnectionSettingsPanel.class, "ConnectionSettingsPanel.txtServerPort.text")); // NOI18N

        btnSaveConfiguration.setText(org.openide.util.NbBundle.getMessage(ConnectionSettingsPanel.class, "ConnectionSettingsPanel.btnSaveConfiguration.text")); // NOI18N

        lblServerPort.setText(org.openide.util.NbBundle.getMessage(ConnectionSettingsPanel.class, "ConnectionSettingsPanel.lblServerPort.text")); // NOI18N

        lblServerAddress.setText(org.openide.util.NbBundle.getMessage(ConnectionSettingsPanel.class, "ConnectionSettingsPanel.lblServerAddress.text")); // NOI18N

        txtServerAddress.setText(org.openide.util.NbBundle.getMessage(ConnectionSettingsPanel.class, "ConnectionSettingsPanel.txtServerAddress.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblServerAddress)
                            .addComponent(lblServerPort))
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtServerPort)
                            .addComponent(txtServerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnTestConnection)
                        .addGap(18, 18, 18)
                        .addComponent(btnSaveConfiguration)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServerAddress)
                    .addComponent(txtServerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServerPort)
                    .addComponent(txtServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveConfiguration)
                    .addComponent(btnTestConnection))
                .addContainerGap(24, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSaveConfiguration;
    private javax.swing.JButton btnTestConnection;
    private javax.swing.JLabel lblServerAddress;
    private javax.swing.JLabel lblServerPort;
    private javax.swing.JTextField txtServerAddress;
    private javax.swing.JTextField txtServerPort;
    // End of variables declaration//GEN-END:variables

}
