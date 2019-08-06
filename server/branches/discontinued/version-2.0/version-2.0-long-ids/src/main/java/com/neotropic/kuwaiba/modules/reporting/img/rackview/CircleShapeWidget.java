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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.Scene;

/**
 * Selectable circle widget
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CircleShapeWidget extends CircleWidget implements PropertyChangeListener {
    
    public CircleShapeWidget(Scene scene, CircleShape circleShape) {
        super(scene);
        circleShape.addPropertyChangeListener(this);
        
        setBackground(circleShape.getColor());        
        setEllipseColor(circleShape.getEllipseColor());
        setOvalColor(circleShape.getOvalColor());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CircleShape.PROPERTY_ELLIPSE_COLOR.equals(evt.getPropertyName())) {
            setEllipseColor((Color) evt.getNewValue());
        } else if (CircleShape.PROPERTY_OVAL_COLOR.equals(evt.getPropertyName())) {
            setOvalColor((Color) evt.getNewValue());
        }
    }
}

