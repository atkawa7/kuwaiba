/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.wizards.connection;

import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import org.vaadin.teemu.wizards.WizardStep;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FirstStep implements WizardStep {
    private ConnectionConfiguration connConfig;
    private FormLayout content;
    
    public ConnectionConfiguration getConnConfig() {
        return connConfig;
    }
    
    @Override
    public String getCaption() {
        return "Configuration ";
    }

    @Override
    public Component getContent() {
        content = new FormLayout();
        content.setSizeFull();
        content.setMargin(true);
        
        TextField tfCaption = new TextField("Caption");
                
        TextField tfStrokeColor = new TextField("Stroke color");
        tfStrokeColor.setRequired(true);
        
        TextField tfStrokeOpacity = new TextField("Stroke opacity", "1");
        tfStrokeOpacity.addValidator(new DoubleRangeValidator("Must be double", 0.0, Double.MAX_VALUE));
        
        TextField tfStrokeWeight = new TextField("Stroke weight", "3");
        tfStrokeWeight.addValidator(new IntegerRangeValidator("Must be integer", 0, 99999));

//        ColorPicker colorPicker = new ColorPicker("Color picker");
        content.addComponents(tfCaption, tfStrokeColor, tfStrokeOpacity, tfStrokeWeight/*, colorPicker*/);
        return content;
    }

    @Override
    public boolean onAdvance() {
        connConfig = new ConnectionConfiguration();
        connConfig.setCaption(((TextField)content.getComponent(0)).getValue());
        connConfig.setStrokeColor(((TextField)content.getComponent(1)).getValue());
        connConfig.setStrokeOpacity(Double.valueOf(((TextField)content.getComponent(2)).getValue()));
        connConfig.setStrokeWeight(Integer.valueOf(((TextField)content.getComponent(3)).getValue()));
        return true;
    }

    @Override
    public boolean onBack() {
        return true;
    }
}
