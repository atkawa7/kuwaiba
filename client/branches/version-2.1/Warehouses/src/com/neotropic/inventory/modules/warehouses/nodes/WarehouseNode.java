/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.warehouses.nodes;

import com.neotropic.inventory.modules.warehouses.nodes.actions.NewSparePoolAction;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WarehouseNode extends ObjectNode {
    
    public WarehouseNode(LocalObjectLight lol) {
        super(lol);
        setChildren(new WarehouseChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {NewSparePoolAction.getInstance()};
    }
}
