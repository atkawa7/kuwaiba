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

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfoLight;

/**
 *
 *  @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class OpenViewWindow extends Window {
    private ViewInfoLight view;
    private boolean ok;
    
    public OpenViewWindow(ViewInfoLight [] views) {
        center();
        VerticalLayout content = new VerticalLayout();        
        content.setMargin(true);
        
        ListSelect lstSelect = new ListSelect("Choose a view");
        
        for (ViewInfoLight view : views)
            lstSelect.addItem(view);
        
        lstSelect.setRows(5);
        lstSelect.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                view = (ViewInfoLight) event.getProperty().getValue();
            }
        });
        content.addComponent(lstSelect);
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(true);
        
        Button btnOk = new Button("Ok");
        btnOk.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                ok = true;
                close();
            }
        });
        buttonsLayout.addComponent(btnOk);
        
        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                ok = false;
                close();
            }
        });
        buttonsLayout.addComponent(btnCancel);
        content.addComponent(buttonsLayout);
        
        setResizable(false);
        setClosable(false);
        setContent(content);
    }
    
    public void showWindow() {
        
    }
    
    public boolean isOk() {
        return ok;
    }
    
    public ViewInfoLight getView() {
        return view;
    }
}
