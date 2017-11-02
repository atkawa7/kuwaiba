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
package org.inventory.views.rackview;

import org.inventory.views.rackview.scene.RackViewScene;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.views.rackview.widgets.EquipmentWidget;
import org.inventory.views.rackview.widgets.NestedDeviceWidget;
import org.inventory.views.rackview.widgets.PortWidget;
import org.inventory.views.rackview.widgets.RackViewConnectionWidget;
import org.inventory.views.rackview.widgets.RackWidget;
import org.inventory.views.rackview.widgets.SelectableRackViewWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service used to load data to render a rack view
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackViewService {
    //this need to be replace, the VirtualPort should be moved under GenericLogicalPort, 
    //find a better place for the other classes under GenericBoard, should be GenericCommunitacionsBoard 
    //to make a diference between the PowerBoards and the Communitacions Boards
    private static final List<String> noVisibleDevices = Arrays.asList(new String [] {"PowerBoard", "VirtualPort", "ServiceInstance", "PowerPort", "Transceiver"}); //NOI18N
    private final LocalObjectLight rackLight;
    private final RackViewScene scene;
    
    public RackViewService(RackViewScene scene, LocalObjectLight rackLight) {
        this.rackLight = rackLight;
        this.scene = scene;
    }
    
    public void shownRack() {
        LocalObject rack = CommunicationsStub.getInstance().getObjectInfo(
            rackLight.getClassName(), rackLight.getOid());
        
        if (rack == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            Integer rackUnits = (Integer) rack.getAttribute(Constants.PROPERTY_RACK_UNITS);
            if (rackUnits == null || rackUnits == 0) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                    NotificationUtil.ERROR_MESSAGE, 
                    String.format("Attribute %s in rack %s does not exist or is not set correctly", Constants.PROPERTY_RACK_UNITS, rack));
                return;
            }
            Boolean ascending = (Boolean) rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING);
            if (ascending == null) {
                ascending = true;
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), NotificationUtil.WARNING_MESSAGE, 
                    "The rack unit sorting has not been set. Ascending is assumed");
            } else
                ascending = !ascending;
            
            scene.render(rack);
            if (scene.getShowConnections()) {
                Widget widget = scene.findWidget(rack);
                if (widget instanceof RackWidget) {
                    for (LocalObject equipment : ((RackWidget) widget).getLocalEquipment()) {
                        Widget equipmentWidget = scene.findWidget(equipment);
                                
                        if(equipmentWidget instanceof EquipmentWidget && ((EquipmentWidget) equipmentWidget).hasEquipmentModelLayout())
                            setEquipmentParent(equipmentWidget, equipmentWidget);
                        else
                            addNestedDevices(equipment);
                    }
                    
                    List<LocalObjectLightList> connections = CommunicationsStub.getInstance().getPhysicalConnectionsInObject(rack.getClassName(), rack.getOid());
                    
                    if (connections == null) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    } else
                        createConnections(connections);
                            
                }
            }
            ((RackWidget) scene.findWidget(rack)).resizeRackWidget();
            scene.repaint();
        }
    }
    
    private void setEquipmentParent(Widget equipmentWidget, Widget parentWidget) {
        
        for (Widget child : parentWidget.getChildren()) {
            Object objectChild = scene.findObject(child);
            if (objectChild instanceof LocalObjectLight) {
                if (CommunicationsStub.getInstance().isSubclassOf(((LocalObjectLight) objectChild).getClassName(), "GenericPhysicalPort")) { //NOI18N
                    if (child instanceof PortWidget)
                        ((PortWidget) child).setParent((NestedDeviceWidget) equipmentWidget);
                }
            }
            setEquipmentParent(equipmentWidget, child);
        }
    }
    
    public void addNestedDevices(LocalObjectLight parent) {
        Widget parentWidget = scene.findWidget(parent);
                
        if (parentWidget == null)
            return;
        
        if (parentWidget instanceof NestedDeviceWidget) {
            
            List<LocalObjectLight> children = CommunicationsStub.getInstance()
                .getObjectChildren(parent.getOid(), parent.getClassName());

            if (children == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            
            for (LocalObjectLight child : children) {
                if (noVisibleDevices.contains(child.getClassName()))
                    continue;
                Widget deviceWidget = scene.addNode(child);
                ((NestedDeviceWidget) parentWidget).addChildDevice((SelectableRackViewWidget) deviceWidget);
                scene.validate();
                addNestedDevices(child);
            }
        }
    }
    
    public void createConnections(List<LocalObjectLightList> connections) {
        List<LocalObjectLight> addedConnections = new ArrayList<>();
        
        for (LocalObjectLightList connection : connections) {
            ObjectConnectionWidget lastConnectionWidget = null;
            LocalObjectLight aSide = null;
            LocalObjectLight bSide = null;        
            LocalObjectLight linkLight = null;
            for (LocalObjectLight object : connection) {
                if(CommunicationsStub.getInstance().isSubclassOf(object.getClassName(), Constants.CLASS_GENERICPORT)) {
                    if(aSide == null)
                        aSide = object;
                    else
                        bSide = object;
                }
                else
                    linkLight = object;
            }
            Widget aSideNode = scene.findWidget(aSide);
            Widget bSideNode = scene.findWidget(bSide);            
            //if the connection has both sides
            if(aSideNode != null && bSideNode != null){
                if (linkLight == null)
                    continue;
                                
                if (!addedConnections.contains(linkLight)) {
                    //The background for the connected ports is set to red.
                    ((PortWidget) bSideNode).setFree(false);
                    ((PortWidget) aSideNode).setFree(false);

                    Widget findWidget = scene.findWidget(linkLight);

                    if (findWidget != null)
                        scene.removeEdge(linkLight);

                    lastConnectionWidget = (ObjectConnectionWidget)scene.addEdge(linkLight);
                    lastConnectionWidget.getLabelWidget().setLabel( 
                            (aSide.getName() == null ? "" : aSide.getName()) + " ** " + (bSide.getName() == null ? "" : bSide.getName()));
                    scene.setEdgeSource(linkLight, aSide);
                    scene.setEdgeTarget(linkLight, bSide);
                    addedConnections.add(linkLight);
                }
            } else {
                if (aSideNode != null && linkLight != null)
                    ((PortWidget) aSideNode).setFree(false);
                
                if (bSideNode != null && linkLight != null)
                    ((PortWidget) bSideNode).setFree(false);
            }
        }
        scene.validate();
        scene.repaint();
    }
    
    public List<List<LocalObjectLight>> getRackTable() {
        List<LocalObjectLight> ports = new ArrayList();
        for (LocalObjectLight node : scene.getNodes()) {
            if (scene.findWidget(node) instanceof PortWidget)
                ports.add(node);
        }        
        List<List<LocalObjectLight>> result = new ArrayList();
                
        while (!ports.isEmpty()) {
            LocalObjectLight port = ports.get(0);
            
            LocalObjectLight[] edges = scene.findNodeEdges(port, true, true).toArray(new LocalObjectLight[] {});
            if (edges.length == 1) {
                LocalObjectLight edge = edges[0];
                
                RackViewConnectionWidget conn = (RackViewConnectionWidget) scene.findWidget(edge);
                PortWidget sourcePort = (PortWidget) conn.getSourceAnchor().getRelatedWidget();
                PortWidget targetPort = (PortWidget) conn.getTargetAnchor().getRelatedWidget();
                
                NestedDevice sourceEquipment = sourcePort;
                while (sourceEquipment.getParent() != null)
                    sourceEquipment = sourceEquipment.getParent();
                
                NestedDevice targetEquipment = targetPort;
                while (targetEquipment.getParent() != null)
                    targetEquipment = targetEquipment.getParent();
                
                EquipmentWidget sourceDevice = (EquipmentWidget) sourceEquipment;
                EquipmentWidget targetDevice = (EquipmentWidget) targetEquipment;
                
                LocalObjectLight sourcePortObj = sourcePort.getLookup().lookup(LocalObjectLight.class);
                LocalObjectLight targetPortObj = targetPort.getLookup().lookup(LocalObjectLight.class);
                
                List<LocalObjectLight> row = new ArrayList();
                row.add(sourceDevice.getLookup().lookup(LocalObject.class));
                row.add(sourcePortObj);
                row.add(targetDevice.getLookup().lookup(LocalObject.class));
                row.add(targetPortObj);
                
                ports.remove(sourcePortObj);
                ports.remove(targetPortObj);
                
                result.add(row);
            } else {
                ports.remove(port);
            }
        }
        return result;
    }
}
