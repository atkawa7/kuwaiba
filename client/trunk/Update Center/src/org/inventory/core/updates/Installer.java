/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.updates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.updates.windows.UpdateCenterOptionsDialog;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * A Module Installer to execute the auto update and install of modules
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                RequestProcessor.getDefault().post(runAutoUpdate, 1000);
            }
        });
    }
    
    public static void runUpdate() {
        runAutoUpdate.run();
    }
    
    private static final Runnable runAutoUpdate = new Runnable() {
        
        private final List<UpdateElement> modulesForInstall = new ArrayList<>();
        private final List<UpdateElement> modulesForUpdate = new ArrayList<>();
        private boolean restart = false;
        boolean isUpdateCenterRegistered = true;
                
        @Override
        public void run() {
            List<UpdateUnitProvider> providers = UpdateUnitProviderFactory
                .getDefault().getUpdateUnitProviders(false);
            
            for (UpdateUnitProvider provider : providers) {
                
                String displayName = ResourceBundle.getBundle("org/inventory/core/updates/Bundle")
                    .getString("Services/AutoupdateType/org_inventory_updates_update_center.instance");
                
                if (provider.getDisplayName().equals(displayName)) {
                    
                    try {
                        provider.refresh(null, true);
                        isUpdateCenterRegistered = true;
                    } catch (IOException ex) {
                        Preferences preferences = Preferences.userRoot()
                            .node(UpdateCenterOptionsDialog.class.getName());
                        
                        Boolean isSelected = preferences.getBoolean(UpdateCenterOptionsDialog.PREFERENCE_KEY_UC_WARNINGS, false);
                        
                        if (isSelected) {
                            
                            NotificationUtil.getInstance().showSimplePopup("Warning", 
                                NotificationUtil.WARNING_MESSAGE, 
                                "Update Center could not be reached, please contact your administrator");
                        }
                    }
                    break;
                }
            }
            if (isUpdateCenterRegistered) {
                setModules();
                OperationContainer<InstallSupport> containerForInstall = 
                    getContainer(OperationContainer.createForInstall(), modulesForInstall);
                installModules(containerForInstall);
                OperationContainer<InstallSupport> containerForUpdate = 
                    getContainer(OperationContainer.createForUpdate(), modulesForUpdate);
                installModules(containerForUpdate);
            }
        }
        
        private void setModules() {
            try {
                List<UpdateUnit> updateUnits = UpdateManager.getDefault().getUpdateUnits();

                for (UpdateUnit updateUnit : updateUnits) {
                    if (updateUnit.getInstalled() == null) {
                        modulesForInstall.add(updateUnit.getAvailableUpdates().get(0)); // add module with highest version
                    } else {
                        if (!updateUnit.getAvailableUpdates().isEmpty()) 
                            modulesForUpdate.add(updateUnit.getAvailableUpdates().get(0)); // add module with highest version

                    }

                }
            } catch (Exception ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                        NotificationUtil.WARNING_MESSAGE, String.format("A problem was found retrieving the update descriptor: %s", ex.getMessage()));
            }
        }
        
        private OperationContainer<InstallSupport> getContainer(
            OperationContainer<InstallSupport> container, List<UpdateElement> modules) {
            
            for (UpdateElement module : modules) {
                if(container.canBeAdded(module.getUpdateUnit(), module)) {
                    OperationInfo<InstallSupport> operationInfo = container.add(module);
                    
                    if (operationInfo != null)
                        container.add(operationInfo.getRequiredElements());
                }
            }
            return container;
        }
        
        private void installModules(OperationContainer<InstallSupport> container) {
            InstallSupport installSupport = container.getSupport();
            if (installSupport != null) {
                try {
                    Validator validator = installSupport.doDownload(null, true, true);
                    InstallSupport.Installer installer = installSupport.doValidate(validator, null);
                    Restarter restarter = installSupport.doInstall(installer, null);
                    
                    if (restarter != null) {
                        installSupport.doRestartLater(restarter);
                        if (!restart) {
                            if (JOptionPane.showConfirmDialog(null, "Click OK to restart or Cancel to restart later", "Restart", JOptionPane.OK_CANCEL_OPTION) 
                                == JOptionPane.OK_OPTION) {
                                
                                restart = true;
                                RestartAction restartAction = new RestartAction(installSupport, restarter);
                                restartAction.actionPerformed(null);
                            }
                        }
                    }
                } catch (OperationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    };
    
    private static final class RestartAction implements ActionListener {

        private final InstallSupport installSupport;
        private final Restarter restarter;

        public RestartAction(InstallSupport installSupport, Restarter restarter) {
            this.installSupport = installSupport;
            this.restarter = restarter;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                installSupport.doRestart(restarter, null);
            } catch (OperationException ex) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, 
                    "Restart kuwiaba failed, try close kuwiaba and open it again");
            }
        }
    }
}