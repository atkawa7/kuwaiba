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
package org.inventory.design.modelsLayouts.providers;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.model.RectangleShape;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.inventory.design.modelsLayouts.scene.widgets.LabelShapeWidget;
import org.inventory.design.modelsLayouts.scene.widgets.RectangleShapeWidget;
import org.inventory.design.modelsLayouts.scene.widgets.SelectableShapeWidget;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ModelLayoutAcceptProvider implements AcceptProvider {

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable t) {
        if (!t.isDataFlavorSupported(Shape.DATA_FLAVOR))
            return ConnectorState.REJECT;
        return ConnectorState.ACCEPT;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable t) {  
        try {  
            Widget newWidget = null;
            Shape shape = (Shape) t.getTransferData(Shape.DATA_FLAVOR);
            
            Shape newShape = null;
            if (shape instanceof LabelShape)
                newShape = new LabelShape();
            else
                newShape = new RectangleShape();
                                    
            if (widget instanceof ModelLayoutScene) {
                newShape.setParent(null);
                newWidget = ((ModelLayoutScene) widget).addNode(newShape);
            }
            else {
                Object parent = ((ModelLayoutScene) widget.getScene()).findObject(widget);
                
                if (parent != null && parent instanceof Shape) {
                    newShape.setParent((Shape) parent);
                    newWidget = ((ModelLayoutScene) widget.getScene()).addNode(newShape);
                }
            }
            
            if (newWidget != null) {
                newWidget.setPreferredLocation(point);
                ((ModelLayoutScene) widget).repaint();
                
                newShape.setX(point.x);
                newShape.setY(point.y);
                if (newShape instanceof LabelShape)
                    ((LabelShapeWidget) newWidget).fixLookup();
                if (newShape instanceof RectangleShape)
                    ((RectangleShapeWidget) newWidget).fixLookup();
            }
        } catch (Exception ex) {            
        }
    }
}
