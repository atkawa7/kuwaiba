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

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
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
    

    public TopologyViewScene() {
        nodesLayer = new LayerWidget(this);
        //connectionsLayer = new LayerWidget(this);
        //labelsLayer = new LayerWidget(this);

        addChild(nodesLayer);
        //addChild(connectionsLayer);
        //getActions().addAction(ActionFactory.createZoomAction());
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        NetNodeWidget myWidget = new NetNodeWidget(this, node);
        nodesLayer.addChild(myWidget);
        myWidget.setImage(defaultIcon);
        myWidget.getActions().addAction(createSelectAction());
        myWidget.getActions().addAction(ActionFactory.createMoveAction());
        return myWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ConnectionWidget connection = new ConnectionWidget(this);
        return connection;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
