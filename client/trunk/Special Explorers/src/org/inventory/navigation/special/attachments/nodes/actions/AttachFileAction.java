/*
 * Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 * 
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.special.attachments.nodes.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Attaches a file to a file to an inventory object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AttachFileAction extends GenericObjectNodeAction implements Presenter.Popup {

    private static ImageIcon ICON = ImageUtilities.loadImageIcon("org/inventory/navigation/special/res/icon_attach_file.png", false);
    
    public AttachFileAction() {
        putValue(NAME, I18N.gm("attach_file"));
        putValue(SMALL_ICON, ICON);
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] { Constants.CLASS_INVENTORYOBJECT };
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_ATTACHMENTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        JTextField txtTags = new JTextField();
        final JButton btnAttachment = new JButton(I18N.gm("click_to_select_a_file"));
        final JFileChooser globalFileChooser = Utils.getGlobalFileChooser();
        globalFileChooser.setSelectedFile(null);
        
        
        btnAttachment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                globalFileChooser.setDialogTitle("Select the File to Attach");
                int option = globalFileChooser.showDialog(null, "Attach");

                if (option == JFileChooser.APPROVE_OPTION) 
                    btnAttachment.setText(globalFileChooser.getSelectedFile().getName().length() > 25 ? 
                            globalFileChooser.getSelectedFile().getName().substring(0, 24) + "..." : globalFileChooser.getSelectedFile().getName());
            }
        });
                
        JComplexDialogPanel pnlNewAttachment = new JComplexDialogPanel(new String[] { 
                I18N.gm("tags"), I18N.gm("file")}, new JComponent[] { txtTags, btnAttachment });
        
        
        if (JOptionPane.showConfirmDialog(null, pnlNewAttachment, I18N.gm("attach_file"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                File selectedFile = globalFileChooser.getSelectedFile();
                
                if (globalFileChooser.getSelectedFile() == null)
                    JOptionPane.showMessageDialog(null, I18N.gm("you_have_to_select_a_file"), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                else {
                    if (CommunicationsStub.getInstance().attachFileToObject(selectedFile.getName(), txtTags.getText(), 
                            Utils.getByteArrayFromFile(selectedFile), selectedObjects.get(0).getClassName(), selectedObjects.get(0).getId()) != null)
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, I18N.gm("file_attached_successfully"));
                    else
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }

            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
        
        
        
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
}
