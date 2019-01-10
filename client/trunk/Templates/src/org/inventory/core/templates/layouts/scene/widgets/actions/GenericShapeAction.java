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

import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * Generic Shape Action used to define the common features of shapes actions
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class GenericShapeAction extends GenericInventoryAction {
    protected Widget selectedWidget;
    
    public Widget getSelectedWidget() {
        return selectedWidget;
    }
    
    public void setSelectedWidget(Widget selectedWidget) {
        this.selectedWidget = selectedWidget;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return null;
    }
}
