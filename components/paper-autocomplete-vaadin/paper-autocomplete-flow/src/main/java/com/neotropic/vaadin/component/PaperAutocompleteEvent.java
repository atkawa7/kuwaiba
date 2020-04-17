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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PaperAutocompleteEvent {
    @DomEvent("autocomplete-selected")
    public static class AutocompleteSelectedEvent extends ComponentEvent<PaperAutocomplete> {
        private final String text;

        public AutocompleteSelectedEvent(PaperAutocomplete source, boolean fromClient, 
            @EventData("event.detail.text") String text) {
            super(source, fromClient);
            this.text = text;
        }
        public String getText() {
            return text;
        }
    }
    @DomEvent("autocomplete-change")
    public static class AutocompleteChangeEvent extends ComponentEvent<PaperAutocomplete> {
        private final String optionText;
        
        public AutocompleteChangeEvent(PaperAutocomplete source, boolean fromClient,
            @EventData("event.detail.option.text") String optionText) {
            super(source, fromClient);
            this.optionText = optionText;
        }
        public String getOptionText() {
            return optionText;
        }
    }
}
