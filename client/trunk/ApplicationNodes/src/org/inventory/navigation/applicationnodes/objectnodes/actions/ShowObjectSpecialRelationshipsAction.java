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
import java.util.HashMap;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.windows.SpecialRelationshipsTopComponent;
import org.openide.util.Lookup;

/**
 * Gets the selected object special relationships
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ShowObjectSpecialRelationshipsAction extends AbstractAction{
    private LocalObjectLight object;

    public ShowObjectSpecialRelationshipsAction(LocalObjectLight object) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SHOW_OBJECT_SPECIAL_RELATIONSHIPS_ACTION"));
        this.object = object;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        HashMap<String, LocalObjectLight[]> relationships = CommunicationsStub.
                getInstance().getSpecialAttributes(object.getClassName(), object.getOid());
        if (relationships == null){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
            return;
        }
        SpecialRelationshipsTopComponent tc = new SpecialRelationshipsTopComponent(object, relationships);
        tc.open();
        tc.requestActive();
    }

}