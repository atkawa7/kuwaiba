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
package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;


/**
 * Support for Long properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class LongProperty extends AbstractProperty<Long>{

    public LongProperty(String name, String displayName, String description, Long value) {
        super(name, displayName, description, value);
    }

    @Override
    public Component getAdvancedEditor() {
        NumberField nbrField = new NumberField(this.getName(), "...");  
        nbrField.setStep(1);
        
        nbrField.setWidthFull();
        nbrField.setMinHeight("300px");
        return nbrField;
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return true;
    }

    @Override
    public AbstractField getInplaceEditor() {
        NumberField nbrField = new NumberField("", "...");
        nbrField.setWidthFull();

        Binder<LongProperty> binder = new Binder<>(LongProperty.class);
        binder.forField(nbrField)
                .withConverter(
                        new DoubleToLongConverter())
                .bind(LongProperty::getValue,
                        LongProperty::setValue);

        binder.setBean(this);
        
        this.setHasBinder(true);

        return nbrField;
    }

    @Override
    public String getAsString() {
        return getValue() + "";
    }

    @Override
    public String getAsStringToPersist() {
        return getAsString();
    }

}
