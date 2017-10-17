/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.design.modelsLayouts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Palette",
        id = "org.inventory.design.modelsLayouts.ShowPaletteAction"
)
@ActionRegistration(
        displayName = "#CTL_ShowPaletteAction"
)
@ActionReference(path = "Menu/Window", position = 3333)
@Messages("CTL_ShowPaletteAction=Show Palette")
/**
 * Action used to show the common palette
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class ShowPaletteAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        for (TopComponent topComponent : WindowManager.getDefault().findMode("commonpalette").getTopComponents()) {
            topComponent.open();
        }
    }
}
