/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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

import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts2.EquipmentLayoutPalette;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Shapes set for a category in the palette
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShapeChildren extends Children.Keys {
    String category;
    
    public ShapeChildren(String category) {
        this.category = category;
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(EquipmentLayoutPalette.shapes.get(category));
    }
    
    @Override
    protected Node[] createNodes(Object key) {
        return new Node[] {new ShapeNode((Shape) key)};
    }
}
