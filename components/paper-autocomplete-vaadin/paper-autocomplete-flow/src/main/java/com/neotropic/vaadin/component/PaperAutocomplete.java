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
package com.neotropic.vaadin.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T>
 */
@Tag("paper-autocomplete")
@JsModule("@cwmr/paper-autocomplete/paper-autocomplete.js")
@NpmPackage(value = "@cwmr/paper-autocomplete", version = "4.0.0")
public class PaperAutocomplete<T extends Object> extends Component {
    private ValueProvider<T, ?> textProvider;
    private ValueProvider<T, ?> valueProvider;
        
    public PaperAutocomplete() {
    }
    public PaperAutocomplete(String id, String label) {
        setId(id);
        setLabel(label);
    }
    //<editor-fold desc="Properties" defaultstate="collapsed">
    public void setClass(String propertyClass) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.CLASS.property(), propertyClass);
    }
    public void setLabel(String label) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.LABEL.property(), label);
    }
    public void setPlaceholder(String placeholder) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.PLACEHOLDER.property(), placeholder);
    }
    /**
     * @param noLabelFloat default value false
     */
    public void setNoLabelFloat(boolean noLabelFloat) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.NO_LABEL_FLOAT.property(), noLabelFloat);
    }
    /**
     * @param alwaysFloatLabel default value false
     */
    public void setAlwaysFloatLabel(boolean alwaysFloatLabel) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.ALWAYS_FLOAT_LABEL.property(), alwaysFloatLabel);
    }
    public void setSource(ValueProvider<T, ?> textProvider, ValueProvider<T, ?> valueProvider, List<T> source) {
        JsonArray sourceAsJsonArray = getDataAsJsonArray(textProvider, valueProvider, source);
        if (sourceAsJsonArray != null)
            getElement().setPropertyJson(Constants.PaperAutocomplete.Property.SOURCE.property(), sourceAsJsonArray);
    }
    /**
     * @param highlightFirst default value false
     */
    public void setHighlightFirst(boolean highlightFirst) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.HIGHLIGHT_FIRST.property(), highlightFirst);
    }
    /**
     * @param showResultsOnFocus default value false
     */
    public void setShowResultsOnFocus(boolean showResultsOnFocus) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.SHOW_RESULTS_ON_FOCUS.property(), showResultsOnFocus);
    }
    
    public void setRemoteSource(boolean remoteSource) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.REMOTE_SOURCE.property(), remoteSource);
    }
    
    public void setMinLength(double minLength) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.MIN_LENGTH.property(), minLength);
    }
    public void setTextProperty(String textProperty) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.TEXT_PROPERTY.property(), textProperty);
    }
    public void setValueProperty(String valueProperty) {
        getElement().setProperty(
            Constants.PaperAutocomplete.Property.VALUE_PROPERTY.property(), valueProperty);
    }
    public void setTextProvider(ValueProvider<T, ?> textProvider) {
        this.textProvider = textProvider;
    }
    public void setValueProvider(ValueProvider<T, ?> valueProvider) {
        this.valueProvider = valueProvider;
    }
    @Synchronize(property = "value", value = "autocomplete-selected")
    public JsonObject getValue() {
        return (JsonObject) getElement().getPropertyRaw(Constants.PaperAutocomplete.Property.VALUE.property());
    }
    //</editor-fold>
    //<editor-fold desc="Registrations" defaultstate="collapsed">
    /**
     * @param listener
     * @return
     */
    public Registration addAutocompleteSelectedListener(ComponentEventListener<PaperAutocompleteEvent.AutocompleteSelectedEvent> listener) {
        return addListener(PaperAutocompleteEvent.AutocompleteSelectedEvent.class, listener);
    }
    /**
     * Notes
     * Set true the remoteSource property 
     * @param listener
     * @return 
     */
    public Registration addAutocompleteChange(ComponentEventListener<PaperAutocompleteEvent.AutocompleteChangeEvent> listener) {
        return addListener(PaperAutocompleteEvent.AutocompleteChangeEvent.class, listener);
    }
    //</editor-fold>
    //suggestions
    public void suggestions(List<T> suggestions) {
        JsonArray suggestionsAsJsonArray = getDataAsJsonArray(textProvider, valueProvider, suggestions);
        if (suggestionsAsJsonArray != null)
            getElement().executeJs("this.suggestions($0)", suggestionsAsJsonArray);
    }
    //<editor-fold desc="Util" defaultstate="collapsed">
    private JsonArray getDataAsJsonArray(ValueProvider<T, ?> textProvider, ValueProvider<T, ?> valueProvider, List<T> data) {
        if (textProvider == null)
            throw new IllegalArgumentException("textProvider cannot be null");
        if (valueProvider == null)
            throw new IllegalArgumentException("valueProvider cannot be null");
        this.textProvider = textProvider;
        this.valueProvider = valueProvider;
        
        if (data != null && !data.isEmpty()) {
            String valueProperty = getElement().getProperty(
                Constants.PaperAutocomplete.Property.VALUE_PROPERTY.property(), 
                Constants.JsonKey.VALUE.key());
            String textProperty = getElement().getProperty(
                Constants.PaperAutocomplete.Property.TEXT_PROPERTY.property(), 
                Constants.JsonKey.TEXT.key());
            
            JsonArray jsonArray = Json.createArray();
            for (int i = 0; i < data.size(); i++) {
                Object value = valueProvider.apply(data.get(i));
                Object text = textProvider.apply(data.get(i));
                
                JsonObject jsonObj = Json.createObject();
                
                if (value instanceof JsonValue)
                    jsonObj.put(valueProperty, (JsonValue) value);
                else if (value instanceof String)
                    jsonObj.put(valueProperty, (String) value);
                else if (value instanceof Boolean)
                    jsonObj.put(valueProperty, (Boolean) value);
                else if (value instanceof Double)
                    jsonObj.put(valueProperty, (Double) value);
                else
                    throw new IllegalArgumentException("Unsupported type, supported types JsonValue, String, Boolean, Double");
                
                if (text instanceof JsonValue)
                    jsonObj.put(textProperty, (JsonValue) text);
                else if (text instanceof String)
                    jsonObj.put(textProperty, (String) text);
                else if (text instanceof Boolean)
                    jsonObj.put(textProperty, (Boolean) text);
                else if (text instanceof Double)
                    jsonObj.put(textProperty, (Double) text);
                else
                    throw new IllegalArgumentException("Unsupported type, supported types JsonValue, String, Boolean, Double");
                
                jsonArray.set(i, jsonObj);
            }
            return jsonArray;
        }
        return null;
    }
    //</editor-fold>
}
