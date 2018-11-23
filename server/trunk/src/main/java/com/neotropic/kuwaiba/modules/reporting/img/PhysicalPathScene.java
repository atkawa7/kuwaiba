/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package com.neotropic.kuwaiba.modules.reporting.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service class for this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PhysicalPathScene extends GraphScene<BusinessObjectLight, BusinessObjectLight>{
    public static final int X_OFFSET = 50;
    private Router router;
    /**
     * Used to hold the nodes
     */
    protected LayerWidget nodeLayer;
    /**
     * Used to hold the connections
     */
    protected LayerWidget edgeLayer;


    public PhysicalPathScene() {       
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        router = RouterFactory.createOrthogonalSearchRouter(nodeLayer);
        nodeLayer.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 50));
        addChild(nodeLayer);
        addChild(edgeLayer);
    }
    
    @Override
    protected Widget attachNodeWidget(BusinessObjectLight node) {
        Color randomColor = ObjectBoxWidget.colorPalette[new Random().nextInt(12)];
        Widget widget = new ObjectBoxWidget(this, node, randomColor);
        widget.getActions().addAction(createSelectAction());
        widget.repaint();
        widget.revalidate();
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(BusinessObjectLight edge) {
        ConnectionWidget widget = new ConnectionWidget(this);
        widget.setForeground(Color.BLUE);
        widget.setStroke(new BasicStroke(1));
        widget.setRouter(router);
        widget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        edgeLayer.addChild(widget);
        return widget;
    }
    
    public void addRootWidget (Widget widget){
        widget.getActions().addAction(ActionFactory.createMoveAction());
        nodeLayer.addChild(widget);
    }
 
    public void organizeNodes() {
        int x = 10;
        for (Widget child : nodeLayer.getChildren()){
            child.resolveBounds (new Point (x, 10), new Rectangle (child.getPreferredBounds().x, 
                    child.getPreferredBounds().y, child.getPreferredBounds().width, child.getPreferredBounds().height));
            x += child.getPreferredBounds().width + X_OFFSET;
        }
    }

    @Override
    protected void attachEdgeSourceAnchor(BusinessObjectLight edge, BusinessObjectLight oldSourceNode, BusinessObjectLight newSourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(BusinessObjectLight edge, BusinessObjectLight oldTargetNode, BusinessObjectLight newTargetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
}
