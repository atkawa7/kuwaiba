/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.core.updates.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.core.updates.windows.UpdateCenterOptionsDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to define the Update Center Options
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ActionID(
        category = "Tools/Administrative",
        id = "org.inventory.core.updates.actions.UpdateCenterOptionsAction"
)
@ActionRegistration(
        displayName = "#CTL_UpdateCenterOptionsAction"
)
@ActionReference(separatorBefore = 1, path = "Menu/Tools" , position = 3333)
@Messages("CTL_UpdateCenterOptionsAction=Update Center")
public final class UpdateCenterOptionsAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        
        UpdateCenterOptionsDialog.getInstance().setVisible(true);
    }
}
