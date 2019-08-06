/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.apis.web.gui.properties;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import java.util.List;

/**
 * An embeddable property sheet 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertySheet extends Grid<AbstractProperty> {

    public PropertySheet() {
        setHeaderVisible(false);
        setSizeUndefined();
        addComponentColumn((property) -> {
            Label label = new Label("<b>" + property.getName() + "</b>", ContentMode.HTML); //NOI18N
            return label;
        }).setWidth(170);
        addComponentColumn((property) -> {
            Label label = new Label(property.getAsString());
            label.setWidthUndefined();
            label.setStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
            return label;
        });
    }
    
    public PropertySheet(List<AbstractProperty> properties, String caption) {
        this();
        setItems(properties);
        setCaption(caption);
    }

    public void clear() {
        setItems();
        setCaption("");
    }
}
