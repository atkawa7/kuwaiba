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
import elemental.json.JsonArray;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("paper-autocomplete")
@JsModule("@cwmr/paper-autocomplete/paper-autocomplete.js")
@NpmPackage(value = "@cwmr/paper-autocomplete", version = "4.0.0")
public class PaperAutocomplete extends Component {
    public PaperAutocomplete() {
    }
    public void setClass(String propertyClass) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.CLASS.getPropertyName(), propertyClass);
    }
    public void setLabel(String label) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.LABEL.getPropertyName(), label);
    }
    public void setPlaceholder(String placeholder) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.PLACEHOLDER.getPropertyName(), placeholder);
    }
    /**
     * @param noLabelFloat default value false
     */
    public void setNoLabelFloat(boolean noLabelFloat) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.NO_LABEL_FLOAT.getPropertyName(), noLabelFloat);
    }
    /**
     * @param alwaysFloatLabel default value false
     */
    public void setAlwaysFloatLabel(boolean alwaysFloatLabel) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.ALWAYS_FLOAT_LABEL.getPropertyName(), alwaysFloatLabel);
    }
    public void setSource(JsonArray source) {
        getElement().setPropertyJson(
            Constants.PaperAutocomplete.Property.SOURCE.getPropertyName(), source);
    }
    /**
     * @param highlightFirst default value false
     */
    public void setHighlightFirst(boolean highlightFirst) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.HIGHLIGHT_FIRST.getPropertyName(), highlightFirst);
    }
    /**
     * @param showResultsOnFocus default value false
     */
    public void setShowResultsOnFocus(boolean showResultsOnFocus) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.SHOW_RESULTS_ON_FOCUS.getPropertyName(), showResultsOnFocus);
    }
}
