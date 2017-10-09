/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.design.modelsLayouts.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GroupCopyShapeAction extends GenericInventoryAction {
    private static GroupCopyShapeAction instance;
    private Widget selectedWidget;
    private Shape shapeToCopy;
    
    public GroupCopyShapeAction() {
        putValue(NAME, "Group Copy");
    }
    
    public static GroupCopyShapeAction getInstance() {
        return instance == null ? instance = new GroupCopyShapeAction() : instance;
    }
    
    public Widget getSelectedWidget() {
        return selectedWidget;        
    }
    
    public void setSelectedWidget(Widget selectedWidget) {
        this.selectedWidget = selectedWidget;
    }
    
    public Shape getShapeToCopy() {
        return shapeToCopy;
    }
    
    public void setShapeToCopy(Shape shapeToCopy) {
        this.shapeToCopy = shapeToCopy;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget != null) {
            // Copy and Group copy actions are mutually exclusive
            CopyShapeAction.getInstance().setShapeToCopy(null);
            
            ModelLayoutScene scene = ((ModelLayoutScene) selectedWidget.getScene());
            Object object = scene.findObject(selectedWidget);
            
            if (object != null && object instanceof Shape) {
                shapeToCopy = (Shape) object;
            }
        }
    }
    
}
