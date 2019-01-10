/*
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.classhierarchy.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;

/**
 * Action to refresh a class node information
 * @author Adrian Martinez Molina {@literal <charles.bedon@kuwaiba.org>}
 */
public class RefreshClassMetadataAction extends GenericInventoryAction {
    ClassMetadataNode node;

    public RefreshClassMetadataAction(ClassMetadataNode node) {
        this.node = node;
        putValue(NAME, I18N.gm("refresh"));
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        node.refresh();
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }
}
