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
package org.inventory.updates.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.updates.Installer;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to register an update center catalog file
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ActionID(
        category = "Tools/Administrative",
        id = "org.inventory.updates.actions.RegisterUpdateCenterAction"
)
@ActionRegistration(
        displayName = "#CTL_RegisterUpdateCenterAction"
)
@ActionReference(path = "Menu/Tools", position = 3333)
@Messages("CTL_RegisterUpdateCenterAction=Register Update Center")
public final class RegisterUpdateCenterAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField txtUpdateCenterURL = new JTextField();
        txtUpdateCenterURL.setName("txtUpdateCenterURL");
        txtUpdateCenterURL.setColumns(10);
        
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false);
        
        UpdateUnitProvider updateUnitProvider = null;
        String oldUpdateCenterURL = null;
        
        for (UpdateUnitProvider provider : providers) {
            if (provider.getDisplayName() == null)
                continue;
            
            String displayName = ResourceBundle.getBundle("org/inventory/updates/Bundle")
                .getString("Services/AutoupdateType/org_inventory_updates_update_center.instance");
            
            if (provider.getDisplayName().equals(displayName)) {
                updateUnitProvider = provider;
                oldUpdateCenterURL = provider.getProviderURL().toString();
                txtUpdateCenterURL.setText(oldUpdateCenterURL);
            }
        }                
        JComplexDialogPanel registerUCDialog = new JComplexDialogPanel(new String[] {"Update Center URL"}, new JComponent[] {txtUpdateCenterURL});
        
        if (JOptionPane.showConfirmDialog(null, registerUCDialog, "Register Update Center", JOptionPane.OK_CANCEL_OPTION) 
            == JOptionPane.OK_OPTION) {
            
            String newUpdateCenterURL = ((JTextField) registerUCDialog.getComponent("txtUpdateCenterURL")).getText();
            
            if (updateUnitProvider != null) {
                try {
                    URL url = new URL(newUpdateCenterURL);
                    updateUnitProvider.setProviderURL(url);
                    
                    try {
                        updateUnitProvider.refresh(null, true);
                        
                        if (updateUnitProvider.getUpdateUnits() == null || updateUnitProvider.getUpdateUnits().isEmpty()) {
                            
                            NotificationUtil.getInstance().showSimplePopup("Error",
                                NotificationUtil.ERROR_MESSAGE,
                                String.format("The URL specified %s is not an update center", newUpdateCenterURL));
                            return;
                        }
                        Installer.runUpdate();
                                                
                    } catch (IOException ex) {
                        NotificationUtil.getInstance().showSimplePopup("Warning", 
                            NotificationUtil.WARNING_MESSAGE, 
                            "Update Center could not be reached, please contact your administrator");
                    }
                } catch (MalformedURLException ex) {
                    NotificationUtil.getInstance().showSimplePopup("Error",
                        NotificationUtil.ERROR_MESSAGE,
                        String.format("Malformed URL %s", newUpdateCenterURL));
                }
            }
        }
    }
}
