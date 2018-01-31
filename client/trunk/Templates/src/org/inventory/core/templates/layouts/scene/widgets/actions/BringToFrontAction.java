/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.event.ActionEvent;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;

/**
 * Action used to Bring to Front a Shape
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BringToFrontAction extends GenericShapeAction {
    private static BringToFrontAction instance;
    
    private BringToFrontAction() {
        putValue(NAME, "Bring To Front");
    }
    
    public static BringToFrontAction getInstance() {
        return instance == null ? instance = new BringToFrontAction() : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectedWidget.bringToFront();
        
        if (selectedWidget.getScene() instanceof DeviceLayoutScene)
            ((DeviceLayoutScene) selectedWidget.getScene()).fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape bring to front"));
    }
    
}
