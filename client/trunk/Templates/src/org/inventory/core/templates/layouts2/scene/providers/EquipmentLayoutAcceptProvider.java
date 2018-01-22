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
package org.inventory.core.templates.layouts2.scene.providers;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.inventory.core.templates.layouts.lookup.SharedContentLookup;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.model.ShapeFactory;
import org.inventory.core.templates.layouts2.scene.EquipmentLayoutScene;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class EquipmentLayoutAcceptProvider implements AcceptProvider {

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable t) {
        if (!t.isDataFlavorSupported(Shape.DATA_FLAVOR))
            return ConnectorState.REJECT;
        return ConnectorState.ACCEPT;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable t) {
        Shape shapeTransferred;
        try {
            shapeTransferred = (Shape) t.getTransferData(Shape.DATA_FLAVOR);
        } catch (UnsupportedFlavorException | IOException ex) {
            return;
        }
        EquipmentLayoutScene scene;
        if (widget.getScene() instanceof EquipmentLayoutScene) {
            scene = (EquipmentLayoutScene) widget.getScene();
            
            Shape shape = ShapeFactory.getInstance().getShape(shapeTransferred.getShapeType());
            if (shape == null) {
                if (shapeTransferred instanceof CustomShape) {
                    shape = ShapeFactory.getInstance().getCustomShape(((CustomShape) shapeTransferred).getListItem());
                    shape.setWidth(-1);
                    shape.setHeight(-1);
                }
                else
                    throw new UnsupportedOperationException(String.format("%s not supported yet", shapeTransferred.getClass()));
            } else {
                shape.setWidth(Shape.DEFAULT_WITH);
                shape.setHeight(Shape.DEFAULT_HEIGHT);
            }
            shape.setX(point.x);
            shape.setY(point.y);
            
            
            Widget newWidget = scene.addNode(shape);
            if (newWidget != null) {
                if (newWidget instanceof SharedContentLookup)
                    ((SharedContentLookup) newWidget).fixLookup();
            }
        }
    }
        
}
