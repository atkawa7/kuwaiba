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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CustomResizeProvider implements ResizeProvider, ResizeStrategy {

    private AbstractScene scene;
    
    public CustomResizeProvider(AbstractScene scene) {
        this.scene = scene;
    }
    
    @Override
    public void resizingStarted(Widget widget) { }

    @Override
    public void resizingFinished(Widget widget) {
        scene.fireChangeEvent(new ActionEvent(scene, AbstractScene.SCENE_CHANGE, "widgetResize"));
    }    

    @Override
    public Rectangle boundsSuggested(Widget widget, Rectangle startRctngl, Rectangle finishRctngl, ControlPoint cp) { 
        return finishRctngl;
    }
}
