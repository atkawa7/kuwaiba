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

package org.kuwaiba.management.services.nodes;

import org.inventory.communications.core.LocalObjectLight;
import org.openide.nodes.AbstractNode;

/**
 *
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class MyNode extends AbstractNode{

    private LocalObjectLight object;

    public MyNode(LocalObjectLight object) {
        super(new CustomerChildren(object));
        this.object = object;
    }
    
    
    
    
}
