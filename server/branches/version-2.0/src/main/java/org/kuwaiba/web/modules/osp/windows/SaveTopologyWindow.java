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

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SaveTopologyWindow extends Window {
    private String viewName;
    private String viewDescription;
    private boolean ok = false;
    
    public SaveTopologyWindow(String name, String description) {
        super("Save Topology");
        center();
        viewName = name;
        viewDescription = description;
        
        VerticalLayout content = new VerticalLayout();
        
        FormLayout form = new FormLayout();
        form.setMargin(true);
        
        TextField tfName = new TextField("Name");
        tfName.setValue(viewName);
        tfName.setRequired(true);
        form.addComponent(tfName);
        
        TextField tfDescription = new TextField("Description");
        tfDescription.setValue(viewDescription);
        form.addComponent(tfDescription);
        
        content.addComponent(form);
        
        HorizontalLayout btnsLayout = new HorizontalLayout();
        
        Button btnOk = new Button("Ok");
        btnOk.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                viewName = tfName.getValue();
                viewDescription = tfDescription.getValue();
                ok = true;
                close();
            }
        });
        btnsLayout.addComponent(btnOk);
        
        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
//                tfName.setValue("");
//                tfDescription.setValue("");
                ok = false;
                close();
            }
        });
        btnsLayout.addComponent(btnCancel);
        
        content.addComponent(btnsLayout);
        
        setResizable(false);
        setClosable(false);
        setContent(content);        
    }
    
    public boolean isOk() {
        return ok;
    }

    public String getViewName() {
        return viewName;
    }
    
    public String getViewDescription() {
        return viewDescription;
    }
}
