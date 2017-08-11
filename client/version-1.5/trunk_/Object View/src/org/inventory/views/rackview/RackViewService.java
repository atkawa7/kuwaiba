/**
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
package org.inventory.views.rackview;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.rackview.scene.RackViewScene;

/**
 * Service for Rack view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackViewService {
    private final LocalObjectLight rackLight;
    private final RackViewScene scene;
    
    public RackViewService(RackViewScene scene, LocalObjectLight rackLight) {
        this.rackLight = rackLight;
        this.scene = scene;
    }
    
    public LocalObjectLight getRack() {
        return rackLight;        
    }
    
    public void buildRackView() throws Exception {
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
        } else {
            ascending = !ascending;
        }
        
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
        
        for (LocalObject emptyModule : emptyModules) {
            if (scene.findWidget(emptyModule) != null)
                scene.removeNode(emptyModule);
            
            scene.addNode(emptyModule);
        }
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
                        units[i] = 1; // rack module used
                    else
                        throw new Exception(String.format("The device %s in the position %s cannot be located inside a unit in use", device.toString(), position));
                }
            } else
                throw new Exception(String.format("The device %s in the position %s cannot have more than %s rack units", device.toString(), position, rackUnits - drawPosition));
            
            if (scene.findWidget(device) != null)
                scene.removeNode(device);
            
            scene.addNode(device);
        }
        String lblName = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NAME"); //NOI18N
        String lblSerialNumber = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_SERIAL_NUMBER"); //NOI18N
        String lblVendor = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_VENDOR"); //NOI18N
        String lblRackNumbering = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NUMBERING"); //NOI18N
        
        String lblAscending = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NUMBERING_ASCENDING"); //NOI18N
        String lblDescending = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NUMBERING_DESCENDING"); //NOI18N
        
        String lblUsagePercentage = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_USAGE_PERCENTAGE"); //NOI18N
        
        String name = rack.getName();
        String serialNumber = rack.getAttribute("serialNumber") == null ? "" : rack.getAttribute("serialNumber").toString(); //NOI18N
        String vendor = rack.getAttribute("vendor") == null ? "" : rack.getAttribute("vendor").toString(); //NOI18N
        String rackNumbering = ascending ? lblAscending : lblDescending;
        String usagePercentage = "" + Math.round((float)rackUnitsCounter * 100/rackUnits) +"% (" + rackUnitsCounter + "U/" + rackUnits + "U)";
        
        scene.addRackInfoLabel(String.format("%s: %s", lblName, name), false);
        scene.addRackInfoLabel(String.format("%s: %s", lblSerialNumber, serialNumber), false);
        scene.addRackInfoLabel(String.format("%s: %s", lblVendor, vendor), false);
        scene.addRackInfoLabel(String.format("%s: %s", lblRackNumbering, rackNumbering), false);
        scene.addRackInfoLabel(String.format("%s: %s", lblUsagePercentage, usagePercentage), true);
    }
}