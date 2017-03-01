/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.windows;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.kuwaiba.apis.web.gui.windows.MessageDialogWindow;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SaveTopologyWindow extends MessageDialogWindow {
    TextField tfName = new TextField("Name");
    TextField tfDescription = new TextField("Description");
        
    public SaveTopologyWindow(Window.CloseListener closeListener, String viewName, String viewDescription) {
        super(closeListener, "Confirmation", MessageDialogWindow.OK_CANCEL_OPTION);
        
        tfName.setValue(viewName);
        tfName.setRequired(true);
        
        tfDescription.setValue(viewDescription);
    }

    public String getViewName() {
        return tfName.getValue();
    }
    
    public String getViewDescription() {
        return tfDescription.getValue();
    }
    
    @Override
    public void initComplexMainComponent() {
        VerticalLayout content = new VerticalLayout();
        
        FormLayout form = new FormLayout();
        form.setMargin(true);

        form.addComponent(tfName);
        form.addComponent(tfDescription);
        
        content.addComponent(form);
        
        setMainComponent(content);
    }

    @Override
    public Component initSimpleMainComponent() {
        return null;
    }
}
