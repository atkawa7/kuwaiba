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
package org.inventory.navigation.special.relationships.nodes;

import java.awt.Color;
import java.awt.Image;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

/**
 * This node represents a special relationship of the parent node. Its children are the related objects
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RelationshipNode extends AbstractNode {
    private static final Image DEFAULT_ICON = Utils.createRectangleIcon(new Color(170, 212,0), 10, 10);
    private Image icon;
    
    public RelationshipNode(String relationshipName, LocalObjectLight[] children) {
        super (new RelationshipNodeChildren(children));
        setDisplayName(relationshipName);
        icon = DEFAULT_ICON;
    }
    
    public RelationshipNode(String relationshipName, LocalObjectLight[] children, Color iconColor) {
        super (new RelationshipNodeChildren(children));
        setDisplayName(relationshipName);
        icon = Utils.createRectangleIcon(iconColor, 10, 10);
    }
    
    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    public static class RelationshipNodeChildren extends AbstractChildren {

        public RelationshipNodeChildren(LocalObjectLight[] children) {
            setKeys(children);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] { new SpecialRelatedObjectNode(key) };
        }

        @Override
        public void addNotify() {}
    }
}