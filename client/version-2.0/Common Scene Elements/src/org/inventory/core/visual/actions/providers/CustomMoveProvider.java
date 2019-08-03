/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.visual.actions.providers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is a custom provider that implements a simple widget move action AND fires a Change Event when the movement is finished.
 * This is useful is you need to know that a widget position changed to check if the scene should saved, for example
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CustomMoveProvider implements MoveProvider, MoveStrategy {

    private AbstractScene scene;

    public CustomMoveProvider(AbstractScene scene) {
        this.scene = scene;
    }

    @Override
    public void movementStarted(Widget widget) {}

    @Override
    public void movementFinished(Widget widget) {
        scene.fireChangeEvent(new ActionEvent(scene, AbstractScene.SCENE_CHANGE, "widgetMove"));
    }

    @Override
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }

    @Override
    public void setNewLocation(Widget widget, Point location) {
        widget.setPreferredLocation(location);
    }

    @Override
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        return suggestedLocation; 
    }
    
    
}
