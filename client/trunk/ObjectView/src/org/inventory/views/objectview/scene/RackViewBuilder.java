/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.visual.scene.AbstractScene;
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
    public AbstractScene getScene() {
        return scene;
    }
    
    @Override
    public final void buildView(LocalObjectLight lol) throws IllegalArgumentException {
        LocalObject rack = com.getObjectInfo(lol.getClassName(), lol.getOid());
        if (rack == null)
            throw new IllegalArgumentException(com.getError());
        
        Integer rackUnits = (Integer)rack.getAttribute(Constants.PROPERTY_RACKUNITS);
        if (rackUnits == null || rackUnits == 0)
            throw new IllegalArgumentException(String.format("Attribute %s in rack %s does not exist or is not set correctly", Constants.PROPERTY_RACKUNITS, lol.toString()));
        else{
            List<LocalObjectLight> children = com.getObjectChildren(lol.getOid(), lol.getClassName());
            if (children == null)
                throw new IllegalArgumentException(com.getError());
            else{
                int rackUnitsCounter = 0;
                for (LocalObjectLight child : children){
                    LocalObject theWholeChild = com.getObjectInfo(child.getClassName(), child.getOid());
                    if (theWholeChild == null)
                        throw new IllegalArgumentException(com.getError());
                        
                    Integer elementRackUnits = (Integer)theWholeChild.getAttribute(Constants.PROPERTY_RACKUNITS);
                    Integer position = (Integer)theWholeChild.getAttribute(Constants.PROPERTY_POSITION);
                    
                    if (elementRackUnits == null ||  position == null || elementRackUnits == 0 || position == 0) 
                        throw new IllegalArgumentException(String.format("Attribute %s or %s does not exist or is not set correctly in element %s", 
                                Constants.PROPERTY_RACKUNITS, Constants.PROPERTY_POSITION, child.toString()));
                        
                    
                    Widget newElement = scene.addNode(theWholeChild);
                    newElement.setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH, RackViewScene.RACK_UNIT_IN_PX * elementRackUnits));
                    newElement.setPreferredLocation(new Point(0, RackViewScene.RACK_UNIT_IN_PX * position - RackViewScene.RACK_UNIT_IN_PX));
                    rackUnitsCounter += elementRackUnits;
                }
                
                if (rackUnitsCounter > rackUnits)
                    throw new IllegalArgumentException(String.format("The sum of the sizes of the elements (%s) exceeds the rack capacity (%s)", rackUnitsCounter, rackUnits));
                    
                scene.getRackWidget().setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH , RackViewScene.RACK_UNIT_IN_PX * rackUnits));
                for (String attribute : rack.getObjectMetadata().getAttributeNames())
                    scene.addInfoLabel(attribute +": " + (rack.getAttribute(attribute) == null ? "" : rack.getAttribute(attribute).toString()), false);
                scene.addInfoLabel("Usage Percentage: "+ Math.round((float)rackUnitsCounter * 100/rackUnits) +"% (" + rackUnitsCounter + "U/" + rackUnits + "U)", true);
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
        JOptionPane.showMessageDialog(null, "This view does not support the selected action", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}