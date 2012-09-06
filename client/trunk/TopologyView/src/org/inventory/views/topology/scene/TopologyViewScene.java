/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.views.topology.scene;

import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.inventory.communications.LocalStuffFactory;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.views.topology.scene.provider.AcceptActionProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Scene used by the GISView component
 * @author Adrian Martinez Molinar <adrian.martinez@kuwaiba.org>
 */
public class TopologyViewScene extends GraphScene<LocalObjectLight, LocalObjectLight> implements PropertyChangeListener, Lookup.Provider{

    private final String GENERIC_ICON_PATH="org/inventory/views/gis/res/default.png"; //NOI18
    /**
     * Default icon
     */
    private final Image defaultIcon = ImageUtilities.loadImage(GENERIC_ICON_PATH);
    /**
     * Layer to contain the nodes (poles, cabinets, etc)
     */
    private LayerWidget nodesLayer;
    /**
     * Layer to contain the connections (containers, links, etc)
     */
    private LayerWidget connectionsLayer;
    /**
     * Layer to contain additional labels (free text)
     */
    private LayerWidget labelsLayer;
    /**
     * Scene lookup
     */
    private SceneLookup lookup;
    

    public TopologyViewScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));
        nodesLayer = new LayerWidget(this);
        connectionsLayer = new LayerWidget(this);
        labelsLayer = new LayerWidget(this);

        addChild(connectionsLayer);
        addChild(nodesLayer);
        addChild(labelsLayer);

        this.lookup = new SceneLookup(Lookup.EMPTY);

        addObjectSceneListener(new ObjectSceneListener() {
            @Override
            public void objectAdded(ObjectSceneEvent event, Object addedObject) { }
            @Override
            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {}
            @Override
            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {}
            @Override
            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1)
                    lookup.updateLookup((LocalObjectLight)newSelection.iterator().next());
            }
            @Override
            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {}
            @Override
            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {}
            @Override
            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {}
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        //Actions
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));

        setActiveTool(ObjectNodeWidget.ACTION_SELECT);
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        ObjectNodeWidget myWidget = new ObjectNodeWidget(this, node);
        nodesLayer.addChild(myWidget);
        myWidget.setImage(defaultIcon);
        myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(createSelectAction());
        myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(ActionFactory.createMoveAction());
        myWidget.getActions(ObjectNodeWidget.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(connectionsLayer, new ConnectProvider() {

            @Override
            public boolean isSourceWidget(Widget sourceWidget) {
                if (sourceWidget instanceof ObjectNodeWidget)
                    return true;
                return false;
            }

            @Override
            public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
                
                if (targetWidget instanceof ObjectNodeWidget)
                    return ConnectorState.ACCEPT;
                return ConnectorState.REJECT;
            }

            @Override
            public boolean hasCustomTargetWidgetResolver(Scene scene) {
                return false;
            }

            @Override
            public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
                return null;
            }

            @Override
            public void createConnection(Widget sourceWidget, Widget targetWidget) {
                ObjectConnectionWidget newEdge = (ObjectConnectionWidget)addEdge(LocalStuffFactory.createLocalObjectLight());
                newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceWidget, 3));
                newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(targetWidget, 3));
                
            }
        }));
        return myWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget myWidget =  new ObjectConnectionWidget(this, edge);
        connectionsLayer.addChild(myWidget);
        myWidget.getActions().addAction(createSelectAction());
        myWidget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
        myWidget.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
        myWidget.setStroke(new BasicStroke(1));
        myWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setRouter(RouterFactory.createFreeRouter());
        return myWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        validate();
        repaint();
    }

    @Override
    public Lookup getLookup(){
        return this.lookup;
    }

    /**
     * Helper class to let us launch a lookup event every time a widget is selected
     */
    private class SceneLookup extends ProxyLookup{

        public SceneLookup(Lookup initialLookup) {
            super(initialLookup);
        }

        public void updateLookup(Lookup newLookup){
            setLookups(newLookup);
        }

        public void updateLookup(LocalObjectLight newElement){
            setLookups(Lookups.singleton(new ObjectNode(newElement)));
        }
    }
}
