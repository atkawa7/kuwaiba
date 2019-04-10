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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Class used to represent a label 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ResizableLabelWidget extends Widget {
    
    private String label;
    
    public ResizableLabelWidget (Scene scene) {
        this (scene, null);
        setCheckClipping (true);
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    public ResizableLabelWidget (Scene scene, String label) {
        super (scene);
        this.label = label;
    }
    
    public String getLabel () {
        return label;
    }
    
    public void setLabel (String label) {
        this.label = label;
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
        // Thanks to https://coderanch.com/t/344978/java/scaling-font-resizing-dragging
        // for provide the sample
        super.paintWidget();
        Graphics2D g2d = getGraphics();
        g2d.setColor(getForeground());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font font = g2d.getFont(); 
        g2d.setFont(font);
        FontRenderContext frc = g2d.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(getLabel(), frc);

        Insets insets = getBorder().getInsets();

        float height = metrics.getAscent() + metrics.getDescent();
        double width = font.getStringBounds(getLabel(), frc).getWidth();
        int w = getPreferredSize().width - insets.left - insets.right;
        int h = getPreferredSize().height - insets.top - insets.bottom;
        
        AffineTransform previousTransform = g2d.getTransform ();
        g2d.translate (0, 0);
        
        g2d.scale(w/width, h/height);
        g2d.drawString(getLabel(), 0, metrics.getAscent());
        
        g2d.setTransform(previousTransform);
    }
    
    public void fontResize() {
        Font font = getFont();
        int size = font.getSize();
                
        font.deriveFont(size);
    }
}
