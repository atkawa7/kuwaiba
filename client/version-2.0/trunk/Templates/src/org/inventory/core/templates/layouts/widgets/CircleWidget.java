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
package org.inventory.core.templates.layouts.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CircleWidget extends Widget {
    private Color ellipseColor = Color.BLACK;
    private Color ovalColor = Color.BLACK;
    
    public CircleWidget(Scene scene) {
        super(scene);
    }
    
    public Color getEllipseColor() {
        return ellipseColor;
    }
    
    public void setEllipseColor(Color ellipseColor) {
        this.ellipseColor = ellipseColor;        
    }
    
    public Color getOvalColor() {
        return ovalColor;
    }
    
    public void setOvalColor(Color ovalColor) {
        this.ovalColor = ovalColor;
    }
    
    @Override
    protected Rectangle calculateClientArea() {
        if (getPreferredSize() == null || getBorder() == null)
            return new Rectangle();
        
        Dimension size = getPreferredSize();
        Insets insets = getBorder().getInsets();

        Rectangle rect = new Rectangle (0, 0, 
            size.width - insets.left - insets.right, 
            size.height - insets.top - insets.bottom);
        return rect;
    }

    @Override
    protected void paintWidget() {
        super.paintWidget();
        Graphics2D g2d = getGraphics(); 
        
        g2d.setColor(getEllipseColor());
        Dimension size = getPreferredSize();
        if (size == null)
            return;
        
        AffineTransform previousTransform = g2d.getTransform ();
        g2d.translate (0, 0);
        
        Insets insets = getBorder().getInsets();

        Ellipse2D elipse2D = new Ellipse2D.Float (0, 0, 
            size.width - insets.left - insets.right, 
            size.height - insets.top - insets.bottom);        
        g2d.fill(elipse2D);
        
        
        g2d.setColor(getOvalColor());
        g2d.drawOval(0, 0,
            size.width - insets.left - insets.right, 
            size.height - insets.top - insets.bottom);
        
        g2d.setTransform(previousTransform);
    }
}
