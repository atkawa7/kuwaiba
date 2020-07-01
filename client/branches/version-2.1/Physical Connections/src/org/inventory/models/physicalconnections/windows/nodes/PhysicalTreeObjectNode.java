/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.models.physicalconnections.windows.nodes;

import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * A node in the Physical Tree Top Component
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PhysicalTreeObjectNode extends ObjectNode {
    private final HashMap<LocalObjectLight, List<LocalObjectLight>> tree;
    private final LocalObjectLight lol;    
    private final boolean isRoot;
    public PhysicalTreeObjectNode(LocalObjectLight lol, HashMap<LocalObjectLight, List<LocalObjectLight>> tree, boolean isRoot) {
        super(lol);
        this.tree = tree;
        this.lol = lol;
        this.isRoot = isRoot;
        setChildren(new PhysicalTreeObjectNodeChildren());
    }
    
    @Override
    public String getHtmlDisplayName() {
        LocalObjectLight parent = CommunicationsStub.getInstance().getParent(lol.getClassName(), lol.getId());
        return "<font color='" + (!isRoot ? "FFFFFF" : "FF0000") + "'>" + parent.getName() + " : " + lol + "</font>"; // NOI18N
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {};
    }
    public class PhysicalTreeObjectNodeChildren extends Children.Keys<LocalObjectLight> {
        @Override
        public void addNotify() {
            if (tree.containsKey(lol))
                setKeys(tree.get(lol).toArray(new LocalObjectLight[0]));
            else
                setKeys(new LocalObjectLight[0]);
        }
        @Override
        protected Node[] createNodes(LocalObjectLight lol) {
            return new Node[]{new PhysicalTreeObjectNode(lol, tree, false)};
        }
    }
}
