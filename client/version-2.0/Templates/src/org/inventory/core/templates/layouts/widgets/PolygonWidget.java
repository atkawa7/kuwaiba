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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PolygonWidget extends Widget {
    private Color outlineColor;
    private Color interiorColor;
    
    public PolygonWidget(Scene scene) {
        super(scene);
    }
    
    public Color getOutlineColor() {
        return outlineColor;        
    }
    
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;     
    }
    
    public Color getInteriorColor() {
        return interiorColor;
    }
    
    public void setInteriorColor(Color interiorColor) {
        this.interiorColor = interiorColor;
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
        
        AffineTransform previousTransform = g2d.getTransform ();
        g2d.translate (0, 0);
                
        Insets insets = getBorder().getInsets();        
        int w = getPreferredSize().width - insets.left - insets.right;
        int h = getPreferredSize().height - insets.top - insets.bottom;
                
        int xpoints[] = {0, w/ 2, w};
        int ypoints[] = {h, 0, h};
        int npoints = 3;
                
        Polygon polygon = new Polygon(xpoints, ypoints, npoints);
                        
        g2d.setColor(getInteriorColor());
        g2d.fill(polygon);
        
        g2d.setColor(getOutlineColor());        
        g2d.drawPolygon(polygon);
        
        g2d.setTransform(previousTransform);
    }
}
