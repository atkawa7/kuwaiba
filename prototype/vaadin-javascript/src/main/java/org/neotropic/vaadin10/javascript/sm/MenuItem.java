/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the MIT and GPLv3 Licenses, Version 1.0 (the "Licenses");
 *  you may not use this file except in compliance with the Licenses.
 *  You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/MIT
 *       https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licenses is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the Licenses.
 */
package org.neotropic.vaadin10.javascript.sm;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import java.util.List;

/**
 * Model class that represents a menu item.
 * @author Charles Edward Bedon Cortazar {@literal charles.bedon@kuwaiba.org}
 */
@Tag(Tag.LI)
public class MenuItem extends HtmlContainer {

    /**
     * A menu item without submenu.
     * @param label The display label.
     * @param url The URL this items points to
     */
    public MenuItem(String label, String url) {
        add(new Tags.MenuLinkTag(label, url));
    }
    
    /**
     * A menu item with a submenu.
     * @param label The label (this implementation does not allow to set a link in the submenu root item).
     * @param subItems The list of sub items.
     */
    public MenuItem(String label, List<MenuItem> subItems) {
        //The root label (without a link, but it's trivial to add a link using the other MenuLinkTag constructor)
        add(new Tags.MenuLinkTag(label));
        //Build the sub menu
        Tags.MenuSubMenuTag tagSubMenu = new Tags.MenuSubMenuTag();
        subItems.stream().forEach(aSubMenuItem -> tagSubMenu.add(aSubMenuItem));
        //Add the submenu
        add(tagSubMenu);
    }

    
}
