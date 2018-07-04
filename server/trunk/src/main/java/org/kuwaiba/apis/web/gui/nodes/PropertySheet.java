/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.nodes;

import com.vaadin.ui.Grid;
import java.util.List;

/**
 * A en embeddable property sheet 
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class PropertySheet extends Grid<AbstractProperty> {

    public PropertySheet(List<AbstractProperty> properties, String caption) {
        setItems(properties);
        setCaption(caption);
        setHeaderVisible(false);
        setSizeFull();
        addColumn(AbstractProperty::getName);
        addColumn(AbstractProperty::getAsString);
    }

    public void clear() {
        setItems();
        setCaption("");
    }
}
