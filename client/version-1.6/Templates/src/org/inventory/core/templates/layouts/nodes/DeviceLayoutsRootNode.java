/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts.nodes;

import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.nodes.AbstractNode;

/**
 * Root node of nodes of device layouts
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceLayoutsRootNode extends AbstractNode {

    public DeviceLayoutsRootNode(List<LocalObjectLight> devices) {
        super(new DeviceLayoutChildren(devices));
    }    
}
