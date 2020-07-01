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
package org.inventory.core.services.api.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.windows.WindowManager;


/**
 * Refreshes the focused component
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionID(id = "org.inventory.core.services.api.actions.RefreshComponentAction", category = "View")
@ActionRegistration(iconBase = "org/inventory/core/services/res/view-refresh.png", displayName = "org.inventory.core.services.Bundle#CTL_Refresh", iconInMenu = true)
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "F5"),
    @ActionReference(path = "Menu/View", name = "org-inventory-core-services-api-actions-RefreshAction", position = 100),
    @ActionReference(path = "Toolbars/00_General", name = "org-inventory-core-services-api-actions-Refresh", position = 2)})
public final class RefreshComponentAction extends AbstractAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Object activeTopComponent = WindowManager.getDefault().getRegistry().getActivated();
        if (activeTopComponent == null)
            return;

        if (activeTopComponent instanceof Refreshable)
            ((Refreshable)activeTopComponent).refresh();
    }
}
