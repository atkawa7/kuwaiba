/**
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
 */
package org.inventory.core.templates.layouts.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.ShowDeviceLayoutTopComponent;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Action used to show how a given device looks like (in the real world)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.OPEN_VIEW)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowDeviceLayoutView extends GenericObjectNodeAction {
    
    public ShowDeviceLayoutView() {
        putValue(NAME, "Device Layout");
    }
    
    @Override
    public boolean isEnabled() {
        Lookup.Result<? extends ObjectNode> selectedObjectNode = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class);
        
        if (selectedObjectNode == null)
            return false;
        
        boolean hasModel = true;
        selectedObjects = new ArrayList();
                
        for (ObjectNode selectedNode : selectedObjectNode.allInstances()) {
            
            LocalObjectLight lol = selectedNode.getLookup().lookup(LocalObjectLight.class);
            
            LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(lol.getClassName(), false);
            
            if (!lcm.hasAttribute("model")) //NOI18N
                hasModel = false;
            
            selectedObjects.add(selectedNode.getObject());
        }
        
        return !selectedObjects.isEmpty() && hasModel;
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {        
        LocalClassMetadata customShapeClass = CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_CUSTOMSHAPE, false); //NOI18N
        if (customShapeClass == null) {
            JOptionPane.showMessageDialog(null, "This database seems outdated. Contact your administrator to apply the necessary patches to use the Device Layout feature", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        for (LocalObjectLight lol : selectedObjects) {
            ShowDeviceLayoutTopComponent devicelayoutView = ((ShowDeviceLayoutTopComponent) WindowManager.
                getDefault().findTopComponent("ShowDeviceLayoutTopComponent_" + lol.getId())); //NOI18N

            if (devicelayoutView == null) {
                devicelayoutView = new ShowDeviceLayoutTopComponent(lol);
                devicelayoutView.open();
            } else {
                if (devicelayoutView.isOpened())
                    devicelayoutView.requestAttention(true);
                else  //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
                    devicelayoutView.open();
            }
            devicelayoutView.requestActive();
        }
    }

    @Override
    public String[] appliesTo() {
        return new String [] {"GenericCommunicationsElement", "GenericDistributionFrame", "GenericBoard"};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
