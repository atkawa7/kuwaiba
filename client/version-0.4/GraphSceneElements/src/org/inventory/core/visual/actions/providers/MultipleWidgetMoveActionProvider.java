/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.core.visual.actions.providers;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map.Entry;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Provides a move action for multiple objects. Thanks to Geertjan Wielenga, Toni Eppleton et all for this code.
 * Check for details <a href="http://blogs.sun.com/geertjan/entry/multiple_selection_in_small_visual">here</a>
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MultipleWidgetMoveActionProvider implements MoveProvider,MoveStrategy {
    private HashMap<Widget,Point> originalLocations = new HashMap<Widget, Point> ();
    private final GraphScene scene;

    public MultipleWidgetMoveActionProvider (GraphScene scene) {
        this.scene = scene;
    }

    @Override
    public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
        return suggestedLocation;
    }

    @Override
    public void movementStarted (Widget widget) {
    }

    @Override
    public void movementFinished (Widget widget) {
        originalLocations.clear();
    }

    @Override
    public Point getOriginalLocation (Widget widget) {
        for (Object o : scene.getSelectedObjects()) {
            Widget w = scene.findWidget (o);
            originalLocations.put (w, ActionFactory.createDefaultMoveProvider ().getOriginalLocation (w));
        }
        return ActionFactory.createDefaultMoveProvider ().getOriginalLocation (widget);
    }

    @Override
    public void setNewLocation (Widget widget, Point location) {
        ActionFactory.createDefaultMoveProvider ().setNewLocation (widget, location);
        Point originalLocation = originalLocations.get(widget);
        if (originalLocation == null)
            return;
        int dx = location.x - originalLocation.x;
        int dy = location.y - originalLocation.y;
        for (Entry<Widget, Point> entry : originalLocations.entrySet()) {
            Widget w = entry.getKey();
            if (w == widget)
                continue;
            Point l = new Point (entry.getValue());
            l.translate(dx, dy);
            ActionFactory.createDefaultMoveProvider().setNewLocation(w, l);
        }
    }
}
