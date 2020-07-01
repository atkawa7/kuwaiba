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
package org.inventory.core.templates.layouts.providers;

import java.awt.Color;
import java.awt.Point;
import java.util.Collections;
import org.inventory.core.services.api.notifications.StatusUtil;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Shape widget select provider
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeSelectProvider implements SelectProvider {
    
    public ShapeSelectProvider() {        
    }

    @Override
    public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return false;
    }

    @Override
    public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return ((ObjectScene) widget.getScene()).findObject (widget) != null;
    }

    @Override
    public void select (Widget widget, Point localLocation, boolean invertSelection) {                  
        DeviceLayoutScene scene = ((DeviceLayoutScene) widget.getScene());

        Object object = scene.findObject (widget);

        scene.setFocusedObject (object);
        if (object != null) {
            if (! invertSelection  &&  scene.getSelectedObjects ().contains (object))
                return;
            scene.userSelectionSuggested (Collections.singleton (object), invertSelection);
            
            for (Shape shape : scene.getNodes()) {
                Widget shapeWidget = ((DeviceLayoutScene) widget.getScene()).findWidget(shape);
                if (shapeWidget != null && !widget.equals(shapeWidget)) {
                    if (shape.isOpaque()) {
                        shapeWidget.setBorder(BorderFactory.createLineBorder(shape.getBorderWidth(), shape.getBorderColor()));                        
                    } else {
                        shapeWidget.setBorder(BorderFactory.createOpaqueBorder(
                            shape.getBorderWidth(), shape.getBorderWidth(), 
                            shape.getBorderWidth(), shape.getBorderWidth()));
                    }
                }                                                
            }
            widget.setBorder(BorderFactory.createResizeBorder(Shape.DEFAULT_BORDER_SIZE, Color.BLACK, true));
            
            double width = widget.getPreferredBounds().getWidth();
            double height = widget.getPreferredBounds().getHeight();
            StatusUtil.getInstance().setStatusText("Aspect ratio: " +  Math.round(width / height) + ":1");
        } else
            scene.userSelectionSuggested (Collections.emptySet (), invertSelection);
    }
}
