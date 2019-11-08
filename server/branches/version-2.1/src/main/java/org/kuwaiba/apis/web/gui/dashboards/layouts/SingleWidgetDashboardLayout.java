/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.apis.web.gui.dashboards.layouts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


/**
 * Use this layout if you want a single widget to use all the available space for the dashboard
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SingleWidgetDashboardLayout extends VerticalLayout {

    public SingleWidgetDashboardLayout() {
        setSizeFull();
    }

    // no existe metodo en Vertical Layout para definir en que orden guarda los elementos, habria que crearlo
    // desde cero
//    @Override
//    public void add(Component c, int index) {
//        if (index == 0)
//            super.add(c, index);
//    }

}
