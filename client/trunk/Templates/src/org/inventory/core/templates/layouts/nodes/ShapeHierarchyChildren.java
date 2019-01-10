/**
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

import java.util.ArrayList;
import java.util.List;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeHierarchyChildren extends Children.Keys {
    private final DeviceLayoutScene scene;
    private final Shape rootShape;
    
    public ShapeHierarchyChildren(Shape rootShape, DeviceLayoutScene scene) {
        this.scene = scene;
        this.rootShape = rootShape;
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        Widget rootWidget = scene.findWidget(rootShape);
        if (rootWidget == null)
            return;        
        
        List<Shape> children = new ArrayList<>();
        
        for (Widget child : rootWidget.getChildren()) {
            Shape shape = (Shape) scene.findObject(child);
            if (shape == null)
                continue;
            
            shape.setUrlIcon("org/inventory/core/templates/res/shape_hierarchy_icon.png");
            children.add(shape);
        }
        setKeys(children);
    }

    @Override
    protected Node[] createNodes(Object key) {
        return new Node[] {new ShapeNode((Shape) key, new ShapeHierarchyChildren((Shape) key, scene))};
    }
}
