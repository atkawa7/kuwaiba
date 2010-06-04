/*
 *  Copyright 2010 Charles Edward Bedon Cortazar.
 * 
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.classmanager;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;

/**
 * Component providing a tool to customazi basic class information
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ConvertAsProperties(dtd = "-//org.inventory.customization.classmanager//ClassManager//EN",
autostore = false)
public final class ClassManagerTopComponent extends TopComponent {

    private static ClassManagerTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/customization/classmanager/res/icon.png";
    private static final String PREFERRED_ID = "ClassManagerTopComponent";

    private JFileChooser fChooser;
    private ClassManagerService cms;
    private NotificationUtil nu;
    private byte[] smallIcon=null;
    private byte[] icon=null;


    public ClassManagerTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ClassManagerTopComponent.class, "CTL_ClassManagerTopComponent"));
        setToolTipText(NbBundle.getMessage(ClassManagerTopComponent.class, "HINT_ClassManagerTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        initCustomComponents();
    }

    public void initCustomComponents(){
        cms = new ClassManagerService(this);
        fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(cms);
        fChooser.setAcceptAllFileFilterUsed(false);

        List<LocalClassMetadataLight> lcml = cms.getAllMeta();
        for (LocalClassMetadataLight lcm : lcml)
            cmbClass.addItem(lcm);
        cmbClass.addActionListener(cms);
        cmbClass.setSelectedIndex(-1);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtIcon = new javax.swing.JTextField();
        txtSmallIcon = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        lblIcon = new javax.swing.JLabel();
        cmbClass = new javax.swing.JComboBox();
        lblSmallIcon = new javax.swing.JLabel();
        txtDisplayName = new javax.swing.JTextField();
        lblDisplayName = new javax.swing.JLabel();
        lblClass = new javax.swing.JLabel();
        btnIconChooser = new javax.swing.JButton();
        btnSmallIconChooser = new javax.swing.JButton();
        lblDescription = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();

        txtIcon.setText(org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.txtIcon.text")); // NOI18N
        txtIcon.setEnabled(false);

        txtSmallIcon.setText(org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.txtSmallIcon.text")); // NOI18N
        txtSmallIcon.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.btnSave.text")); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblIcon, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.lblIcon.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblSmallIcon, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.lblSmallIcon.text")); // NOI18N

        txtDisplayName.setText(org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.txtDisplayName.text")); // NOI18N
        txtDisplayName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDisplayNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblDisplayName, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.lblDisplayName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblClass, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.lblClass.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnIconChooser, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.btnIconChooser.text")); // NOI18N
        btnIconChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIconChooserActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnSmallIconChooser, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.btnSmallIconChooser.text")); // NOI18N
        btnSmallIconChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSmallIconChooserActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblDescription, org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.lblDescription.text")); // NOI18N

        txtDescription.setText(org.openide.util.NbBundle.getMessage(ClassManagerTopComponent.class, "ClassManagerTopComponent.txtDescription.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblClass)
                    .addComponent(lblDisplayName)
                    .addComponent(lblDescription)
                    .addComponent(lblSmallIcon)
                    .addComponent(lblIcon))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIconChooser))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtSmallIcon, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSmallIconChooser))
                    .addComponent(txtDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(txtDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(cmbClass, 0, 271, Short.MAX_VALUE))
                .addGap(33, 33, 33))
            .addGroup(layout.createSequentialGroup()
                .addGap(186, 186, 186)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(191, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblClass)
                    .addComponent(cmbClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDisplayName)
                    .addComponent(txtDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDescription)
                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSmallIcon)
                        .addComponent(txtSmallIcon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnSmallIconChooser))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtIcon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblIcon))
                    .addComponent(btnIconChooser))
                .addGap(18, 18, 18)
                .addComponent(btnSave)
                .addContainerGap(21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtDisplayNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDisplayNameActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtDisplayNameActionPerformed

    private void btnSmallIconChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSmallIconChooserActionPerformed
         if (fChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION){
            txtSmallIcon.setText(fChooser.getSelectedFile().getAbsolutePath());
            Image mySmallIcon = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (mySmallIcon == null)
                getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "Image in "+fChooser.getSelectedFile().getAbsolutePath()+" couldn't be loaded");
            else{
                //This image trick if useful because for some 8bits gif, the getHeight/Width returns -1
                if((new ImageIcon(mySmallIcon)).getIconHeight() > 16) //We don't accept images of more tha 16x16 pixels
                    getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The height if the given image exceeds 16 pixels");
                else{
                    if((new ImageIcon(mySmallIcon)).getIconWidth() > 16) //We don't accept images of more tha 16x16 pixels
                        getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The widtth if the given image exceeds 16 pixels");
                    else{
                        smallIcon = Utils.getByteArrayFromImage(fChooser.getSelectedFile(),cms.getExtension(fChooser.getSelectedFile()));
                        if (smallIcon == null)
                            getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The file couldn't be converted");
                    }
                }
            }
         }
    }//GEN-LAST:event_btnSmallIconChooserActionPerformed

    private void btnIconChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIconChooserActionPerformed
        if (fChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION){
            Image mySmallIcon = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (mySmallIcon == null)
                getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "Image in "+fChooser.getSelectedFile().getAbsolutePath()+" couldn't be loaded");
            else{
                if(mySmallIcon.getHeight(null) > 48) //We don't accept images of more tha 48x48 pixels
                    getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The height if the given image is bigger tha 16 pixels");
                else{
                    if(mySmallIcon.getWidth(null) > 48) //We don't accept images of more tha 48x48 pixels
                        getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The widtth if the given image is bigger tha 16 pixels");
                    else{
                        icon = Utils.getByteArrayFromImage(fChooser.getSelectedFile(),cms.getExtension(fChooser.getSelectedFile()));
                        if (icon == null)
                            getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The file couldn't be converted");
                        else
                            txtIcon.setText(fChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            }
         }
    }//GEN-LAST:event_btnIconChooserActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        cms.saveProperties((LocalClassMetadataLight)cmbClass.getSelectedItem(),
                txtDisplayName.getText().trim(),txtDescription.getText().trim(),smallIcon,icon);
    }//GEN-LAST:event_btnSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIconChooser;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSmallIconChooser;
    private javax.swing.JComboBox cmbClass;
    private javax.swing.JLabel lblClass;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblDisplayName;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblSmallIcon;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JTextField txtIcon;
    private javax.swing.JTextField txtSmallIcon;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ClassManagerTopComponent getDefault() {
        if (instance == null) {
            instance = new ClassManagerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ClassManagerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ClassManagerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ClassManagerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ClassManagerTopComponent) {
            return (ClassManagerTopComponent) win;
        }
        Logger.getLogger(ClassManagerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

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

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }

    public JTextField getTxtDescription() {
        return txtDescription;
    }

    public JTextField getTxtDisplayName() {
        return txtDisplayName;
    }

    public JTextField getTxtIcon() {
        return txtIcon;
    }

    public JTextField getTxtSmallIcon() {
        return txtSmallIcon;
    }
}
