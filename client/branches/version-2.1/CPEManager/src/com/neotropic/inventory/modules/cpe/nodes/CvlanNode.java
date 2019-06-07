/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.cpe.nodes;

import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Children;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CvlanNode extends ObjectNode {
    
    public CvlanNode(LocalObjectLight egvlan) {
        super(egvlan);
        setChildren(Children.LEAF);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
}
