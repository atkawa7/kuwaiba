/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.core.templates.layouts.customshapes.nodes.actions;

import static java.awt.Component.CENTER_ALIGNMENT;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.DatatypeConverter;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.imports.filters.ImageFileFilter;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * This action sets the icon for a Custom shape in the palette
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SetPaletteIconAction extends GenericInventoryAction {
    public SetPaletteIconAction() {
        putValue(NAME, I18N.gm("action_lbl_set_palette_icon"));
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TEMPLATES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final LocalObjectListItem customShape = Utilities.actionsGlobalContext().lookup(LocalObjectListItem.class);
        if (customShape == null)
            return;
        
        JPanel pnlImgBrowser = new JPanel();
        pnlImgBrowser.setLayout(new BoxLayout(pnlImgBrowser, BoxLayout.Y_AXIS));
        pnlImgBrowser.setBorder(new EmptyBorder(10, 10, 10, 10) );
        
        JLabel lblText = new JLabel("The size of the icon must be equal or less than 33x33");

        JButton btnImageChooser = new JButton(I18N.gm("browse"));

        btnImageChooser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                ImageFileFilter imgFileFilter = new ImageFileFilter();

                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter(imgFileFilter);
                fileChooser.setMultiSelectionEnabled(false);

                if (fileChooser.showOpenDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
                    Image myIcon = Toolkit.getDefaultToolkit().createImage(fileChooser.getSelectedFile().getAbsolutePath());
                    if (myIcon == null) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, String.format(I18N.gm("image_not_loaded"), fileChooser.getSelectedFile().getAbsolutePath()));
                        return;
                    }
                    // The width and height take a little time in be set
                    while (myIcon.getWidth(null) == -1 && myIcon.getHeight(null) == -1) {}

                    if (myIcon.getWidth(null) > 33 || myIcon.getHeight(null) > 33) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                            NotificationUtil.ERROR_MESSAGE, "The size of the icon must be equal or less than 33x33");
                        return;
                    }

                    try {
                        byte [] byteArray = Utils.getByteArrayFromFile(fileChooser.getSelectedFile());

                        String fileName = fileChooser.getSelectedFile().getName();
                        String fileExtension = imgFileFilter.getExtension(fileChooser.getSelectedFile());

                        String byteArrayEncode = DatatypeConverter.printBase64Binary(byteArray);

////                        String iconAttributeValue = fileName + ";/;" +  fileExtension + ";/;" + byteArrayEncode;
////
////                        HashMap<String, Object> attributesToUpdate = new HashMap<>();
////                        attributesToUpdate.put(Constants.PROPERTY_ICON, iconAttributeValue);
////
//                        if(!CommunicationsStub.getInstance().updateObject(customShape.getClassName(), 
//                                customShape.getId(), attributesToUpdate)) {
//                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
//                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
//                        } else {
//                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
//                                NotificationUtil.INFO_MESSAGE, "The icon was set successfully");
//                        }                     
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        lblText.setAlignmentX(CENTER_ALIGNMENT);
        pnlImgBrowser.add(lblText);
        btnImageChooser.setAlignmentX(CENTER_ALIGNMENT);
        pnlImgBrowser.add(btnImageChooser);
        
        JOptionPane.showConfirmDialog(null, pnlImgBrowser, "Set palette icon", JOptionPane.CANCEL_OPTION);
    }    
}
