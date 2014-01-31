/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
 */
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.windows.ServicesFrame;
import org.openide.util.Lookup;

/**
 * This action allows the user relate the current object to a service as a resource
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RelateToServiceAction extends AbstractAction {
    private LocalObjectLight element;

    public RelateToServiceAction(LocalObjectLight element) {
        this.element = element;
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_RELATE_TO_SERVICE"));
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight[] services = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICSERVICE);
        if (services ==  null)
            nu.showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        else{
            ServicesFrame frame = new ServicesFrame(element, services);
            frame.setVisible(true);
        }
    }
    
}
