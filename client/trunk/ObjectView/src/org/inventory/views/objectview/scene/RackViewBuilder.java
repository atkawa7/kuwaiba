/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.objectview.scene;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.objectview.ObjectViewService;
import org.netbeans.api.visual.widget.Widget;

/**
 * Renders a rack view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RackViewBuilder implements AbstractViewBuilder {
    private RackViewScene scene;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private ObjectViewService service;
    
    public RackViewBuilder(ObjectViewService service) {
        this.scene = new RackViewScene();
        this.service = service;
    }

    @Override
    public RackViewScene getScene() {
        return scene;
    }
    
    @Override
    public final void buildView(LocalObjectLight lol) throws IllegalArgumentException {
        LocalObject rack = com.getObjectInfo(lol.getClassName(), lol.getOid());
        if (rack == null)
            throw new IllegalArgumentException(com.getError());
        
        Integer rackUnits = (Integer)rack.getAttribute(Constants.PROPERTY_RACK_UNITS);
        if (rackUnits == null || rackUnits == 0)
            throw new IllegalArgumentException(String.format("Attribute %s in rack %s does not exist or is not set correctly", Constants.PROPERTY_RACK_UNITS, lol));
        else {
            
            int rackHeight = RackViewScene.RACK_UNIT_IN_PX * rackUnits;
            
            Boolean ascending = (Boolean)rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING);      
            if (ascending == null) {
                ascending = true;
                NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.INFO_MESSAGE, String.format("The rack unit sorting has not been set. Ascending is assumed"));
            }
            
            List<LocalObjectLight> children = com.getObjectChildren(lol.getOid(), lol.getClassName());
            if (children == null)
                throw new IllegalArgumentException(com.getError());
            else {
                int rackUnitsCounter = 0;
                for (LocalObjectLight child : children){
                    LocalObject theWholeChild = com.getObjectInfo(child.getClassName(), child.getOid());
                    if (theWholeChild == null)
                        throw new IllegalArgumentException(com.getError());
                        
                    Integer elementRackUnits = (Integer)theWholeChild.getAttribute(Constants.PROPERTY_RACK_UNITS);
                    Integer position = (Integer)theWholeChild.getAttribute(Constants.PROPERTY_POSITION);
                    
                    if (elementRackUnits == null ||  position == null) 
                        throw new IllegalArgumentException(String.format("Attribute %s or %s does not exist or is not set correctly in element %s", 
                                Constants.PROPERTY_RACK_UNITS, Constants.PROPERTY_POSITION, child.toString()));
                        
                    if (elementRackUnits < 1 || position < 1) //Some children of the rack do not need to be displayed because they don't use any rack unit
                        continue;
                    
                    Widget newElement = scene.addNode(theWholeChild);
                    newElement.setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH, RackViewScene.RACK_UNIT_IN_PX * elementRackUnits));
                    
                    //Rack position 1 is on top if ascending = true, else it starts from the bottom 
                    if (ascending)
                        newElement.setPreferredLocation(new Point(0, RackViewScene.RACK_Y_OFFSET + RackViewScene.RACK_UNIT_IN_PX * position - RackViewScene.RACK_UNIT_IN_PX));
                    else
                        newElement.setPreferredLocation(new Point(0, rackHeight - RackViewScene.RACK_UNIT_IN_PX * position - RackViewScene.RACK_UNIT_IN_PX + RackViewScene.RACK_Y_OFFSET));
                    
                    rackUnitsCounter += elementRackUnits;
                }
                
                if (rackUnitsCounter > rackUnits)
                    throw new IllegalArgumentException(String.format("The sum of the sizes of the elements (%s) exceeds the rack capacity (%s)", rackUnitsCounter, rackUnits));
                    
                scene.getRackWidget().setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH , rackHeight + RackViewScene.RACK_Y_OFFSET));
                
                scene.renderPositions(rackUnits, ascending);
                
                scene.addInfoLabel("Name: " + rack.getName(), false);
                scene.addInfoLabel("Serial Number: " + (rack.getAttribute("serialNumber") == null ? "" : rack.getAttribute("serialNumber").toString()), false); //NOI18N
                scene.addInfoLabel("Vendor: " + (rack.getAttribute("vendor") == null ? "" : rack.getAttribute("vendor").toString()), false); //NOI18N
                scene.addInfoLabel("Rack Numbering: " + (rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING) == null || ascending
                                                    ? "Ascending" : "Descending"), false); //NOI18N
                scene.addInfoLabel("Usage Percentage: "+ Math.round((float)rackUnitsCounter * 100/rackUnits) +"% (" + rackUnitsCounter + "U/" + rackUnits + "U)", true);
                scene.getInfoWidget().setPreferredLocation(new Point((int) (scene.getRackWidget().getPreferredSize().getWidth()), RackViewScene.RACK_Y_OFFSET));
            }
        }
    }   

    @Override
    public String getName() {
        return "Rack View";
    }

    //This view is avaible only to racks
    @Override
    public boolean supportsClass(String className) {
        return Constants.CLASS_RACK.equals(className);
    }

    @Override
    public void refresh() {
        scene.clear();
        buildView(service.getCurrentObject());
    }

    @Override
    public void saveView() {
        JOptionPane.showMessageDialog(null, "This view can not be saved. Try exporting it to an image file instead.", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}