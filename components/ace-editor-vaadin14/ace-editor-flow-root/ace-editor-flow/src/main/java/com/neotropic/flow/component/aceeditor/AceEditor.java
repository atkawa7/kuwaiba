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
package com.neotropic.flow.component.aceeditor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

/**
 * Main component class for ace editor web component test
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
@Tag("ace-editor")
@JsModule("./ace-editor/ace-editor.js")
public final class AceEditor extends Component {

    private static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_HEIGHT = "height";
    private static final String PROPERTY_MAX_WIDTH = "maxWidth";
    private static final String PROPERTY_MAX_HEIGHT = "maxHeight";
    private static final String PROPERTY_MODE = "mode";
    private static final String PROPERTY_THEME = "theme";
    private static final String PROPERTY_FONT_SIZE = "fontSize";
    private static final String PROPERTY_READ_ONLY = "readOnly";
    private static final String PROPERTY_WRAP = "wrap";
    private static final String PROPERTY_MAX_LINES = "maxLines";
    private static final String PROPERTY_MIN_LINES = "minLines";
    private static final String PROPERTY_VALUE = "value";
    private static final String PROPERTY_ENABLE_LIVE_AUTO_COMPLETION = "enableLiveAutocompletion";
    
    public AceEditor() {
        setWidth("100%");
        setHeight("100%");
    }
        
    public String getWidth() {
        return getElement().getProperty(PROPERTY_WIDTH);
    }
        
    public void setWidth(String prop) {
        getElement().setProperty(PROPERTY_WIDTH, prop);
        getElement().getStyle().set(PROPERTY_WIDTH, prop);
    }
    
    public String getHeight() {
        return getElement().getProperty(PROPERTY_HEIGHT);
    }
        
    public void setHeight(String prop) {
        getElement().setProperty(PROPERTY_HEIGHT, prop);
        getElement().getStyle().set(PROPERTY_HEIGHT, prop);
    }
    
    public String getMaxWidth() {
        return getElement().getProperty(PROPERTY_MAX_WIDTH);
    }
    
    public void setMaxWidth(String prop) {
        getElement().setProperty(PROPERTY_MAX_WIDTH, prop);
    }
    
    public String getMaxHeight() {
        return getElement().getProperty(PROPERTY_MAX_HEIGHT);
    }
        
    public void setMaxHeight(String prop) {
        getElement().setProperty(PROPERTY_MAX_HEIGHT, prop);

    }

	public void setMode(AceMode mode) {
		getElement().setProperty(PROPERTY_MODE, "ace/mode/" + mode);
	}

	public void setTheme(AceTheme theme) {
		getElement().setProperty(PROPERTY_THEME, "ace/theme/" + theme);
	}

	public void setFontsize(Integer fontsize) {
		getElement().setProperty(PROPERTY_FONT_SIZE, fontsize);
	}

	public void setReadonly(Boolean readonly) {
		getElement().setProperty(PROPERTY_READ_ONLY, readonly);
	}

	public void setWrap(Boolean wrap) {
		getElement().setProperty(PROPERTY_WRAP, wrap);
	}

	public void setMaxLines(Integer maxLines) {
		getElement().setProperty(PROPERTY_MAX_LINES, maxLines);
	}

	public void setMinLines(Integer minLines) {
		getElement().setProperty(PROPERTY_MIN_LINES, minLines);
	}
        
        public void setEnableLiveAutoCompletion(Boolean value) {
		getElement().setProperty(PROPERTY_ENABLE_LIVE_AUTO_COMPLETION, value);
	}

	public void setValue(String value) {
		getElement().setProperty(PROPERTY_VALUE, value);
	}

	@Synchronize(property = PROPERTY_VALUE, value = "value-changed")
	public String getValue() {
		return getElement().getProperty(PROPERTY_VALUE);
	}

	public Registration addAceEditorLoadedListener(ComponentEventListener<AceEditorLoadedEvent> clickListener) {
            return super.addListener(AceEditorLoadedEvent.class, clickListener);
        }
        
	public Registration addAceEditorValueChangedListener(ComponentEventListener<AceEditorValueChangedEvent> clickListener) {
            return super.addListener(AceEditorValueChangedEvent.class, clickListener);
        }

}