/**
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * Set of methods to manage the updates on shape nodes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeWidgetUtil {
    
    public static void makingVisibleChanges(Widget widget) {
        widget.getScene().setVisible(false);
        widget.getScene().revalidate();
        widget.getScene().setVisible(true);
        widget.getScene().revalidate();
    }
    
    public static void shapeToWidget(Shape sourceShape, Widget targetWidget, boolean revalidate) {
        if (sourceShape == null || targetWidget == null)
            return;
        targetWidget.setPreferredLocation(new Point(sourceShape.getX(), sourceShape.getY()));
        targetWidget.setPreferredBounds(new Rectangle(
            sourceShape.getBorderWidth(), sourceShape.getBorderWidth(), 
            sourceShape.getWidth(), sourceShape.getHeight()));
        targetWidget.setPreferredSize(new Dimension(sourceShape.getWidth(), sourceShape.getHeight()));
        targetWidget.setBackground(sourceShape.getColor());
        if (sourceShape.isOpaque())
            targetWidget.setBorder(BorderFactory.createLineBorder(sourceShape.getBorderWidth(), sourceShape.getBorderColor()));
        else {
            targetWidget.setBorder(BorderFactory.createOpaqueBorder(
                sourceShape.getBorderWidth(), sourceShape.getBorderWidth(), 
                sourceShape.getBorderWidth(), sourceShape.getBorderWidth()));
        }
        targetWidget.setOpaque(sourceShape.isOpaque());
        
        if (!revalidate)
            targetWidget.revalidate();
    }
}
