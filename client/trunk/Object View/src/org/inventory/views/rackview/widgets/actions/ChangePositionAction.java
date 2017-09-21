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
 *
 */
package org.inventory.views.rackview.widgets.actions;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.inventory.communications.core.LocalObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.rackview.scene.RackViewScene;
import org.inventory.views.rackview.widgets.EquipmentWidget;
import org.inventory.views.rackview.widgets.RackUnitWidget;
import org.inventory.views.rackview.widgets.RackWidget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action used to change the position of an Equipment Widget
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ChangePositionAction extends WidgetAction.LockedAdapter {       
    private boolean moved;
    
    public ChangePositionAction() {
    }
    
    @Override
    protected boolean isLocked () {
        return moved;
    }

    @Override
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1)
            return State.createLocked (widget, this);
        
        return State.REJECTED;
    }

    @Override
    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (moved && widget instanceof EquipmentWidget) {
            LocalObject equipmentObject = ((EquipmentWidget) widget).getLookup().lookup(LocalObject.class);

            RackViewScene rackViewScene = ((EquipmentWidget) widget).getRackViewScene();
            RackWidget rackWidget = (RackWidget) rackViewScene.findWidget(rackViewScene.getRack());

            Point newLocation = widget.convertLocalToScene (event.getPoint());

            RackUnitWidget rackUnit = rackWidget.findRackUnitIndex(newLocation);

            if (rackUnit == null) {
                rackWidget.getLocalEquipment().remove(equipmentObject);
                rackWidget.addEquipment(equipmentObject);

                NotificationUtil.getInstance().showSimplePopup("Information", 
                    NotificationUtil.INFO_MESSAGE, "The equipment must be dropped inside of a rack unit");
            } else {
                if (rackWidget.canBeMoved(equipmentObject, rackUnit.getRackUnitIndex())) {
                    rackWidget.getLocalEquipment().remove(equipmentObject);
                    rackWidget.freeEquipmentRackUnits(equipmentObject);
                    rackUnit.setEquipmentPosition(rackUnit, equipmentObject);
                } else {
                    rackWidget.getLocalEquipment().remove(equipmentObject);
                    rackWidget.addEquipment(equipmentObject);
                }
            }
            moved = false;
            return State.CONSUMED;
        }
        moved = false;
        return State.REJECTED;
    }

    @Override
    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        if (widget instanceof EquipmentWidget) {
            moved = true;
            Point location = widget.getLocation();
            Point inLocation = event.getPoint();               

            Point newLocation = new Point(location.x, location.y + inLocation.y);

            Point p = widget.getParentWidget().convertSceneToLocal(widget.convertLocalToScene(newLocation));

            if (p.y < 0)
                newLocation.setLocation(newLocation.x, 0);

            widget.setPreferredLocation(newLocation);
            widget.bringToFront();
            
            ((EquipmentWidget) widget).getScene().repaint();
            
            return State.createLocked(widget, this);
        }
        return State.REJECTED;
    }
}
