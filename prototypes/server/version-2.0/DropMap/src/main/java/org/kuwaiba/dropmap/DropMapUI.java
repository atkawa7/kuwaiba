/*
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
package org.kuwaiba.dropmap;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

/**
 * View to drag an node choose on the Tree to drag in the map
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Theme("mytheme")
public class DropMapUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Navigator navigator = new Navigator(this, this);
        getNavigator().addView(MapView.NAME, MapView.class);
        navigator.navigateTo(MapView.NAME);
    }

    @WebServlet(urlPatterns = "/*", name = "DropMapUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DropMapUI.class, widgetset = "org.kuwaiba.KuwaibaWidgetSet")
    public static class MyUIServlet extends VaadinServlet {
    }
}
