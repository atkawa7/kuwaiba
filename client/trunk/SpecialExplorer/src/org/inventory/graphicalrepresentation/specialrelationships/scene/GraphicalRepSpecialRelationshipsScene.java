/**
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.graphicalrepresentation.specialrelationships.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.graphicalrepresentation.specialrelationships.actions.GraphicalRepSpecialRelationshipsActionsFactory;
import org.inventory.graphicalrepresentation.specialrelationships.actions.ShowSpecialRelationshipChildrenAction;
import org.inventory.graphicalrepresentation.specialrelationships.widgets.SpecialRelatedObjectNodeWidget;
import org.inventory.graphicalrepresentation.specialrelationships.wrappers.LocalObjectLightWrapper;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.graph.layout.GraphLayoutSupport;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene used to do a graphical representation of Special Relationships
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GraphicalRepSpecialRelationshipsScene extends AbstractScene<LocalObjectLightWrapper, String> {
    /**
     * Counter used to guarantee that the edges are unique
     */
    private long edgeCounter = 0;
    /**
     * Tree Graph Layout
     */
    private SceneLayout sceneLayout;
    /**
     * Custom select provider
     */
    private final WidgetAction selectAction;
    /**
     * Reference to the action factory used to assign actions to the nodes
     */
    private final GraphicalRepSpecialRelationshipsActionsFactory actionsFactory;
    

        
    public GraphicalRepSpecialRelationshipsScene(LocalObjectLightWrapper root) {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(nodeLayer);
        addChild(edgeLayer);
                        
        selectAction = ActionFactory.createSelectAction(new SpecialCustomSelectProvider(this), false);
        actionsFactory = new GraphicalRepSpecialRelationshipsActionsFactory(this);
        
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
                
        initSceneLayout(root);
    }
    
    public long getEdgeCounter() {
        long current = edgeCounter;
        edgeCounter += 1;
        return current;
    }
    
    private void initSceneLayout(LocalObjectLightWrapper root) {
        int originX = 50; //TODO: to improve the value of orginX
        int originY = 50; //TODO: to improve the value of orginY
        GraphLayout<LocalObjectLightWrapper, String> layout = GraphLayoutFactory.createTreeGraphLayout(originX, originY, 150, 150, false);//.createTreeGraphLayout(originX, originY, 100, 50, false, true);
        GraphLayoutSupport.setTreeGraphLayoutRootNode(layout, root);
        sceneLayout = LayoutFactory.createSceneGraphLayout(this, layout);
    }
    
    public void reorganizeNodes() {
        sceneLayout.invokeLayoutImmediately();
        repaint();
    }

    @Override
    public byte[] getAsXML() {
        return null;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
    }

    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) {
        return null;
    }

    @Override
    public ConnectProvider getConnectProvider() {
        return null;
    }

    @Override
    public boolean supportsConnections() {
        return true;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLightWrapper node) {
        if (node == null)
            return null;
        
        LocalObjectLight lol = node.getLocalObjectLightWrapped();
        
        SpecialRelatedObjectNodeWidget nodeWidget = new SpecialRelatedObjectNodeWidget(this, lol);
            
        nodeWidget.setImage(CommunicationsStub.getInstance().getMetaForClass(lol.getClassName(), false).getIcon());
        nodeWidget.setLabel(lol.toString());
        
        nodeWidget.getActions().addAction(selectAction);
        nodeWidget.getActions().addAction(ActionFactory.createMoveAction());
        nodeWidget.getActions().addAction(ActionFactory.createPopupMenuAction(
            actionsFactory.createSpecialRelatedObjectNodeMenu()));
        nodeLayer.addChild(nodeWidget);
            
        return nodeWidget;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget(this);
        
        connectionWidget.setRouter(RouterFactory.createFreeRouter());
        connectionWidget.getActions().addAction(createSelectAction());
        connectionWidget.setStroke(new BasicStroke(1));
        
        LabelWidget labelWidget = new LabelWidget(this, edge.substring(edge.indexOf(" ") + 1));
        labelWidget.setOpaque(true);
        labelWidget.getActions().addAction(ActionFactory.createMoveAction ());
        connectionWidget.addChild(labelWidget);
        connectionWidget.setConstraint(labelWidget, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);
        
        edgeLayer.addChild(connectionWidget);
        return connectionWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, LocalObjectLightWrapper oldSourceNode, LocalObjectLightWrapper sourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget) findWidget(edge);
        Widget sourceWidget = findWidget(sourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createDirectionalAnchor(sourceWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, LocalObjectLightWrapper oldTargetNode, LocalObjectLightWrapper targetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget) findWidget(edge);
        Widget targetWidget = findWidget(targetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createDirectionalAnchor(targetWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL) : null);
    }
    
    private class SpecialCustomSelectProvider extends CustomSelectProvider {

        public SpecialCustomSelectProvider(AbstractScene scene) {
            super(scene);
        }
        
        @Override
        public void select (Widget widget, Point localLocation, boolean invertSelection) {
            super.select(widget, localLocation, invertSelection);
            fireChangeEvent(new ActionEvent(
                findObject(widget), 
                AbstractScene.SCENE_CHANGE, 
                ShowSpecialRelationshipChildrenAction.COMMAND));
        }  
    };
}
