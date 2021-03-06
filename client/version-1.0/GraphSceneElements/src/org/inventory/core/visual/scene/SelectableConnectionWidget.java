/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A connection widget that can be selected and its wrapped object exposed via the property sheet
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class SelectableConnectionWidget extends ConnectionWidget {
    protected ObjectNode node;
    private Lookup lookup;
    
    public SelectableConnectionWidget(Scene scene, LocalObjectLight businessObject) {
        super(scene);
        node = new ObjectNode(businessObject);
        lookup = Lookups.singleton(node);
    }
    
    public ObjectNode getNode() {
        return node;
    }
    
    public void setNode(ObjectNode node) {
        this.node = node;
        lookup = Lookups.singleton(node);
        setToolTipText(node.getObject().toString());
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
