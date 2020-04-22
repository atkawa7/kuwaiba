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

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Constants {
    private interface EnumProperty {
        String property();
    }
    public enum JsonKey {
        TEXT ("text"),
        VALUE ("value");

        private final String key;

        JsonKey(String key) {
            this.key = key;
        }
        public String key() {
            return key;
        }
    }
    public static class PaperAutocomplete {
        public enum Property implements EnumProperty {
            CLASS ("class"), //NOI18N
            ID ("id"), //NOI18N
            LABEL ("label"), //NOI18N
            PLACEHOLDER ("placeholder"), //NOI18N
            NO_LABEL_FLOAT ("noLabelFloat"), //NOI18N
            ALWAYS_FLOAT_LABEL ("alwaysFloatLabel"), //NOI18N
            SOURCE ("source"), //NOI18N
            HIGHLIGHT_FIRST ("highlightFirst"), //NOI18N
            SHOW_RESULTS_ON_FOCUS ("showResultsOnFocus"), //NOI18N
            REMOTE_SOURCE ("remoteSource"), //NOI18N
            MIN_LENGTH ("minLength"), //NOI18N
            VALUE ("value"), //NOI18N
            TEXT_PROPERTY ("textProperty"), //NOI18N
            VALUE_PROPERTY ("valueProperty"); //NOI18N
            
            private final String propertyName;

            private Property(String propertyName) {
                this.propertyName = propertyName;
            }
            @Override
            public String property() {
                return propertyName;
            }
        }
    }
}
