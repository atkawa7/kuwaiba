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

import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Service used to load data to render a rack view
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewService {
    //this need to be replace, the VirtualPort should be moved under GenericLogicalPort, 
    //find a better place for the other classes under GenericBoard, should be GenericCommunitacionsBoard 
    //to make a diference between the PowerBoards and the Communitacions Boards
    private final RemoteObject rack;
    private final RackViewScene scene;
    
    public RackViewService(RackViewScene scene, RemoteObject rack) {
        this.rack = rack;
        this.scene = scene;
    }
    
    public void shownRack() {
        
        if (rack == null) {
            Notification.show("Empty Rack", Notification.Type.ERROR_MESSAGE);
        } else {
            Boolean ascending = Boolean.valueOf(rack.getAttribute("rackUnitsNumberingDescending")); // NOI18N
            if (ascending == null) {
                Notification.show("The rack unit sorting has not been set. Ascending is assumed", Notification.Type.WARNING_MESSAGE);
            }
            scene.render(rack);
            
            if (scene.getShowConnections()) {
                Widget widget = scene.findWidget(rack);
                if (widget instanceof RackWidget) {
                    for (RemoteObject equipment : ((RackWidget) widget).getLocalEquipment()) {
                        Widget equipmentWidget = scene.findWidget(equipment);
                                
                        if(equipmentWidget instanceof EquipmentWidget && ((EquipmentWidget) equipmentWidget).hasLayout())
                            setEquipmentParent(equipmentWidget, equipmentWidget);
                    }
                    List<RemoteObjectLight> specialChildren = null;
                    try {
                        specialChildren = RackViewImage.getInstance().getWebserviceBean().getObjectSpecialChildren(
                            rack.getClassName(), 
                            rack.getId(), 
                            RackViewImage.getInstance().getIpAddress(), 
                            RackViewImage.getInstance().getRemoteSession().getSessionId());
                        
                    } catch (ServerSideException ex) {
                        Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        Exceptions.printStackTrace(ex);
                    }
                    
                    if (specialChildren == null) {
                        
                    } else {
                        List<RemoteObjectLight> connections = new ArrayList();
                        for (RemoteObjectLight specialChild : specialChildren) {
                            boolean isSubclassOf = false;
                            
                            try {
                                isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                                        specialChild.getClassName(),
                                        "GenericPhysicalLink", //NOI18N
                                        RackViewImage.getInstance().getIpAddress(),
                                        RackViewImage.getInstance().getRemoteSession().getSessionId());
                            } catch (ServerSideException ex) {
                            }
                            
                            if (isSubclassOf)
                                connections.add(specialChild);
                        }
                        createConnections(connections);
                    }
                }
            }
            ((RackWidget) scene.findWidget(rack)).resizeRackWidget();
            scene.validate();
            scene.repaint();
        }
    }
    /**
     * Used to create the connection router
     */
    private void setEquipmentParent(Widget equipmentWidget, Widget parentWidget) {
        
        for (Widget child : parentWidget.getChildren()) {
            Object objectChild = scene.findObject(child);
            if (objectChild instanceof RemoteObjectLight) {
                
                boolean isSubclassOf = false;
                
                try {
                    isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                        ((RemoteObjectLight) objectChild).getClassName(), 
                        "GenericPhysicalPort", //NOI18N
                        RackViewImage.getInstance().getIpAddress(), 
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
                    
                } catch (ServerSideException ex) {
                }
                
                if(isSubclassOf) {
                    if (child instanceof PortWidget)
                        ((PortWidget) child).setParent((NestedDeviceWidget) equipmentWidget);
                }
            }
            setEquipmentParent(equipmentWidget, child);
        }
    }
    
    private void createConnections(List<RemoteObjectLight> connections) {
        
        HashMap<RemoteObjectLight, HashMap<String, RemoteObjectLight[]>> connectionsMap = new HashMap();
        
        for (RemoteObjectLight connection : connections) {
            
            RemoteObjectSpecialRelationships specialAttributes = null;
            
            try {
                specialAttributes = RackViewImage.getInstance().getWebserviceBean().getSpecialAttributes(
                        connection.getClassName(),
                        connection.getId(),
                        RackViewImage.getInstance().getIpAddress(),
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
                
                if (specialAttributes != null) {
                    HashMap<String, RemoteObjectLight[]> res = new HashMap<>();

                    for (int i = 0; i < specialAttributes.getRelationships().size(); i++){

                        RemoteObjectLightList relatedRemoteObjects = specialAttributes.getRelatedObjects().get(i);
                        RemoteObjectLight[] relatedLocalObjects = new RemoteObjectLight[relatedRemoteObjects.getList().size()];
                        int j = 0;
                        for (RemoteObjectLight relatedRemoteObject : relatedRemoteObjects.getList()) {
                            relatedLocalObjects[j] = relatedRemoteObject;
                            j++;
                        }
                        res.put(specialAttributes.getRelationships().get(i), relatedLocalObjects);
                    }
                    connectionsMap.put(connection, res);
                } else
                    connectionsMap.put(connection, null);
            } catch (ServerSideException ex) {
            }
        }
                        
        for (int i = 0; i < connections.size(); i += 1) {
            RemoteObjectLight connection = connections.get(i);
            
            HashMap<String, RemoteObjectLight[]> specialAttributes = connectionsMap.get(connection);
            
            if (specialAttributes == null) {
                continue;
            }
            
            RemoteObjectLight[] endpointsA = specialAttributes.get("endpointA"); //NOI18N
            if (endpointsA == null)
                continue;
                            
            RemoteObjectLight[] endpointsB = specialAttributes.get("endpointB"); //NOI18N
            if (endpointsB == null)
                continue;
            
            if (endpointsA.length == 0) {
                Notification.show(String.format("The endpointA was removed in the link %s", connection.toString()), Notification.Type.WARNING_MESSAGE);
                continue;
            }
            if (endpointsB.length == 0) {
                Notification.show(String.format("The Endpoint B was removed in the link %s", connection.toString()), Notification.Type.WARNING_MESSAGE);
                continue;
            }
            RemoteObjectLight aSide = endpointsA[0];
            boolean isSubclassOf = false;
            try {
                isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                        aSide.getClassName(),
                        "GenericPort", //NOI18N
                        RackViewImage.getInstance().getIpAddress(),
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
            } catch (ServerSideException ex) {
            }
            
            if (!isSubclassOf) {
                continue;
            }
            RemoteObjectLight bSide = endpointsB[0];
            isSubclassOf = false;
            try {
                isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                        bSide.getClassName(),
                        "GenericPort", //NOI18N
                        RackViewImage.getInstance().getIpAddress(),
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
            } catch (ServerSideException ex) {
            }
            if (!isSubclassOf) {
                Notification.show(String.format("The endpointB in link %s is not a %s", connection.toString(), "GenericPort"), Notification.Type.WARNING_MESSAGE); //NOI18N
                continue;
            }
            
            Widget aSideWidget = scene.findWidget(aSide);
            Widget bSideWidget = scene.findWidget(bSide);
            
            if (aSideWidget != null && bSideWidget != null) { 
                RackViewConnectionWidget lastConnectionWidget = (RackViewConnectionWidget)scene.addEdge(connection);

                lastConnectionWidget.getLabelWidget().setLabel((aSide.getName() == null ? "" : aSide.getName()) + " ** " + (bSide.getName() == null ? "" : bSide.getName()));
                scene.setEdgeSource(connection, aSide);
                scene.setEdgeTarget(connection, bSide);
            }
            if (aSideWidget instanceof PortWidget)
                ((PortWidget) aSideWidget).setFree(false);

            if (bSideWidget instanceof PortWidget)
                ((PortWidget) bSideWidget).setFree(false);
            
            scene.validate();
            scene.repaint();
        }
    }
}

