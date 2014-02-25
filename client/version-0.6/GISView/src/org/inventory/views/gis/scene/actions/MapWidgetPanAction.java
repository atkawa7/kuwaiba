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

import java.awt.Point;
import java.awt.geom.Point2D;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.MapPanel;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * A modified version of the original pan action for a scrolless container. It pans over a JXMapViewer component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MapWidgetPanAction extends WidgetAction.LockedAdapter{
    /**
     * Local scene
     */
    private Scene scene;
    /**
     * Last location to calculate the delta
     */
    private Point lastLocation;
    /**
     * Map component to be managed
     */
    private MapPanel map;
    /**
     * What mouse button should be used to activate the pan action? Use MouseEvent.BUTTONX constants
     */
    private int mouseButton;

    /**
     * Main constructor
     * @param mapComponent The map component to be manipulated
     * @param mouseButton What mouse button should be used to activate the pan action? Use MouseEvent.BUTTONX constants
     */
    public MapWidgetPanAction(MapPanel mapComponent, int mouseButton) {
        this.map = mapComponent;
        this.mouseButton = mouseButton;
    }

    @Override
    protected boolean isLocked () {
        return false;
    }

    @Override
    public State mousePressed (Widget widget, WidgetMouseEvent event) {

        if (event.getButton () == mouseButton) {
            scene = widget.getScene ();
            lastLocation = event.getPoint ();
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    @Override
    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return pan (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean pan (Widget widget, Point newLocation) {
        if (scene != widget.getScene ())
            return false;
        int deltaX = lastLocation.x - newLocation.x, deltaY = lastLocation.y - newLocation.y;
//        map.getMainMap().setCenter(new Point2D.Double(map.getMainMap().getCenter().getX() + deltaX,
//                map.getMainMap().getCenter().getY() + deltaY));
//        ((GISViewScene)scene).pan(deltaX, deltaY);
        lastLocation = newLocation;
        return true;
    }
}