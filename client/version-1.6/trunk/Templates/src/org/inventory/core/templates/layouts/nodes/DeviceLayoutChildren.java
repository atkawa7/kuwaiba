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

import java.util.Collections;
import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.templates.layouts.DeviceLayoutsService;
import org.inventory.core.templates.nodes.TemplateElementNode;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.Node;

/**
 * Set of nodes of device layout
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceLayoutChildren extends AbstractChildren {
    
    public DeviceLayoutChildren(List<LocalObjectLight> devices) {
        Collections.sort(devices);
        setKeys(devices);
    }

    @Override
    public void addNotify() {
        DeviceLayoutsService service = new DeviceLayoutsService();
        List<LocalObjectLight> devices = service.getDevices();
        
        if (devices == null)
            setKeys(Collections.EMPTY_SET);
        else {
            Collections.sort(devices);
            setKeys(devices);
        }
    }

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new TemplateElementNode [] {new TemplateElementNode(key)};
    }
    
}
