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

package org.neotropic.vaadin10.javascript;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.Arrays;
import org.neotropic.vaadin10.javascript.sm.MenuItem;
import org.neotropic.vaadin10.javascript.sm.SmartMenuComponent;

/**
 * A page to test the Smart Menus wrapper (for details see {@link SmartMenuComponent } ).
 * @author Charles Edward Bedon Cortazar { @literal <charles.bedon@kuwaiba.org> }
 */
@JavaScript("/js/jquery/jquery.js")
@JavaScript("/js/jquery/jquery.smartmenus.js")
@StyleSheet("/css/sm/sm-core-css.css")
@StyleSheet("/css/sm/sm-blue/sm-blue.css")
@Route("menu")
public class SmartMenusView extends VerticalLayout {

    public SmartMenusView() {
        setSizeFull();
    }
    
    @Override
    protected void onAttach(AttachEvent ev) {
        getUI().ifPresent(ui -> {
            SmartMenuComponent menu = new SmartMenuComponent("main-nav","main-menu");
            menu.addRootMenuItem(new MenuItem("Go to kuwaiba.org", "https://www.kuwaiba.org"));
            menu.addRootMenuItem(new MenuItem("Search Engines", Arrays.asList(
                    new MenuItem("DuckDuckGo", "https://www.duckduckgo.com"), 
                    new MenuItem("Bing", "https://www.bing.com"),
                    new MenuItem("Other Search Engines", Arrays.asList(
                            new MenuItem("Google", "https://www.google.com"), 
                            new MenuItem("Yahoo", "https://www.yahoo.com"))))));
            menu.buildHtml();
            add(menu);
            ui.getPage().executeJavaScript(menu.buildJs());
        });
    }
}
