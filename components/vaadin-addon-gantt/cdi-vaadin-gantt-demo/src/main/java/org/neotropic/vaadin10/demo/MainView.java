/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.vaadin10.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
* The root view. From here you will be able to launch a demo that wraps the DHTMLX Gantt library.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("")
@Theme(Lumo.class)
public class MainView extends VerticalLayout {

    public MainView() {
        setAlignItems(Alignment.CENTER);
        //We will add a button and a link. They both do the same. It's just to demonstrate how to navigate between views.
        //First the Gantt chart library wrapper
        Button btnChart = new Button("Click me to see a nice Gantt Chart");
        
        add(btnChart);
        btnChart.addClickListener((anEvent) -> {
            btnChart.getUI().ifPresent(ui -> ui.navigate("gantt"));
        });
        
        add(new RouterLink("You can also click here if you don't like buttons!", GanttView.class));
    }
}