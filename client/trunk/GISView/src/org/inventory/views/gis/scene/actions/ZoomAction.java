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

import org.inventory.views.gis.scene.GISViewScene;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is a custom zoom action that only zooms (in or out) the inner map
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ZoomAction extends WidgetAction.Adapter{
    /**
     * Zoom multiplier. Unused for now
     */
    private double zoomMultiplier;

    /**
     * Main constructor. Uses a zoom factor of 1
     * @param mapComponent
     */
    public ZoomAction() {
        this(1);
    }

    /**
     * Alternate constructor
     * @param mapComponent The map component to be manipulated
     * @param zoomMultiplier zoom multiplier
     */
    public ZoomAction(int zoomMultiplier) {
        this.zoomMultiplier = zoomMultiplier;
    }

    @Override
    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        GISViewScene scene = (GISViewScene)widget.getScene ();

        return zoom(event.getWheelRotation (), scene);

        
    }

    @Override
    public State mouseClicked(Widget widget, WidgetMouseEvent event) {
        if (event.getClickCount() == 2){
           GISViewScene scene = (GISViewScene)widget.getScene ();
           return zoom(-1,scene);
        }else
            return WidgetAction.State.REJECTED;
    }

    private State zoom (int howMuch, GISViewScene scene){
//        if (howMuch < 0) //Zoom in
//            scene.zoomIn();
//        else //Zoom out
//            scene.zoomOut();

        return WidgetAction.State.CONSUMED;
    }
}
