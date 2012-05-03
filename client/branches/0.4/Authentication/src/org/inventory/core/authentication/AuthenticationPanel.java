/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.authentication;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * This is the main auth panel which contains the login and password textfields
 * and the connection settings section
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AuthenticationPanel extends javax.swing.JPanel {
    private String detailedError;

    /** Creates new form AuthenticationPanel */
    public AuthenticationPanel() {
        initComponents();
        initCustomComponents();
    }
    
    private void initCustomComponents(){
        pnlSettingsContainer.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                toggleView();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        //Finally we embed the connections settings into the collapsable panel
        pnlSettingsContainer.add(new ConnectionSettingsPanel(),BorderLayout.CENTER);
        pnlSettingsContainer.getComponent(0).setVisible(false);
        txtUser.setRequestFocusEnabled(true);
        lblError.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlLogin = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        lblError = new javax.swing.JLabel();
        pnlSettingsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        pnlLogin.setMaximumSize(new java.awt.Dimension(380, 110));
        pnlLogin.setPreferredSize(new java.awt.Dimension(380, 110));

        lblUser.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.lblUser.text")); // NOI18N

        lblPassword.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.lblPassword.text")); // NOI18N

        txtUser.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.txtUser.text")); // NOI18N

        txtPassword.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.txtPassword.text")); // NOI18N

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/authentication/res/error.png"))); // NOI18N
        lblError.setText(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.lblError.text")); // NOI18N
        lblError.setAutoscrolls(true);
        lblError.setFocusable(false);
        lblError.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblErrorMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlLoginLayout = new javax.swing.GroupLayout(pnlLogin);
        pnlLogin.setLayout(pnlLoginLayout);
        pnlLoginLayout.setHorizontalGroup(
            pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLoginLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLoginLayout.createSequentialGroup()
                        .addGroup(pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPassword)
                            .addComponent(lblUser))
                        .addGap(16, 16, 16)
                        .addGroup(pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPassword)
                            .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(37, Short.MAX_VALUE))
                    .addGroup(pnlLoginLayout.createSequentialGroup()
                        .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(360, 360, 360))))
        );
        pnlLoginLayout.setVerticalGroup(
            pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLoginLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(31, 31, 31))
        );

        add(pnlLogin, java.awt.BorderLayout.NORTH);

        pnlSettingsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AuthenticationPanel.class, "AuthenticationPanel.pnlSettingsContainer.border.title"))); // NOI18N
        pnlSettingsContainer.setRequestFocusEnabled(false);
        pnlSettingsContainer.setLayout(new java.awt.BorderLayout());
        add(pnlSettingsContainer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void lblErrorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblErrorMouseClicked
        JOptionPane.showMessageDialog(this, detailedError, "Error", JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_lblErrorMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlLogin;
    private javax.swing.JPanel pnlSettingsContainer;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables

    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnExit;

    /**
     * Hides or shows the collapseable panel
     * Thanks to Craig Wood and H Tasfr at CodeRanch for their ideas on this
     * http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels
     */
    private void toggleView(){
        JPanel contained = (JPanel)pnlSettingsContainer.getComponent(0);
        if (contained.isShowing()){
            contained.setVisible(false);
            ((TitledBorder)pnlSettingsContainer.getBorder()).setTitle("[+] Connection Settings");
        }
        else{
            contained.setVisible(true);
            ((TitledBorder)pnlSettingsContainer.getBorder()).setTitle("[-] Connection Settings");
        }
        ((JDialog)SwingUtilities.getAncestorOfClass(JDialog.class,this)).pack();
    }

    public Object[] getOptions(){
        return new Object[]{btnLogin,btnExit};
    }

    public JLabel getLblError(){
        return this.lblError;
    }

    public JTextField getTxtUser(){
        return this.txtUser;
    }

    public JPasswordField getTxtPassword(){
        return this.txtPassword;
    }

    public ConnectionSettingsPanel getContainedPanel(){
        return (ConnectionSettingsPanel)this.pnlSettingsContainer.getComponent(0);
    }

    public String getDetailedError() {
        return detailedError;
    }

    public void setDetailedError(String detailedError) {
        this.detailedError = detailedError;
    }
}
