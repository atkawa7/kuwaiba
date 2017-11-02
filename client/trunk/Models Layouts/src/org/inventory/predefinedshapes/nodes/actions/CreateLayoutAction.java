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
package org.inventory.predefinedshapes.nodes.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.design.modelsLayouts.ModelLayoutTopComponent;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateLayoutAction extends GenericInventoryAction {
    public CreateLayoutAction() {
        putValue(NAME, I18N.gm("action_lbl_create_layout"));
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_MODELS_LAYOUTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectListItem preferedShape = Utilities.actionsGlobalContext().lookup(LocalObjectListItem.class);
        if (preferedShape == null)
            return;
        ModelLayoutTopComponent topComponent = (ModelLayoutTopComponent) WindowManager.getDefault().findTopComponent(ModelLayoutTopComponent.ID + preferedShape.getId());
        if (topComponent == null) {
            topComponent = new ModelLayoutTopComponent(preferedShape);
            topComponent.open();
        } else {
            if (topComponent.isOpened())
                topComponent.requestAttention(true);
            else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                   //so we will reuse the instance, refreshing the vierw first
                topComponent.refresh();
                topComponent.open();
            }
        }
        topComponent.requestActive();
    }
}