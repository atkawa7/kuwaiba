/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.gis.scene.actions;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.openide.awt.StatusDisplayer;

/**
 * An adaptation of the generic move action: Based on the original code at <code>org.netbeans.modules.visual.action.MoveAction</code>
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class MoveAction extends WidgetAction.LockedAdapter {

    private Widget movingWidget = null;
    private Point lastMouseLocation = null;

    @Override
    protected boolean isLocked () {
        return movingWidget != null;
    }

    @Override
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            movingWidget = widget;
            lastMouseLocation = event.getPoint ();
            return State.createLocked (widget, this);
        }
        
        return State.REJECTED;
    }

    @Override
    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        boolean state;
        if (lastMouseLocation != null  &&  lastMouseLocation.equals (event.getPoint ()))
            state = true;
        else
            state = move (widget, event.getPoint ());
        if (state) {
            movingWidget = null;
            lastMouseLocation = null;
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    @Override
    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point newLocation) {
        if (movingWidget != widget)
            return false;
        
        int deltaX = newLocation.x - lastMouseLocation.x + widget.getBounds().width/2;
        int deltaY = newLocation.y - lastMouseLocation.y + widget.getBounds().height/2;

        widget.setPreferredLocation(new Point(widget.getPreferredLocation().x + deltaX, widget.getPreferredLocation().y + deltaY));
        ((GeoPositionedNodeWidget)widget).updateCoordinates();
        StatusDisplayer.getDefault().setStatusText(((GeoPositionedNodeWidget)widget).getLatitude()+", "+((GeoPositionedNodeWidget)widget).getLongitude());
        return true;
    }

}