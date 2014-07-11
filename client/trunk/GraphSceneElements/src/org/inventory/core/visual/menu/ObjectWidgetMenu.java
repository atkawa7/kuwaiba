/**
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.inventory.core.visual.scene.SelectableWidget;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.actions.Presenter;

/**
 * Menu with the actions associated to an edge (a physical connection)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectWidgetMenu implements PopupMenuProvider {

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        JPopupMenu menu = new JPopupMenu();       
        for (Action action : ((SelectableWidget)widget).getNode().getActions(false)){
            if (action !=  null){
                //For some stupid reason, the show-pop-up-action is ignoring actions
                //implementing the Presenter.Popup interface, thus not showing the submenus
                if (action instanceof Presenter.Popup)
                    menu.add(((Presenter.Popup)action).getPopupPresenter());
                else
                    menu.add(action);
            }
        }
        return menu;
    }
}
