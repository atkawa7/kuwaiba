/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.models.physicalconnections.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service class for this module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PhysicalTreeScene extends AbstractScene <LocalObjectLight, LocalObjectLight>{
    public static final int X_OFFSET = 50;
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private final CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(this);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
    private final CustomAddRemoveControlPointAction addRemoveControlPointAction =
            new CustomAddRemoveControlPointAction(this);
    
    private final Router router;
    
    public PhysicalTreeScene() {       
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        router = RouterFactory.createFreeRouter();
        
        addChild(nodeLayer);
        addChild(edgeLayer);  
    }
    
    public LayerWidget getNodeLayer() {
        return nodeLayer;
    }
    
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        LocalClassMetadata nodeClass = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        Color classColor = nodeClass.getColor();
        Widget widget = new ObjectBoxWidget(this, node, classColor);
        ((ObjectBoxWidget) widget).getLabelWidget().setLabel(node.getName());        
        widget.getActions().addAction(createSelectAction());
        initSelectionListener();
        widget.repaint();
        widget.revalidate();
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        SimpleConnectionWidget widget = new SimpleConnectionWidget(this, edge, Color.BLUE);
        widget.getActions().addAction(createSelectAction());
        widget.setStroke(new BasicStroke(1));
        widget.getActions().addAction(addRemoveControlPointAction);
        widget.getActions().addAction(moveControlPointAction);
        widget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        widget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
////        widget.setRouter(RouterFactory.createOrthogonalSearchRouter(nodeLayer, edgeLayer));
        widget.setRouter(router);
        edgeLayer.addChild(widget);
        return widget;
    }
        
    public void addRootWidget (Widget widget){
        widget.getActions().addAction(ActionFactory.createMoveAction());
        nodeLayer.addChild(widget);
    }

    public Router getRouter() {
        return router;
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
    public byte[] getAsXML() {
        //For now
        return null;
    }
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        //TODO: Render here, not in the service
    }

    @Override
    public ConnectProvider getConnectProvider() {
        return null;
    }
    
    @Override
    public boolean supportsConnections() {
        return false;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }

    @Override
    public void render(LocalObjectLight root) {
    }
}
