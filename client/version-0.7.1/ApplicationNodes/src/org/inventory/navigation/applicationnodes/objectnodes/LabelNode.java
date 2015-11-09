/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes;

import java.awt.Color;
import java.awt.Image;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * A node that represents only a label
 */
public class LabelNode extends AbstractNode {
    private final Image icon = Utils.createRectangleIcon(new Color(170, 212,0), 10, 10);
    public LabelNode(String label, LocalObjectLight[] children) {
        super (new Children.Array());
        setDisplayName(label);
        for (LocalObjectLight child : children)
            getChildren().add(new SpecialObjectNode[] {new SpecialObjectNode(child)});
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    
}