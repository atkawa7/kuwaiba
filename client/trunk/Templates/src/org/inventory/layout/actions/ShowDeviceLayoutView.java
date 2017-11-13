/**
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
 */
package org.inventory.layout.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.layout.ShowDeviceLayoutTopComponent;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Action used to show how a given device looks like (in the real world)
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowDeviceLayoutView extends GenericObjectNodeAction {
    
    public ShowDeviceLayoutView() {
        putValue(NAME, "Show Device Layout");
    }

    @Override
    public String getValidator() {
        return Constants.VALIDATOR_GENERIC_COMMUNICATIONS_ELEMENT;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_MODELS_LAYOUTS, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {        
        LocalClassMetadata equipmentModelClass = CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_EQUIPMENTMODEL, true); //NOI18N
        if (equipmentModelClass == null) {
            JOptionPane.showMessageDialog(null, "This database seems outdated. Contact your administrator to apply the necessary patches to run the Show Device Layout action", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        for (LocalObjectLight lol : selectedObjects) {
            ShowDeviceLayoutTopComponent modelLayoutView = ((ShowDeviceLayoutTopComponent) WindowManager.
                getDefault().findTopComponent("ShowDeviceLayoutTopComponent_" + lol.getOid())); //NOI18N
            
            if (modelLayoutView == null) {
                modelLayoutView = new ShowDeviceLayoutTopComponent(lol);
                modelLayoutView.open();
            } else {
                if (modelLayoutView.isOpened())
                    modelLayoutView.requestAttention(true);
                else  //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
                    modelLayoutView.open();
                
            }
            modelLayoutView.requestActive();
        }
    }
    
}
