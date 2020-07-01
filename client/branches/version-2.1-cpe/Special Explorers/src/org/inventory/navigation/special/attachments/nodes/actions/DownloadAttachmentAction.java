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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFileObject;
import org.inventory.communications.core.LocalFileObjectLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.special.attachments.nodes.FileObjectNode;
import org.openide.util.Utilities;

/**
 * Downloads a file associated to an inventory object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DownloadAttachmentAction extends GenericInventoryAction {

    public DownloadAttachmentAction() {
        putValue(NAME, I18N.gm("download_attachment"));
    }
    
    

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_ATTACHMENTS, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<? extends FileObjectNode> selectedFileObjectNodes = Utilities.actionsGlobalContext().lookupResult(FileObjectNode.class).allInstances();
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        for (FileObjectNode fileObjectNode : selectedFileObjectNodes) {
            LocalFileObjectLight selectedFileObject = fileObjectNode.getLookup().lookup(LocalFileObjectLight.class);
            LocalObjectLight inventoryObject = fileObjectNode.getParentNode().getLookup().lookup(LocalObjectLight.class);
            
            JFileChooser globalFileChooser = Utils.getGlobalFileChooser();
            globalFileChooser.setDialogTitle("Select a location to save the file");
            globalFileChooser.setSelectedFile(new File(selectedFileObject.getName()));

            int option = globalFileChooser.showSaveDialog(null);
            
            if (option == JFileChooser.APPROVE_OPTION) {
                LocalFileObject theFile = com.getFile(selectedFileObject.getFileOjectId(), inventoryObject.getClassName(), inventoryObject.getId());
                if (theFile != null) {
                    try (FileOutputStream fos = new FileOutputStream(globalFileChooser.getSelectedFile().getAbsolutePath())) {
                        fos.write(theFile.getFile());
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, I18N.gm("file_saved_successfully"));
                        if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
                            Desktop.getDesktop().open(globalFileChooser.getSelectedFile());
                        else
                            JOptionPane.showMessageDialog(null, I18N.gm("cant_open_file"), 
                                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                    }
                }
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }
    }
    
}
