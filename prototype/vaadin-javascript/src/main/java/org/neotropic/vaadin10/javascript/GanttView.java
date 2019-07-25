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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.neotropic.vaadin10.javascript.services.ProjectsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The view that will display the actual Gantt chart.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
//The URI
@Route("gantt")
//The necessary CSS and Javascript dependencies
@JavaScript("/js/dhtmlxg/dhtmlxgantt.js?v=6.1.6")
@StyleSheet("/js/dhtmlxg/dhtmlxgantt.css?v=6.1.6")
public class GanttView extends VerticalLayout {
    
    @Autowired
    private ProjectsService service;

    public GanttView(ProjectsService service) {
        this.service = service;
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setSizeFull();
        add(new GanttChartDiv());
        attachEvent.getUI().getPage().executeJavaScript(service.createProject());
    }
    
    @Tag("div")
    public static class GanttChartDiv extends Component {
        public GanttChartDiv() {
            getElement().setProperty("id", "gantt");
            getElement().setProperty("style", "width:100%;height:100%");
        }
    }
}
