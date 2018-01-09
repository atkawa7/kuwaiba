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
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * Action used to group/ungroup shapes in the scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GroupShapesAction extends GenericInventoryAction {
    private static GroupShapesAction instance;
    public boolean isGroup = true;
    
    private GroupShapesAction() {
        putValue(NAME, "Ungroup shapes");
    }        
    
    public static GroupShapesAction getInstance() {
        return instance == null ? instance = new GroupShapesAction() : instance;        
    }
    
    public boolean isGroup() {
        return isGroup;
    }
    
    public void setIsGroup(boolean isGroup) {
        if (isGroup)
            putValue(NAME, "Ungroup shapes");
        else
            putValue(NAME, "Group shapes");
        
        this.isGroup = isGroup;        
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setIsGroup(!isGroup);
    }
}
