/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service class for this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalPathScene extends GraphScene<LocalObjectLight, LocalObjectLight> {
    private LayerWidget nodesLayer;
    private LayerWidget edgesLayer;
    private Router router;

    public PhysicalPathScene() {       
        nodesLayer = new LayerWidget(this);
        edgesLayer = new LayerWidget(this);
        router = RouterFactory.createOrthogonalSearchRouter(nodesLayer);
        nodesLayer.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 50));
        addChild(nodesLayer);
        addChild(edgesLayer);
    }
    
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Color randomColor = ObjectBoxWidget.colorPalette[new Random().nextInt(12)];
        Widget widget = new ObjectBoxWidget(this, node, randomColor);
        widget.repaint();
        widget.revalidate();
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        SimpleObjectConnectionWidget widget = new SimpleObjectConnectionWidget(this, edge, Color.BLUE);
        widget.setStroke(new BasicStroke(2));
        edgesLayer.addChild(widget);
        return widget;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
    }
    
    public void addRootWidget (Widget widget){
        widget.getActions().addAction(ActionFactory.createMoveAction());
        nodesLayer.addChild(widget);
    }

    public void clear() {
        List<LocalObjectLight> clonedNodes = new ArrayList<LocalObjectLight>(getNodes());
        List<LocalObjectLight> clonedEdges = new ArrayList<LocalObjectLight>(getEdges());
        
        for(LocalObjectLight lol : clonedNodes)
            removeNode(lol);
        for(LocalObjectLight lol : clonedEdges)
            removeEdge(lol);

        nodesLayer.removeChildren();
        edgesLayer.removeChildren();
    }

    public Router getRouter() {
        return router;
    }
    
    public void organizeNodes() {
        int x = 10;
        for (Widget child : nodesLayer.getChildren()){
            child.resolveBounds (new Point (x, 10), new Rectangle (child.getPreferredBounds().x, 
                    child.getPreferredBounds().y, child.getPreferredBounds().width, child.getPreferredBounds().height));
            x += child.getPreferredBounds().width + 50;
        }
    }
}
