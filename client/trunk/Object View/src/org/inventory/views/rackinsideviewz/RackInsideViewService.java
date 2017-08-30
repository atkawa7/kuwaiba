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
 */
package org.inventory.views.rackinsideviewz;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.rackview.scene.RackInsideViewScene;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service for rack inside view
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class RackInsideViewService {
    private final LocalObjectLight rackLight;
    private final RackInsideViewScene scene;
    
    public RackInsideViewService(RackInsideViewScene scene, LocalObjectLight rackLight) {
        this.rackLight = rackLight;
        this.scene = scene;
    }
    
    public LocalObjectLight getRack() {
        return rackLight;        
    }
    
    public void buildRackInsideView() throws Exception {
        scene.clear();
        LocalObject rack = CommunicationsStub.getInstance().getObjectInfo(rackLight.getClassName(), rackLight.getOid());
        
        if (rack == null)
            throw new Exception(CommunicationsStub.getInstance().getError());
        
        scene.render(rack);
        
        Integer rackUnits = (Integer) rack.getAttribute(Constants.PROPERTY_RACK_UNITS);
        if (rackUnits == null || rackUnits == 0)
            throw new Exception(String.format("Attribute %s in rack %s does not exist or is not set correctly", Constants.PROPERTY_RACK_UNITS, rack));
                
        Boolean ascending = (Boolean) rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING);
        if (ascending == null) {
            ascending = true;
            NotificationUtil.getInstance().showSimplePopup("Warning", 
                NotificationUtil.INFO_MESSAGE, String.format("The rack unit sorting has not been set. Ascending is assumed"));
        }else 
            ascending = !ascending;
        
        List<LocalObjectLight> devicesLight = CommunicationsStub.getInstance().getObjectChildren(rack.getOid(), rack.getClassName());
        
        if (devicesLight == null)
            throw new Exception(CommunicationsStub.getInstance().getError());
        
        int rackUnitsCounter = 0;
        
        List<LocalObject> devices = new ArrayList();
        
        for (LocalObjectLight deviceLight : devicesLight) {
            
            LocalObject device = CommunicationsStub.getInstance().getObjectInfo(deviceLight.getClassName(), deviceLight.getOid());
            if (device == null)
                throw new Exception(CommunicationsStub.getInstance().getError());
            
            Integer deviceRackUnits = (Integer) device.getAttribute(Constants.PROPERTY_RACK_UNITS);
            Integer position = (Integer) device.getAttribute(Constants.PROPERTY_POSITION);
            
            if (deviceRackUnits == null || position == null) {
                throw new Exception(String.format(
                    "Attribute %s or %s does not exist or is not set correctly in element %s", 
                    Constants.PROPERTY_RACK_UNITS, 
                    Constants.PROPERTY_POSITION, device.toString()));
            }
            
            if (deviceRackUnits < 1 || position < 1)
                continue;
            
            if (position == rackUnits.intValue() && deviceRackUnits > 1)
                throw new Exception(String.format("The device %s in the position %s cannot have more than one rack unit", device.toString(), position));
            
            devices.add(device);
            rackUnitsCounter += deviceRackUnits;
        }
        if (rackUnitsCounter > rackUnits) {
            throw new Exception(String.format(
                "The sum of the sizes of the elements (%s) exceeds the rack capacity (%s)", 
                rackUnitsCounter, 
                rackUnits));
        }
        List<LocalObject> emptyModules = new ArrayList();
                
        for (int i = 0; i < rackUnits; i += 1) {
            Long id = -1L * (i + 1);
            emptyModules.add(new LocalObject("", id, new String[0], new Object[0]));
        }
        
        scene.createNumberingInRack(ascending, rackUnits);
       
        int [] units = new int[rackUnits];        
        for (int i = 0; i <= rackUnits; i += 1)
            units[0] = 0; // rack module free
        
        for (LocalObject device : devices) {
            Integer U = (Integer) device.getAttribute(Constants.PROPERTY_RACK_UNITS);
            Integer position = (Integer) device.getAttribute(Constants.PROPERTY_POSITION);
            
            int drawPosition = position;
            if (ascending)
                drawPosition -= 1;
            else
                drawPosition = rackUnits - position - (U - 1);
            
            if (drawPosition + (U - 1) < rackUnits) {
                for (int i = drawPosition; i <= drawPosition + (U - 1); i += 1) {
                    if (units[i] == 0)
                        units[i] = 1; // set rack module in used
                    else
                        throw new Exception(String.format("The device %s in the position %s cannot be located inside a unit in use", device.toString(), position));
                }
            } else
                throw new Exception(String.format("The device %s in the position %s cannot have more than %s rack units", device.toString(), position, rackUnits - drawPosition));
            
            if (scene.findWidget(device) != null)
                scene.removeNode(device);
            Widget deviceNode = scene.addNode(device);
            ((NestedDeviceWidget)deviceNode).setBackgroundColor(new Color(213, 216, 221, 240));
            
            addSubdevices(deviceNode, device);
            scene.addRootWidget(deviceNode, U, position);
        }

        //add the empty modules
        for (int i = 0; i<units.length; i++) {
            if (scene.findWidget(emptyModules.get(i)) != null)
                scene.removeNode(emptyModules.get(i));

            scene.addNode(emptyModules.get(i));
        }
        createConnections(CommunicationsStub.getInstance().getPhysicalConnectionsInObject(rack.getClassName(), rack.getOid()));
    }
    
    public void addSubdevices(Widget deviceNode, LocalObjectLight device){
        List<LocalObjectLight> objectChildren = CommunicationsStub.getInstance().getObjectChildren(device.getOid(), device.getClassName());
        Color childrenColor = randomColor();
        if(!objectChildren.isEmpty()){
            for (LocalObjectLight objectChild : objectChildren) {
                if(objectChild.getClassName().equals("PowerBoard"))
                    scene.removeNode(device);
                //this need to be replace, the VirtualPort should be moved under GenericLogicalPort, 
                //find a better place for the other classes under GenericBoard should be GenericCommunitacionsBoard 
                //to make a diference between the PowerBoards and the GenericCommunitacionsBoards
                else if(!objectChild.getClassName().equals("VirtualPort") &&
                        !objectChild.getClassName().equals("ServiceInstance") &&
                        !objectChild.getClassName().equals("PowerPort") &&
                        !objectChild.getClassName().equals("Transceiver")){
                
                    Widget subDevice = scene.findWidget(objectChild);
                    if(subDevice != null)
                        scene.removeNode(objectChild);
                        
                    subDevice = scene.addNode(objectChild);
                    //if is a port the backgorund color is set to green by default
                    if(CommunicationsStub.getInstance().isSubclassOf(objectChild.getClassName(), Constants.CLASS_GENERICPORT)){
                        ((NestedDeviceWidget)subDevice).setBackgroundColor(new Color(144, 245, 0));
                        subDevice.setMaximumSize(new Dimension(20, 20));
                    }
                    else
                        ((NestedDeviceWidget)subDevice).setBackgroundColor(childrenColor);
                    ((NestedDeviceWidget)deviceNode).addBox(subDevice);
                    
                    addSubdevices(subDevice, objectChild);
                }
            }
        }
    }
    
    /**
     * Iterates over the connections inside the rack and add the edges to the scene
     * @param connections all the connections inside the rack, there is a list 
     * with a path(aSide, bSide, connection)for every connection 
     */
    public void createConnections(List<LocalObjectLightList> connections){
        List<LocalObjectLight> addedConnections = new ArrayList<>();
        for (LocalObjectLightList connection : connections) {
            SimpleConnectionWidget lastConnectionWidget = null;
            LocalObjectLight aSide = null;
            LocalObjectLight bSide = null;        
            LocalObjectLight linkLight = null;
            for (LocalObjectLight object : connection) {
                if(CommunicationsStub.getInstance().isSubclassOf(object.getClassName(), Constants.CLASS_GENERICPORT)){
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
                LocalObject link = CommunicationsStub.getInstance().getObjectInfo(linkLight.getClassName(), linkLight.getOid());

                if(!addedConnections.contains(link)){
                    //The background for the connected ports is set to red.
                    ((NestedDeviceWidget)bSideNode).setBackgroundColor(new Color(255, 62, 51));
                    ((NestedDeviceWidget)aSideNode).setBackgroundColor(new Color(255, 62, 51));

                     Widget findWidget = scene.findWidget(link);

                    if(findWidget != null)
                        scene.removeEdge(link);

                    lastConnectionWidget = (SimpleConnectionWidget)scene.addEdge(link);
                    lastConnectionWidget.getLabelWidget().setLabel( 
                            (aSide.getName() == null ? "" : aSide.getName()) + " ** " + (bSide.getName() == null ? "" : bSide.getName()));
                    lastConnectionWidget.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideNode));
                    lastConnectionWidget.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideNode));
                    lastConnectionWidget.setForeground(link.getObjectMetadata().getColor());
                    addedConnections.add(link);
                }
            }
        }
    }
    
    private static int rand(double min, double max) {
        return (int)(min + (Math.random()) * (max - min));
    }
    
    protected static Color randomColor(){
        return new Color(rand(25, 255), rand(25, 255), rand(25, 255), 230);
    }
}