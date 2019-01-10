/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Support for date-type properties
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DateProperty extends AbstractProperty<Date>{

    public DateProperty(String name, String displayName, String description, Date value) {
        super(name, displayName, description, value);
    }
    
    public DateProperty(String name, String displayName, String description, long value) {
        super(name, displayName, description, new Date(value));
    }

    @Override
    public Component getAdvancedEditor() {
        throw new UnsupportedOperationException("This property type does not support an advanced editor."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return false;
    }

    @Override
    public AbstractField getInplaceEditor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMMM d, YYYY HH:mm:ss");
        return formatter.format(getValue());
    }

}
