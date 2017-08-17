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
package org.inventory.core.visual.menu;

import java.awt.Point;
import javax.swing.JPopupMenu;
import org.inventory.core.visual.actions.DeleteFrameAction;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Create Menu for frames inside topology designer scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FrameMenu implements PopupMenuProvider {
    private JPopupMenu theMenu = null;
    private static FrameMenu instance;
    
    private FrameMenu() {
    }
    
    public static FrameMenu getInstance() {
        return instance == null ? instance = new FrameMenu() : instance;
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        if (theMenu == null) {
            theMenu = new JPopupMenu("Frame Menu");
            theMenu.add(DeleteFrameAction.getInstance((AbstractScene) widget.getScene()));
        }
        return theMenu;
    }
}
