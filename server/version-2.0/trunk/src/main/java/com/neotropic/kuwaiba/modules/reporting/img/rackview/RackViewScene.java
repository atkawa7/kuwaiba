/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Scene for Rack view, shows the front view of the rack
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewScene extends GraphScene<RemoteObjectLight, RemoteObjectLight> {
    public static int STROKE_WIDTH = 3;
    public static int SELECTED_STROKE_WIDTH = 3;
    private boolean addingNestedDevice = true;
    private boolean showConnections = false;
    private RemoteObject rack;
        
    private final List<RemoteObject> equipments;
    
    private final LayerWidget nodeLayer;
    private final LayerWidget interactionLayer;
    
    public RackViewScene(List<RemoteObject> equipments) {
        nodeLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        addChild(nodeLayer);
        addChild(interactionLayer);
        
        this.equipments = equipments;
    }
    
    public RemoteObjectLight getRack() {
        return rack;
    }
    
    public boolean getShowConnections() {
        return showConnections;
    }
    
    public void setShowConnections(boolean showConnections) {
        this.showConnections = showConnections;
    }

    public boolean isAddingNestedDevice() {
        return addingNestedDevice;
    }

    public void setAddingNestedDevice(boolean addNestedDevice) {
        this.addingNestedDevice = addNestedDevice;
    }
        
    public void render(RemoteObjectLight root) {
        rack = (RemoteObject) root;
        RackWidgetWrapper rackWidgetWrapper = new RackWidgetWrapper(this, root, !getShowConnections());
        rackWidgetWrapper.setPreferredLocation(new Point(70, 30));
        rackWidgetWrapper.paintRack();
        nodeLayer.addChild(rackWidgetWrapper);
        validate();
    }
    
    @Override
    protected Widget attachNodeWidget(RemoteObjectLight node) {
        Widget widget = null;

        if (node instanceof RemoteObject) {
            RemoteObject object = ((RemoteObject) node);
            if (object.getAttribute("rackUnits") != null && //NOI18N
                object.getAttribute("position") != null) { //NOI18N
                
                RemoteClassMetadata rcm = null;
                try {
                    rcm = RackViewImage.getInstance().getWebserviceBean().getClass(
                            object.getClassName(),
                            RackViewImage.getInstance().getIpAddress(),
                            RackViewImage.getInstance().getRemoteSession().getSessionId());
                } catch (ServerSideException ex) {
                    Exceptions.printStackTrace(ex);
                }

                widget = new EquipmentWidget(this, object, new Color(rcm.getColor()), !isAddingNestedDevice());
            }
            
        } else if (showConnections) {
            boolean isSubclassOf = false;
            try {
                isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                    node.getClassName(),
                    "GenericPhysicalPort", //NOI18N
                    RackViewImage.getInstance().getIpAddress(),
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            if (isSubclassOf) {
                widget = new PortWidget(this, node, addingNestedDevice);
                
            } else {
                widget = new NestedDeviceWidget(this, node, !isAddingNestedDevice());
                ((NestedDeviceWidget) widget).paintNestedDeviceWidget();
            }
        }            
        if (getRack().equals(node)) {
            if (showConnections)
                widget = new RackWidget(this, node, (int) Math.round(1086 * 1.5), (int) Math.round(100 * 1.5), 15, equipments);
            else
                widget = new RackWidget(this, node, 300, 35, 5, equipments);
        }
        validate();
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(RemoteObjectLight edge) {
        RackViewConnectionWidget newWidget = new RackViewConnectionWidget(this, edge);
        newWidget.setStroke(new BasicStroke(RackViewScene.STROKE_WIDTH));                    
        newWidget.getLabelWidget().setVisible(false);
        
        RemoteClassMetadata edgeClass = null;
        try {
            edgeClass = RackViewImage.getInstance().getWebserviceBean().getClass(
                edge.getClassName(),
                RackViewImage.getInstance().getIpAddress(),
                RackViewImage.getInstance().getRemoteSession().getSessionId());
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        if (edgeClass == null) {
            return null;
        }        
        newWidget.setLineColor(new Color(edgeClass.getColor()));
        
        RackWidget rackWidget = (RackWidget) findWidget(rack);        
        
        newWidget.setRouter(new RackViewConnectionRouter(rackWidget.getEdgetLayer(), rackWidget.getRackUnitHeight()));
        rackWidget.getEdgetLayer().addChild(newWidget);
        
        validate();
        return newWidget;
    }
    
    @Override
    protected void attachEdgeSourceAnchor(RemoteObjectLight edge, RemoteObjectLight oldSourceNode, RemoteObjectLight newSourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(RemoteObjectLight edge, RemoteObjectLight oldTargetNode, RemoteObjectLight newTargetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
}
