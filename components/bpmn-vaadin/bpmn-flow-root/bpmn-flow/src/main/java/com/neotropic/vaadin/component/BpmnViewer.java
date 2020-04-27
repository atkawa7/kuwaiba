/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("bpmn-viewer")
@JsModule("@neotropic-sas/bpmn-element/bpmn-viewer.js")
@NpmPackage(value = "@neotropic-sas/bpmn-element", version = "^1.0.4")
public class BpmnViewer extends Component {
    /**
     * @param diagramUrl 
     */
    public BpmnViewer(String diagramUrl) {
        getElement().getStyle().set("width", "100%");
        getElement().getStyle().set("height", "100%");
        getElement().getStyle().set("margin", "0");
        getElement().getStyle().set("padding", "0");
        
        getElement().setProperty(Property.VIEWER_DISTRO.getName(), 
            "src/bpmn-js@6.4.2/bpmn-navigated-viewer.production.min.js");
        getElement().setProperty(Property.DIAGRAM_URL.getName(), diagramUrl);
    }
    /**
     * @param containerId 
     * @param diagramUrl 
     */
    public BpmnViewer(String containerId, String diagramUrl) {
        this(diagramUrl);
        getElement().setProperty(Property.CONTAINER_ID.getName(), containerId);
    }
}