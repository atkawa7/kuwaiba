/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.vaadin.component.demo.states;

import com.neotropic.vaadin.component.demo.model.Source;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("autocomplete-search")
@NpmPackage(value="@cwmr/paper-autocomplete", version="4.0.0")
@JsModule("./src/autocomplete-search.js")
public class AutocompleteSearch extends PolymerTemplate<SourceModel> {

    public AutocompleteSearch() {
    }
    public AutocompleteSearch(String label, List<Source> source, boolean suffix, boolean prefix, String icon) {
        setLabel(label);
        setSource(source);
        if (suffix)
            setSuffix();
        if (prefix)
            setPrefix();
        setIcon(icon);
    }
    public void setLabel(String label) {
        getModel().setLabel(label);
    }
    public void setSource(List<Source> source) {
        getModel().setSource(source);
    }
    public void setSuffix() {
        getModel().setSlotId("suffix"); //NOI18N
    }
    public void setPrefix() {
        getModel().setSlotId("prefix"); //NOI18N
    }
    public void setIcon(String icon) {
        getModel().setIcon(icon);
    }
}
