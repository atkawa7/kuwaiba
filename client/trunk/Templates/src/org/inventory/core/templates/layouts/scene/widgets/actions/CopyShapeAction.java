/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;

/**
 * Action used to Copy Shapes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CopyShapeAction extends GenericShapeAction {
    private static CopyShapeAction instance;
    private Shape shapeToCpy;
    
    private CopyShapeAction() {
        putValue(NAME, I18N.gm("lbl_copy_action"));
    }
    
    public static CopyShapeAction getInstance() {
        return instance == null ? instance = new CopyShapeAction() : instance;
    }
    
    public Shape getShapeToCopy() {
        return shapeToCpy;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget != null) {
            DeviceLayoutScene scene = (DeviceLayoutScene) selectedWidget.getScene();
            
            Object obj = scene.findObject(selectedWidget);
            
            if (obj instanceof Shape)
                shapeToCpy = (Shape) obj;
        }
    }
    
}
