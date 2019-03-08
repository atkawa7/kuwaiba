/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.core.visual.scene;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Any widget that can be selected and its wrapped object exposed via the property sheet should implement this interface
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class SelectableNodeWidget extends Widget {
    private Lookup lookup;
    
    public SelectableNodeWidget(Scene scene, LocalObjectLight businessObject) {
        super(scene);
        //It's strange, but having in the lookup just the node won't work for classes expecting the enclosed business object to also be in the lookup (unlike BeanTreeViews)
        lookup = Lookups.fixed(new ObjectNode(businessObject), businessObject);
    }
 
    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
