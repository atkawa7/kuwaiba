/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.visual.menu;

import java.awt.Point;
import javax.swing.JPopupMenu;
import org.inventory.core.visual.scene.SelectableConnectionWidget;
import org.inventory.core.visual.scene.SelectableNodeWidget;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 * Menu with the actions associated to an edge (a physical connection)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectWidgetMenu implements PopupMenuProvider {

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        if (widget instanceof SelectableNodeWidget)
            return Utilities.actionsToPopup(((SelectableNodeWidget)widget).getNode().getActions(true), 
                    widget.getScene().getView());
        
        return Utilities.actionsToPopup(((SelectableConnectionWidget)widget).getNode().getActions(true), 
                    widget.getScene().getView());
    }
}
