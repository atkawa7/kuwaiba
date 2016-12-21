/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.design.topology.scene;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Temporary widget used to represent a node in the topology designer module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectNodeWidget extends IconNodeWidget {

    private Lookup lookup;
    
    public ObjectNodeWidget(Scene scene, LocalObjectLight object) {
        super(scene);
        lookup = Lookups.singleton(new ObjectNode(object, true));
        getLabelWidget().setFont(null);
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
