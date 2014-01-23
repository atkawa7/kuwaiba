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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.utils.Utils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

/**
 * Provides a custom property editor for icon and small-icon.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IconPropertyEditor extends PropertyEditorSupport
    implements ExPropertyEditor{
       
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    /**
     * A reference to the notification mechanism
     */
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
    
    private byte[] icon = null;
        
    private long id;
    
    private String attribute;
    
    private int maxAllowedSize;
    
    private InnerPanel myPanel;
        
    public IconPropertyEditor(long id, String attribute) {
        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        this.id = id;
        this.attribute = attribute;
        this.com = CommunicationsStub.getInstance();
    }

    @Override
    public String getAsText() {
        return "[Click the button to choose a file]";
    }
    
    @Override
    public Component getCustomEditor(){
       if (myPanel == null){
            myPanel = new InnerPanel();
            maxAllowedSize = (attribute.equals(Constants.PROPERTY_ICON)) ? 32 : 16;
       }
       return myPanel;
    }
    
    @Override
    public boolean supportsCustomEditor(){
        return true;
    }
    
    public void captureImage(JFileChooser fChooser){
        if(fChooser.showOpenDialog(fChooser)  == JFileChooser.APPROVE_OPTION){ 
            Image myIcon = Toolkit.getDefaultToolkit().createImage(fChooser.getSelectedFile().getAbsolutePath());
            if (myIcon == null)
                nu.showSimplePopup("Image Load", NotificationUtil.ERROR, String.format("Image in %s couldn't be loaded", fChooser.getSelectedFile().getAbsolutePath()));
            else{
                if((myIcon.getHeight(null) > maxAllowedSize) || (myIcon.getWidth(null) > maxAllowedSize)) //Images have limits depending of if you need to set "icon" or "smallIcon"
                    nu.showSimplePopup("Image Load", NotificationUtil.ERROR, String.format("The size of the image is exceeds the limits"));
                else{
                    try {
                        icon = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                        myPanel.updateIcon();
                        //This should be here but in the setValue method, however I haven't discovered yet why clicking on "cancel" still calls setValue
                        if (attribute.equals(Constants.PROPERTY_ICON)){
                            if(!com.setClassMetadataProperties(id, null, null, null, null, icon, null, null, null, null))
                                nu.showSimplePopup("Class Properties", NotificationUtil.ERROR, com.getError());
                            else
                                Cache.getInstace().resetAll();
                        }else{
                            if(!com.setClassMetadataProperties(id, null, null, null, icon, null, null, null, null, null))
                                nu.showSimplePopup("Class Properties", NotificationUtil.ERROR, com.getError());
                            else
                                Cache.getInstace().resetAll();
                        }
                    } catch (IOException ex) {
                        icon = null;
                        nu.showSimplePopup("Image Load", NotificationUtil.ERROR, "The file couldn't be converted: " + ex.getMessage());
                    }
                }
            }
        }
    }
           
    @Override
    public void attachEnv(PropertyEnv pe) {
       //Here comes the code to access the property using us
    }  
    
    private class InnerPanel extends JPanel{
        private JFileChooser fChooser;;
        private JLabel lblText;
        private JButton btnImageChooser;
        
        public InnerPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(10, 10, 10, 10) );
            fChooser = new JFileChooser();
            fChooser.setAcceptAllFileFilterUsed(false);
            fChooser.setFileFilter(new ImageFileFilter());
            fChooser.setMultiSelectionEnabled(false);

            lblText = new JLabel("Select an image file (up to 32x32 pixels for icons and 16x16 for small icons):");
            
            btnImageChooser = new JButton("Browse...");

            btnImageChooser.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    captureImage(fChooser);
                }
            });
            
            lblText.setAlignmentX(CENTER_ALIGNMENT);
            add(lblText);
            btnImageChooser.setAlignmentX(CENTER_ALIGNMENT);
            add(btnImageChooser);
        }
        
        public void updateIcon() {
            btnImageChooser.setIcon(new ImageIcon(org.inventory.communications.util.Utils.getImageFromByteArray(icon)));
        }
    }
}