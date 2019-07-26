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

import com.vaadin.flow.component.html.Div;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a Vaadin10-compatible navigation menu that wraps the <a href="https://www.smartmenus.org/">Smart Menus</a> JQuery libray, licensed 
 * under MIT license.
 * @author Charles Edward Bedon Cortazar {@literal charles.bedon@kuwaiba.org}
 */
public class SmartMenuComponent extends Div {
    /**
     * The css class of the main navigation element (<code>nav</code> tag). Only one class is allowed here.
     */
    private final String htmlClass;
    /**
     * The id of the main navigation element (<code>nav</code> tag).
     */
    private final String htmlId;
    /**
     * The list of root items
     */
    private final List<MenuItem> rootItems;
    /**
     * The main navigation element (<code>nav</code> tag)
     */
    private Tags.MenuNavigationTag tagMenuNav;
    /**
     * The menu main row. This implementation provides support for a single-row menu only.
     */
    private Tags.MenuRowTag tagMenuRow;

    /**
     * Default constructor.
     * @param htmlClass The css class of the main navigation element (<code>nav</code> tag). Only one class is allowed here.
     * @param htmlId The id of the main navigation element (<code>nav</code> tag).
     */
    public SmartMenuComponent(String htmlClass, String htmlId) {
        this.htmlClass = htmlClass;
        this.htmlId = htmlId;
        this.rootItems = new ArrayList<>();
        this.tagMenuNav = new Tags.MenuNavigationTag(this.htmlClass);
        this.tagMenuRow = new Tags.MenuRowTag(this.htmlId, "sm","sm-blue");
        this.tagMenuNav.add(this.tagMenuRow);
        this.add(this.tagMenuNav);
        this.setWidth("100%");
    }

    /**
     * Adds a new root entry to the menu model.
     * @param mnuItem The menu item.
     */
    public void addRootMenuItem(MenuItem mnuItem) {
        this.rootItems.add(mnuItem);
    }

    /**
     * Generates the HTML elements necessary for the original JS library (optional, because not all libraries require an HTML part).
     */
    public void buildHtml() {
        getRootItems().stream().forEach(anItem -> this.tagMenuRow.add(anItem));
    }
    
    /**
     * Generates the JS code ready to be executed by the page to add or start the component.
     * @return A string containing the JS code ready to be executed using Page.executeJavascript(String)
     */
    public String buildJs() {
        String jsString =
                "if (typeof $ !== 'undefined') {" + //In certain race conditions, the Smart Menus library might not be loaded
                "$(function() {\n" +                //when thios code is executed, so we check if $ is defined before.
                "  $('#" + getHtmlId() + "').smartmenus({\n" +
                "    subMenusSubOffsetX: 1,\n" +
                "    subMenusSubOffsetY: -8\n" +
                "  });\n" +
                "});\n" +
                "}\n";
        return jsString;
    }    

    public String getHtmlId() {
        return this.htmlId;
    }

    public List<MenuItem> getRootItems() {
        return this.rootItems;
    }
}
