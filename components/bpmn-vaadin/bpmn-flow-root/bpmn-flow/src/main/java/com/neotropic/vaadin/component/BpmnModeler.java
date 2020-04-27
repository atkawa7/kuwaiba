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
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.Json;
import elemental.json.JsonArray;

/**
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("bpmn-modeler")
@JsModule("@neotropic-sas/bpmn-element/bpmn-modeler.js")
@NpmPackage(value = "@neotropic-sas/bpmn-element", version = "^1.0.4")
public class BpmnModeler extends Component {
    
    public BpmnModeler() {
        getElement().getStyle().set("width", "100%");
        getElement().getStyle().set("height", "100%");
        getElement().getStyle().set("margin", "0");
        getElement().getStyle().set("padding", "0");
        
        JsonArray jsonArray = Json.createArray();
        jsonArray.set(0, "src/bpmn-js@6.4.2/assets/diagram-js.css");
        jsonArray.set(1, "src/bpmn-js@6.4.2/assets/bpmn-font/css/bpmn.css");
        getElement().setPropertyJson(Property.MODELER_STYLES.getName(), jsonArray);
        
        getElement().setProperty(Property.MODELER_DISTRO.getName(), 
            "src/bpmn-js@6.4.2/bpmn-modeler.production.min.js");
    }
    public BpmnModeler(String containerId) {
        this();
        getElement().setProperty(Property.CONTAINER_ID.getName(), containerId);
    }
    public BpmnModeler(boolean djsPalette, boolean djsContextPad) {
        this();
        getElement().setProperty(Property.DJS_PALETTE.getName(), djsPalette);
        getElement().setProperty(Property.DJS_CONTEXT_PAD.getName(), djsContextPad);
    }
    public BpmnModeler(String containerId, String diagramUrl) {
        this(containerId);
        getElement().setProperty(Property.DIAGRAM_URL.getName(), diagramUrl);
    }
    public BpmnModeler(String containerId, String diagramUrl, boolean djsPalette, boolean djsContextPad) {
        this(containerId, diagramUrl);
        getElement().setProperty(Property.DJS_PALETTE.getName(), djsPalette);
        getElement().setProperty(Property.DJS_CONTEXT_PAD.getName(), djsContextPad);
    }
    public BpmnModeler(String diagramUrl, boolean djsPalette, boolean djsContextPad) {
        this(djsPalette, djsContextPad);
        getElement().setProperty(Property.DIAGRAM_URL.getName(), diagramUrl);
    }
    public void exportDiagram(SerializableConsumer<String> consumer) {
        getElement().executeJs("this.exportDiagram()");
        getElement().executeJs("return this.xml").then(String.class, consumer);
    }
}
