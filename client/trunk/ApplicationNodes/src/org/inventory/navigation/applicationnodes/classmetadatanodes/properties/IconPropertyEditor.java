/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.navigation.applicationnodes.classmetadatanodes.properties;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.services.utils.Utils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

/**
 * Provides a custom property editor for icon and small-icon.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IconPropertyEditor extends PropertyEditorSupport
    implements ExPropertyEditor, VetoableChangeListener{
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    /**
     * A reference to the notification mechanism
     */
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
    /**
     * The complex dialog shown in the editor
     */
    private JComplexDialogPanel pnlMyDialog = null;
    
    
    private IconsFilters iconfilters;

    private JFileChooser fChooser;
    
    private JButton btnIconChooser;
    
    private byte[] icon=null;
    
    LocalClassMetadata oldClassMetadata;
    
    private long id;
    
    boolean isSmallIcon;
        
    public IconPropertyEditor(long id, boolean isSmallIcon) {
        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        this.id = id;
        this.isSmallIcon = isSmallIcon;
    }
    
    @Override
    public Component getCustomEditor(){
        fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(iconfilters);
        fChooser.setAcceptAllFileFilterUsed(false);
        
        com = CommunicationsStub.getInstance();
        oldClassMetadata = com.getMetaForClass(id, true);
        
        if (pnlMyDialog == null ){
            JTextField txtName = new JTextField(), txtDescription =  new JTextField();
            txtName.setName("txtSmallIcon"); //NOI18N
            txtDescription.setName("txtIcon"); //NOI18N

            btnIconChooser = new JButton();

            if(oldClassMetadata.getIcon() != null && !isSmallIcon)
                setIcon(oldClassMetadata.getIcon());
            
            else if(oldClassMetadata.getSmallIcon() != null && isSmallIcon)
                setSmallIcon(oldClassMetadata.getSmallIcon());
            
            else
                btnIconChooser.setText("...");// NOI18N
            
            if(!isSmallIcon){
                btnIconChooser.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnIconChooserActionPerformed(evt);
                    }
                });
            }
            else{
                btnIconChooser.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSmallIconChooserActionPerformed(evt);
                    }
                });
            }

            this.pnlMyDialog = new JComplexDialogPanel(
                    new String[]{java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SELECT_ICON")},
                    new JComponent[]{btnIconChooser});
            return pnlMyDialog;
        }else 
            return pnlMyDialog;
    }
    
    @Override
    public boolean supportsCustomEditor(){
        return true;
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }
       
    private void btnIconChooserActionPerformed(java.awt.event.ActionEvent evt) {                                               
        if(fChooser.showOpenDialog(fChooser)  == JFileChooser.APPROVE_OPTION){ 
            Image mySmallIcon = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (mySmallIcon == null)
                getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "Image in "+fChooser.getSelectedFile().getAbsolutePath()+" couldn't be loaded");
            else{
                if(mySmallIcon.getHeight(null) > 32) //We don't accept images of more tha 48x48 pixels
                    getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The height of the image is exceeds 32 pixels");
                else{
                    if(mySmallIcon.getWidth(null) > 32) //We don't accept images of more tha 48x48 pixels
                        getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The widtth of the image exceeds 32 pixels");
                    else{
                        try {
                            icon = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                            setIcon(Utils.getImageFromByteArray(icon));
                            
                            if(com.setClassMetadataProperties(id, null, null, null, null, icon, null, null, null, null))
                                getNotifier().showSimplePopup("Class Properties Modification", NotificationUtil.INFO, "Operation completed successfully");
                            else
                                getNotifier().showSimplePopup("Class Properties Modification", NotificationUtil.ERROR, "Operation completed with errors. Check log for details");
                        } catch (IOException ex) {
                            icon = null;
                        }
                        if (icon == null)
                            getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The file couldn't be converted");
                    }
                }
            }
        }//end if
    }
    
    private void btnSmallIconChooserActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        if (fChooser.showOpenDialog(fChooser) == JFileChooser.APPROVE_OPTION){
            Image mySmallIcon = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (mySmallIcon == null)
                getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "Image in "+fChooser.getSelectedFile().getAbsolutePath()+" couldn't be loaded");
            else{
                //This image trick if useful because for some 8bits gif, the getHeight/Width returns -1
                if((new ImageIcon(mySmallIcon)).getIconHeight() > 16) //We don't accept images of more tha 16x16 pixels
                    getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The height of the image exceeds 16 pixels");
                else{
                    if((new ImageIcon(mySmallIcon)).getIconWidth() > 16) //We don't accept images of more tha 16x16 pixels
                        getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The width of the image exceeds 16 pixels");
                    else{
                        try {
                            icon = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                            setIcon(Utils.getImageFromByteArray(icon));
                            com = CommunicationsStub.getInstance();

                            if(com.setClassMetadataProperties(id, null, null, null, icon, null, null, null, null, null))
                                getNotifier().showSimplePopup("Class Properties Modification", NotificationUtil.INFO, "Operation completed successfully");
                            else
                                getNotifier().showSimplePopup("Class Properties Modification", NotificationUtil.ERROR, "Operation completed with errors. Check log for details");
                        } catch (IOException ex) {
                           icon = null;
                        }
                        if (icon == null)
                            getNotifier().showSimplePopup("Image Load", NotificationUtil.ERROR, "The file couldn't be converted");
                    }
                }
            }
        }
    } 

    public void setIcon(Image classIcon){
        Icon newIcon = new ImageIcon(classIcon);
        btnIconChooser.setText("");
        btnIconChooser.setSize(32, 32);
        btnIconChooser.setIcon(newIcon);
    }
    
    public void setSmallIcon(Image classSmallIcon){
        Icon newIcon = new ImageIcon(classSmallIcon);
        btnIconChooser.setText("");
        btnIconChooser.setSize(16, 16);
        btnIconChooser.setIcon(newIcon);
    }

    @Override
    public void attachEnv(PropertyEnv pe) {
        
    }
}
