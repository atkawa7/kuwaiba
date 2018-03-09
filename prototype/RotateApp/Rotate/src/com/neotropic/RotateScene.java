/*
 * Copyright (c) 2018 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
 */
package com.neotropic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author johnyortega
 */
public class RotateScene extends GraphScene<RectangleRotater, String> {
    Random random = new Random();
    int i = 1000;
    private final Widget rectangleLayer;
    private final Widget textLayer;
    
    public RotateScene() {
        rectangleLayer = new LayerWidget(this);
        textLayer = new LayerWidget(this);
        addChild(rectangleLayer);
        addChild(textLayer);
    }
    
    @Override
    protected Widget attachNodeWidget(RectangleRotater node) {
        if (node.getText().isEmpty()) {
            Widget widget = new Widget(this);
            widget.setOpaque(true);
            widget.setBackground(Color.BLUE);
            widget.setPreferredBounds(new Rectangle(node.x, node.y, node.width, node.height));
            rectangleLayer.addChild(widget);            
            return widget;
        }
            
        ResizableLabelWidget widget = new ResizableLabelWidget(this, node.getText(), node.getAngle());
        widget.setBackground(Color.RED);
        widget.setOpaque(true);
        widget.setPreferredSize(new Dimension(node.height, node.width));
        widget.setPreferredLocation(new Point(node.x, node.y));
        
        this.repaint();
        this.validate();
        
        textLayer.addChild(widget);
        
        return widget;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, RectangleRotater oldSourceNode, RectangleRotater sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, RectangleRotater oldTargetNode, RectangleRotater targetNode) {
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        return null;
    }
    
}