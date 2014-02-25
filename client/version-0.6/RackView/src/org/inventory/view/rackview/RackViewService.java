/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.view.rackview;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.view.rackview.scene.RackViewScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Service associated to the RackViewTC
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RackViewService implements LookupListener {
    private RackViewTopComponent rvtc;
    private RackViewScene scene;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private Lookup.Result<LocalObjectLight> selectedNodes;
    
    public RackViewService(final RackViewTopComponent rvtc) {
        this.rvtc = rvtc;
        this.scene = new RackViewScene();
         
    }
    
    public void initializeLookupListener(){
        selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        selectedNodes.addLookupListener(this);
        if (selectedNodes.allInstances().size() == 1){ //There's a node already selected
            LocalObjectLight selectedObject = (LocalObjectLight)selectedNodes.allInstances().iterator().next();
            if (selectedObject.getClassName().equals(Constants.CLASS_RACK)){
                rvtc.setDisplayName(selectedObject.toString());
                buildView(selectedObject);
            }
        }
    }
    
    public void terminateLookListener(){
        selectedNodes.removeLookupListener(this);
    }

    public RackViewScene getScene() {
        return scene;
    }
    
    public final void buildView(LocalObjectLight lol){
        LocalObject rack = com.getObjectInfo(lol.getClassName(), lol.getOid());
        if (rack == null)
            rvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
        
        Integer rackUnits = (Integer)rack.getAttribute(Constants.PROPERTY_RACKUNITS);
        if (rackUnits == null || rackUnits == 0){
            rvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, 
                    String.format("attribute %s in rack %s doesn not exist or is not set correctly", Constants.PROPERTY_RACKUNITS, lol.toString()));
        }else{
            List<LocalObjectLight> children = com.getObjectChildren(lol.getOid(), lol.getClassName());
            if (children == null)
                rvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            else{
                int rackUnitsCounter = 0;
                for (LocalObjectLight child : children){
                    LocalObject theWholeChild = com.getObjectInfo(child.getClassName(), child.getOid());
                    if (theWholeChild == null){
                        rvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
                        scene.clear();
                        return;
                    }
                    Integer elementRackUnits = (Integer)theWholeChild.getAttribute(Constants.PROPERTY_RACKUNITS);
                    Integer position = (Integer)theWholeChild.getAttribute(Constants.PROPERTY_POSITION);
                    
                    if (elementRackUnits == null ||  position == null || elementRackUnits == 0 || position == 0) {
                        rvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, 
                                String.format("attribute %s or %s does not exist or is not set correctly in element %s", 
                                Constants.PROPERTY_RACKUNITS, Constants.PROPERTY_POSITION, child.toString()));
                        scene.clear();
                        return;
                    }
                    
                    Widget newElement = scene.addNode(theWholeChild);
                    newElement.setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH, (int)(RackViewScene.RACK_UNIT_IN_PX * elementRackUnits)));
                    newElement.setPreferredLocation(new Point(0, RackViewScene.RACK_UNIT_IN_PX * position - RackViewScene.RACK_UNIT_IN_PX));
                    rackUnitsCounter += elementRackUnits;
                }
                
                if (rackUnitsCounter > rackUnits){
                    rvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, 
                                String.format("The sum of the sizes of the elements (%s) exceeds the rack capacity (%s)", rackUnitsCounter, rackUnits));
                    scene.clear();
                    return;
                }
                
                scene.getRackWidget().setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH , (int)(RackViewScene.RACK_UNIT_IN_PX * rackUnits)));
                for (String attribute : rack.getObjectMetadata().getAttributeNames())
                    scene.addInfoLabel(attribute +": " + (rack.getAttribute(attribute) == null ? "" : rack.getAttribute(attribute).toString()), false);
                scene.addInfoLabel("Usage Percentage: "+ Math.round((float)rackUnitsCounter * 100/rackUnits) +"% (" + rackUnitsCounter + "U/" + rackUnits + "U)", true);
            }
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Lookup.Result lookupResult = (Lookup.Result)ev.getSource();
        if(lookupResult.allInstances().size() == 1){ //There's only one node selected
            scene.clear();
            rvtc.setDisplayName(null);
            LocalObjectLight selectedObject = (LocalObjectLight)lookupResult.allInstances().iterator().next();
            if (selectedObject.getClassName().equals(Constants.CLASS_RACK)){
                rvtc.setDisplayName(selectedObject.toString());
                buildView(selectedObject);
            }
        }
    }
    
}
