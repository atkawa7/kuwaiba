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
package org.inventory.views.rackview.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.models.physicalconnections.wizards.NewLinkWizard;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.views.rackview.widgets.EquipmentWidget;
import org.inventory.views.rackview.widgets.NestedDeviceWidget;
import org.inventory.views.rackview.widgets.PortWidget;
import org.inventory.views.rackview.widgets.RackViewConnectionWidget;
import org.inventory.views.rackview.widgets.RackWidget;
import org.inventory.views.rackview.widgets.RackWidgetWrapper;
import org.inventory.views.rackview.widgets.actions.ChangePositionAction;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene for Rack view, shows the front view of the rack
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    private boolean addingNestedDevice = true;
    private boolean showConnections = false;
    private LocalObjectLight rack;
    
    private final ChangePositionAction changePositionAction = new ChangePositionAction();
    
    public RackViewScene(LocalObjectLight rack) {
        this.rack = rack;
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
        setActiveTool(ACTION_SELECT);
        initSelectionListener();
        
        nodeLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        addChild(nodeLayer);
        addChild(interactionLayer);
    }
    
    public LocalObjectLight getRack() {
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
    
    @Override
    public byte[] getAsXML() {
        return null;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
    }

    @Override
    public void render(LocalObjectLight root) {
        this.rack = root;
        RackWidgetWrapper rackWidgetWrapper = new RackWidgetWrapper(this, root, !getShowConnections());
        rackWidgetWrapper.setPreferredLocation(new Point(70, 30));
        rackWidgetWrapper.paintRack();
        nodeLayer.addChild(rackWidgetWrapper);
        validate();
    }

    @Override
    public ConnectProvider getConnectProvider() {
        
        return new ConnectProvider() {

            @Override
            public boolean isSourceWidget(Widget sourceWidget) {
                return sourceWidget instanceof PortWidget && ((PortWidget) sourceWidget).isFree();
            }

            @Override
            public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
                if (targetWidget instanceof PortWidget && ((PortWidget) targetWidget).isFree()) {
                    if(sourceWidget.equals(targetWidget))
                        return ConnectorState.REJECT;
                    
                    return ConnectorState.ACCEPT;
                }
                return ConnectorState.REJECT;
            }

            @Override
            public boolean hasCustomTargetWidgetResolver(Scene scene) {
                return false;
            }

            @Override
            public Widget resolveTargetWidget(Scene scene, Point point) {
                return null;
            }

            @Override
            public void createConnection(Widget sourceWidget, Widget targetWidget) {
                LocalObjectLight newConnection;
                LocalObjectLight sourcePort = sourceWidget.getLookup().lookup(LocalObjectLight.class);
                LocalObjectLight targetPort = targetWidget.getLookup().lookup(LocalObjectLight.class);
                
                LocalObjectLight commonParent = CommunicationsStub.getInstance()
                    .getCommonParent(sourcePort.getClassName(), sourcePort.getOid(), 
                        targetPort.getClassName(), targetPort.getOid());
                if (commonParent == null) {
                    NotificationUtil.getInstance().showSimplePopup("Error", 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                } 
                List<LocalObjectLight> existintWireContainersList = CommunicationsStub.getInstance()
                    .getContainersBetweenObjects(sourcePort.getClassName(), sourcePort.getOid(), 
                        targetPort.getClassName(), targetPort.getOid(), Constants.CLASS_WIRECONTAINER);
                if (existintWireContainersList == null) {
                    NotificationUtil.getInstance().showSimplePopup("Error", 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                } 
                NewLinkWizard newLinkWizard = new NewLinkWizard(sourceWidget.getLookup().lookup(ObjectNode.class), 
                    targetWidget.getLookup().lookup(ObjectNode.class), commonParent, existintWireContainersList);
                newLinkWizard.show();
                newConnection = newLinkWizard.getNewConnection();
                
                if (newConnection != null) {                    
                    RackViewConnectionWidget edge = (RackViewConnectionWidget) addEdge(newConnection);
                    setEdgeSource(newConnection, sourcePort);
                    setEdgeTarget(newConnection, targetPort);
                    
                    ((PortWidget) sourceWidget).setFree(false);
                    ((PortWidget) targetWidget).setFree(false);
                            
                    edge.getLabelWidget().setVisible(true);
                    edge.setStroke(new BasicStroke(3));                    
                    edge.setLineColor(Color.CYAN);
                    
                    validate();
                }
            }
        };
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
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget widget = null;
        
        if (node instanceof LocalObject) {
            LocalObject object = ((LocalObject) node);
            if (object.getAttribute(Constants.PROPERTY_RACK_UNITS) != null && 
                object.getAttribute(Constants.PROPERTY_POSITION) != null) {

                widget = new EquipmentWidget(this, object, object.getObjectMetadata().getColor());
                widget.createActions(AbstractScene.ACTION_SELECT);
                widget.getActions(ACTION_SELECT).addAction(createSelectAction());
                widget.getActions(ACTION_SELECT).addAction(changePositionAction);
            }
            
        } else if (showConnections) {
            if (CommunicationsStub.getInstance().isSubclassOf(node.getClassName(), "GenericPhysicalPort")) {
                widget = new PortWidget(this, node, addingNestedDevice);
                
                widget.createActions(AbstractScene.ACTION_CONNECT);
                widget.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(interactionLayer, getConnectProvider()));
                widget.getActions(ACTION_CONNECT).addAction(createSelectAction());                
            } else {
                widget = new NestedDeviceWidget(this, node);
                ((NestedDeviceWidget) widget).paintNestedDeviceWidget();
            }
            widget.createActions(AbstractScene.ACTION_SELECT);
            widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        }            
        if (getRack().equals(node)) {
            if (showConnections)
                widget = new RackWidget(this, node, 1086, 100, 15);
            else
                widget = new RackWidget(this, node, 300, 35, 5);
            widget.createActions(AbstractScene.ACTION_SELECT);
            widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        }
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        RackViewConnectionWidget newWidget = new RackViewConnectionWidget(this, edge);
        newWidget.getLabelWidget().setVisible(false);
        newWidget.getActions().addAction(ActionFactory.createSelectAction(new RackConnectionSelectProvider()));
        
        LocalClassMetadata edgeClass = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (edgeClass == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        }        
        newWidget.setLineColor(edgeClass.getColor());
        
        RackWidget rackWidget = (RackWidget) findWidget(rack);        
        
        newWidget.setRouter(new RackViewConnectionRouter(rackWidget.getEdgetLayer(), rackWidget.getRackUnitHeight()));
        rackWidget.getEdgetLayer().addChild(newWidget);
        
        validate();
        return newWidget;
    }
    
    public class RackConnectionSelectProvider implements SelectProvider {
    
        public RackConnectionSelectProvider() {        
        }

        @Override
        public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        @Override
        public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return ((RackViewScene) widget.getScene()).findObject(widget) != null;
        }

        @Override
        public void select (Widget widget, Point localLocation, boolean invertSelection) {
            RackViewScene scene = ((RackViewScene) widget.getScene());
            
            Object object = scene.findObject (widget);
            
            scene.setFocusedObject (object);
            if (object != null) {
                if (!invertSelection && scene.getSelectedObjects().contains(object))
                    return;
                scene.userSelectionSuggested (Collections.singleton(object), invertSelection);
                
                for (LocalObjectLight edge : scene.getEdges()) {
                    Widget edgeWidget = scene.findWidget(edge);
                    
                    if (edgeWidget != null && edgeWidget instanceof RackViewConnectionWidget) {
                        
                        LocalClassMetadata connectionClass = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
                        if (connectionClass == null) {
                            NotificationUtil.getInstance().showSimplePopup("Error", 
                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                            continue;
                        }
                        ((RackViewConnectionWidget )edgeWidget).setLineColor(connectionClass.getColor());
                    }
                }
                if (widget instanceof RackViewConnectionWidget)
                    ((RackViewConnectionWidget) widget).setLineColor(Color.CYAN);
            } else
                scene.userSelectionSuggested (Collections.emptySet(), invertSelection);
        }
    }
}
