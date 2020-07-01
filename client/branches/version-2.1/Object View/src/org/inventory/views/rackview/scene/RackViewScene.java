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
package org.inventory.views.rackview.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPopupMenu;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.AbstractScene;
import static org.inventory.core.visual.scene.AbstractScene.ACTION_CONNECT;
import static org.inventory.core.visual.scene.AbstractScene.ACTION_SELECT;
import org.inventory.models.physicalconnections.wizards.NewLinkWizard;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.views.rackview.widgets.EquipmentWidget;
import org.inventory.views.rackview.widgets.NestedDeviceWidget;
import org.inventory.views.rackview.widgets.PortWidget;
import org.inventory.views.rackview.widgets.RackViewConnectionWidget;
import org.inventory.views.rackview.widgets.RackWidget;
import org.inventory.views.rackview.widgets.RackWidgetWrapper;
import org.inventory.views.rackview.widgets.actions.ChangePositionAction;
import org.inventory.views.rackview.widgets.actions.DeletePhysicalLink;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene for Rack view, shows the front view of the rack
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    public static int STROKE_WIDTH = 3;
    public static int SELECTED_STROKE_WIDTH = 3;
    private boolean addingNestedDevice = true;
    private boolean showConnections = false;
    private LocalObject rack;
    
    private final ChangePositionAction changePositionAction = new ChangePositionAction();
    
    private final PopupMenuProvider defaultPopupMenuProvider;
    
    private List<LocalObject> equipments;
    
    public RackViewScene(List<LocalObject> equipments) {
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
        getInputBindings ().setPanActionButton(MouseEvent.BUTTON1); //Pan using the left click
        
        setActiveTool(ACTION_SELECT);
        initSelectionListener();
        
        nodeLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        addChild(nodeLayer);
        addChild(interactionLayer);
        
        defaultPopupMenuProvider = new PopupMenuProvider() {
            private JPopupMenu popupMenu = null;
            
            @Override
            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                if (popupMenu == null) {
                    popupMenu = new JPopupMenu("Connection Menu");
                    popupMenu.add(DeletePhysicalLink.getInstance());
                }
                DeletePhysicalLink.getInstance().setSelectedWidget(widget);
                return popupMenu;
            }
        };
        this.equipments = equipments;
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
        rack = (LocalObject) root;
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
                List<LocalObjectLight> newConnections = new ArrayList<>();
                LocalObjectLight sourcePort = sourceWidget.getLookup().lookup(LocalObjectLight.class);
                LocalObjectLight targetPort = targetWidget.getLookup().lookup(LocalObjectLight.class);
                
                LocalObjectLight commonParent = CommunicationsStub.getInstance()
                    .getCommonParent(sourcePort.getClassName(), sourcePort.getId(), 
                        targetPort.getClassName(), targetPort.getId());
                if (commonParent == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                } 
                List<LocalObjectLight> existintWireContainersList = CommunicationsStub.getInstance()
                    .getContainersBetweenObjects(sourcePort.getClassName(), sourcePort.getId(), 
                        targetPort.getClassName(), targetPort.getId(), Constants.CLASS_WIRECONTAINER);
                if (existintWireContainersList == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                } 
                NewLinkWizard newLinkWizard = new NewLinkWizard(sourceWidget.getLookup().lookup(ObjectNode.class), 
                    targetWidget.getLookup().lookup(ObjectNode.class), commonParent, existintWireContainersList);
                newLinkWizard.show();
                newConnections = newLinkWizard.getNewConnections();
                
                if (newConnections != null && !newConnections.isEmpty()) {                    
                    RackViewConnectionWidget edge = (RackViewConnectionWidget) addEdge(newConnections.get(0));
                    setEdgeSource(newConnections.get(0), sourcePort);
                    setEdgeTarget(newConnections.get(0), targetPort);
                    
                    ((PortWidget) sourceWidget).setFree(false);
                    ((PortWidget) targetWidget).setFree(false);
                            
                    edge.getLabelWidget().setVisible(true);
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

                widget = new EquipmentWidget(this, object, object.getObjectMetadata().getColor(), !isAddingNestedDevice());
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
                widget = new NestedDeviceWidget(this, node, !isAddingNestedDevice());
                ((NestedDeviceWidget) widget).paintNestedDeviceWidget();
            }
            widget.createActions(AbstractScene.ACTION_SELECT);
            widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        }            
        if (getRack().equals(node)) {
            if (showConnections)
                widget = new RackWidget(this, node, (int) Math.round(1086 * 1.5), (int) Math.round(100 * 1.5), 15, equipments);
            else
                widget = new RackWidget(this, node, 300, 35, 5, equipments);
            widget.createActions(AbstractScene.ACTION_SELECT);
            widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        }
        validate();
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        RackViewConnectionWidget newWidget = new RackViewConnectionWidget(this, edge);
        newWidget.setStroke(new BasicStroke(RackViewScene.STROKE_WIDTH));                    
        newWidget.getLabelWidget().setVisible(false);
        newWidget.getActions().addAction(ActionFactory.createSelectAction(new RackConnectionSelectProvider()));
        newWidget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        
        LocalClassMetadata edgeClass = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (edgeClass == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
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
}
