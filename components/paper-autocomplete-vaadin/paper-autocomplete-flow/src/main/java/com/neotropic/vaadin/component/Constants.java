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
        String getPropertyName();
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
            HIGHLIGHT_FIRST ("highlight-first"), //NOI18N
            SHOW_RESULTS_ON_FOCUS ("show-results-on-focus"); //NOI18N

            private final String propertyName;

            private Property(String propertyName) {
                this.propertyName = propertyName;
            }
            @Override
            public String getPropertyName() {
                return propertyName;
            }
        }
    }
    public static class PaperIconButton {
        public enum Property implements EnumProperty {
            SLOT ("slot"), //NOI18N
            SUFFIX ("suffix"), //NOI18N
            ICON ("icon"), //NOI18N
            PREFIX ("prefix"); //NOI18N

            private final String propertyName;

            private Property(String propertyName) {
                this.propertyName = propertyName;
            }
            @Override
            public String getPropertyName() {
                return propertyName;
            }
        }
    }
}
