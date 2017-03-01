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
import com.vaadin.ui.Component;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.kuwaiba.apis.web.gui.windows.MessageDialogWindow;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfoLight;

/**
 *
 *  @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class OpenViewWindow extends MessageDialogWindow {
    private ViewInfoLight view;

    private final ListSelect lstSelect = new ListSelect("Choose a view");
    /**
     * Listeners
     */
    private final Property.ValueChangeListener valueChangeListener;
    
    public OpenViewWindow(Window.CloseListener closeListener, ViewInfoLight [] views) {
        super(closeListener, "", MessageDialogWindow.OK_CANCEL_OPTION);
        
        for (ViewInfoLight view : views)
            lstSelect.addItem(view);
                        
        lstSelect.setSizeFull();
        lstSelect.setRows(5);
        
        valueChangeListener = new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                view = (ViewInfoLight) event.getProperty().getValue();
            }
        };

        lstSelect.addValueChangeListener(valueChangeListener);
    }   
    
    public ViewInfoLight getView() {
        return view;
    }
    
    @Override    
    public void close() {
        lstSelect.removeValueChangeListener(valueChangeListener);
        super.close();
    }

    @Override
    public Component initSimpleMainComponent() {
        return null;
    }

    @Override
    public void initComplexMainComponent() {
        VerticalLayout content = new VerticalLayout();        
        content.setMargin(true);
        
        content.addComponent(lstSelect);
        
        setMainComponent(content);
    }
}
