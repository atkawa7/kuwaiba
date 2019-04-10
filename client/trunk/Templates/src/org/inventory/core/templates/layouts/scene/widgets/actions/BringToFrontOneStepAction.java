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
import java.util.List;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action used to Bring to Front One Step a Shape
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class BringToFrontOneStepAction extends GenericShapeAction {
    private static BringToFrontOneStepAction instance;
    
    private BringToFrontOneStepAction() {
        putValue(NAME, "Bring to Front One Step");
    }
    
    public static BringToFrontOneStepAction getInstance() {
        return instance == null ? instance = new BringToFrontOneStepAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget.getParentWidget() == null)
            return;
        List<Widget> children = selectedWidget.getParentWidget().getChildren();
        int idx = children.indexOf(selectedWidget);
        
        if (idx < 0 || idx == children.size() - 1)
            return;
                        
        Widget oldFrontWidgetOneStep = children.get(idx + 1);
        selectedWidget.bringToFront();
        
        int idxA = children.indexOf(oldFrontWidgetOneStep);
        int idxB = children.indexOf(selectedWidget);
        
        while (idxB != idxA + 1) {
            children.get(idxA + 1).bringToFront();
            
            idxA = children.indexOf(oldFrontWidgetOneStep);
            idxB = children.indexOf(selectedWidget);            
        }
        
        if (selectedWidget.getScene() instanceof DeviceLayoutScene)
            ((DeviceLayoutScene) selectedWidget.getScene()).fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape bring to front"));
    }
    
}
