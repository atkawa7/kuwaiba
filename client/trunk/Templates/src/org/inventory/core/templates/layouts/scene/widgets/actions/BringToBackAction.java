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
 * Action used to Bring to Back a Shape
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class BringToBackAction extends GenericShapeAction {
    private static BringToBackAction instance;
    
    private BringToBackAction() {
        putValue(NAME, "Bring To Back");
    }
    
    public static BringToBackAction getInstance() {
        return instance == null ? instance = new BringToBackAction() : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectedWidget.bringToBack();
        
        if (selectedWidget.getScene() instanceof DeviceLayoutScene)
            ((DeviceLayoutScene) selectedWidget.getScene()).fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape bring to back"));
    }
    
}
