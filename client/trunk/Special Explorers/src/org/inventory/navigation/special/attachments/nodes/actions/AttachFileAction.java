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
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Attaches a file to a file to an inventory object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AttachFileAction extends GenericObjectNodeAction {

    public AttachFileAction() {
        putValue(NAME, "Attach File...");
    }

    @Override
    public String[] getValidators() {
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
        
        JFileChooser globalFileChooser = Utils.getGlobalFileChooser();
        globalFileChooser.setDialogTitle("Select the File to Attach");
        int option = globalFileChooser.showDialog(null, "Attach");
        
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = globalFileChooser.getSelectedFile();
                if (CommunicationsStub.getInstance().attachFileToObject(selectedFile.getName(), "", 
                        Utils.getByteArrayFromFile(selectedFile), selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid()) != null)
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The file was attached successfully");
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                
            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
    }
    
}
