/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Confirm dialog content used to create a container connection
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogContentNewContainer extends FormLayout {
    /**
     * Command to execute to create a container connection
     */
    private final Command command;
    
    public DialogContentNewContainer(TranslationService ts) {
        TextField txtName = new TextField();
        ComboBox cmbClass = new ComboBox();
        ComboBox cmbTemplate = new ComboBox();
        Checkbox chkUseTemplate = new Checkbox();
        
        addFormItem(txtName, ts.getTranslatedString("module.general.labels.name"));
        addFormItem(cmbClass, ts.getTranslatedString("module.ospman.new-connection-class"));
        addFormItem(cmbTemplate, ts.getTranslatedString("module.ospman.new-connection-template"));
        addFormItem(chkUseTemplate, ts.getTranslatedString("module.ospman.new-connection-use-template"));
        command = () -> {
            
        };
    }
    /**
     * Gets the command to execute to create a container connection
     * @return 
     */
    public Command getCommand() {
        return command;
    }
}
