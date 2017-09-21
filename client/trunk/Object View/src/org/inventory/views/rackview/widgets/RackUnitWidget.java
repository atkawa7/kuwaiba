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
package org.inventory.views.rackview.widgets;

import org.inventory.views.rackview.scene.RackViewScene;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import static org.inventory.core.visual.scene.AbstractScene.SCENE_CHANGE;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * Widget used to represent an empty rack unit which listen drop node actions 
 * when the user wants add an equipment in the rack view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackUnitWidget extends RackViewWidget {
    private static final Color rackUnitColor = new Color(112, 112, 112);
    private final int rackUnitIndex;
    // Used to verify if a rack unit are in used
    private boolean available = true;
    
    public RackUnitWidget(RackViewScene scene, int rackUnitIndex, RackWidget parentRack) {
        super(scene);
        this.rackUnitIndex = rackUnitIndex;        
        setOpaque(true);
        setBackground(rackUnitColor);                
        setMinimumSize(new Dimension(parentRack.getRackUnitWidth(), parentRack.getRackUnitHeight()));
        getActions().addAction(getAcceptAction());
    }
            
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {        
        this.available = available;
    }
        
    public int getRackUnitIndex() {
        return rackUnitIndex;
    }
    
    private WidgetAction getAcceptAction() {
        return ActionFactory.createAcceptAction(new AcceptProvider() {

            @Override
            public ConnectorState isAcceptable(Widget widget, Point point, Transferable t) {
                if (available)
                    return ConnectorState.ACCEPT;
                return ConnectorState.REJECT_AND_STOP;
            }

            @Override
            public void accept(Widget widget, Point point, Transferable t) {
                try {                  
                    Object object = t.getTransferData(LocalObjectLight.DATA_FLAVOR);
                    if (object instanceof LocalObjectLight) {
                        LocalObjectLight equipmentLight = (LocalObjectLight) object;
                        
                        setEquipmentPosition(widget, equipmentLight);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                }
            }
        });
    }
    
    public void setEquipmentPosition(Widget widget, LocalObjectLight equipmentLight) {
        LocalObject equipment = CommunicationsStub.getInstance().
            getObjectInfo(equipmentLight.getClassName(), equipmentLight.getOid());

        if (equipment == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }                        
        RackWidget parentRackWidget = (RackWidget) ((RackUnitWidget) widget).getRackViewScene().findWidget(((RackUnitWidget) widget).getRackViewScene().getRack()); 
        int position = ((RackUnitWidget) widget).getRackUnitIndex();
        if (!parentRackWidget.isRackable(equipment, position))
            return;

        if (!parentRackWidget.containsEquipment(equipment)) {

            LocalObjectLight parentRack = parentRackWidget.getLookup().lookup(LocalObjectLight.class);                            

            LocalObjectLight equipmentParent = CommunicationsStub.getInstance().getParent(equipment.getClassName(), equipment.getOid());                            
            if (equipmentParent == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            // Moves the equipment if are not in the current rack
            if (!equipmentParent.equals(parentRack)) {                                
                if (!CommunicationsStub.getInstance().moveObjects(parentRack.getClassName(), parentRack.getOid(), new LocalObjectLight [] {equipment})) {
                    NotificationUtil.getInstance().showSimplePopup("Error", 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                }                                
            }                            
            // Updates the position of the equipment
            LocalObject update = new LocalObject(equipment.getClassName(), equipment.getOid(), new String [] {Constants.PROPERTY_POSITION}, new Object [] {position});

            if (!CommunicationsStub.getInstance().saveObject(update)) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            // Adds the equipment in the rack
            equipment = CommunicationsStub.getInstance().getObjectInfo(equipmentLight.getClassName(), equipmentLight.getOid());
            if (equipment == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            parentRackWidget.addEquipment(equipment);

            parentRackWidget.getRackViewScene().fireChangeEvent(new ActionEvent(equipment, SCENE_CHANGE, "equipment-add-operation"));
        } else {
            NotificationUtil.getInstance().showSimplePopup("Information", 
                NotificationUtil.INFO_MESSAGE, "The equipment is already displayed in the rack");
        }
    }
}