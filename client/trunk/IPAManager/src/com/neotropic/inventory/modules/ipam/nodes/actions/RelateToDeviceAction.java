/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.ipam.nodes.actions;

import com.neotropic.inventory.modules.ipam.windows.DevicesFrame;
import com.neotropic.inventory.modules.ipam.nodes.IpNode;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class RelateToDeviceAction extends AbstractAction{
    
    public RelateToDeviceAction(IpNode node){
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELATE_IP"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight[] devices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        
        if (devices ==  null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else{
            Collection lookupResult = selectedNodes.allInstances();
            LocalObjectLight[] selectedObjects = new LocalObjectLight[lookupResult.size()];
            int i = 0;
            for (Iterator it = lookupResult.iterator(); it.hasNext();) {
                selectedObjects[i] = (LocalObjectLight)it.next();
                i++;
            }
            DevicesFrame frame = new DevicesFrame(selectedObjects, devices);
            frame.setVisible(true);
        }    
    }
}
