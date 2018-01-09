/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This actions verifies that all connections inside a given object (not recursively) are well formed and have the proper parent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class CheckConnectionsIntegrityAction extends GenericObjectNodeAction {

    public CheckConnectionsIntegrityAction() {
        putValue(NAME, I18N.gm("check_connections_integrity"));
    }
    
    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] { "ViewableObject" };
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> specialChildren = CommunicationsStub.getInstance().getObjectSpecialChildren(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid());
        
        
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}
