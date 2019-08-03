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
package org.inventory.core.templates.layouts.widgets;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.templates.layouts.model.ContainerShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Shape widget used to contain another shapes widget
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ContainerShapeWidget extends SelectableShapeWidget {
    private List<Shape> shapesSet;
    
    public ContainerShapeWidget(Scene scene, ContainerShape shape) {
        super(scene, shape);
        setOpaque(false);
    }
    
    public ContainerShapeWidget(Scene scene, CustomShape shape) {
        super(scene, shape);
        setOpaque(false);
    }
    
    public List<Shape> getShapesSet() {
        return shapesSet == null ? shapesSet = new ArrayList() : shapesSet;
    }
    
    public void setShapesSet(List<Shape> shapesSet) {
        this.shapesSet = shapesSet;
    }
    
    public boolean isCustomShape() {
        return lookup.lookup(Shape.class) instanceof CustomShape;
    }
    
    public void clearShapesSet() {
        if (getScene() instanceof DeviceLayoutScene) {
            DeviceLayoutScene scene = (DeviceLayoutScene) getScene();
            
            for (Shape shape : shapesSet) {
                Widget widget = scene.findWidget(shape);
                if (widget == null)
                    continue;
                                
                if (widget instanceof ContainerShapeWidget)
                    ((ContainerShapeWidget) widget).clearShapesSet();
                
                shape.removeAllPropertyChangeListeners();
                scene.removeNode(shape);
                
                scene.validate();
                scene.paint();
                
                scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape deleted"));
            }
        }
    }    
}
